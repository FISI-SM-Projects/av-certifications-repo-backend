package pe.edu.unmsm.fisi.gestiondocente.constancia.repository;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.GenerationAlreadyExistsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidStoragePathException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.StorageException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.StoragePathSanitizer;

@Repository("fileSystemConstanciaRepository")
public class FileSystemConstanciaRepository implements ConstanciaRepository {

    private static final String CERTIFICATES_DIR = "certificates";
    private static final String COURSE_DIR = "course";
    private static final String SEMESTER_DIR = "semester";
    private static final String REQUEST_FILE = "request.json";
    private static final String METADATA_FILE = "metadata.json";
    private static final String PDF_FILE = "certificate.pdf";

    private final Path root;
    private final ObjectMapper objectMapper;
    private final StoragePathSanitizer storagePathSanitizer;

    @Autowired
    public FileSystemConstanciaRepository(@Value("${app.storage.root:storage}") String storageRoot,
            ObjectMapper objectMapper, StoragePathSanitizer storagePathSanitizer) {
        this(Path.of(storageRoot), objectMapper, storagePathSanitizer);
    }

    public FileSystemConstanciaRepository(Path storageRoot, ObjectMapper objectMapper,
            StoragePathSanitizer storagePathSanitizer) {
        this.root = storageRoot.toAbsolutePath().normalize();
        this.objectMapper = objectMapper;
        this.storagePathSanitizer = storagePathSanitizer;
    }

    @Override
    public CertificateGenerationMetadata saveGeneration(Object request,
            CertificateGenerationMetadata metadata, byte[] pdfBytes) {
        validateMetadata(metadata);

        Path generationDirectory = resolveGenerationDirectory(metadata);
        if (Files.exists(generationDirectory)) {
            throw new GenerationAlreadyExistsException(metadata.getGenerationId());
        }

        Path parentDirectory = generationDirectory.getParent();
        Path tempDirectory = parentDirectory.resolve(".tmp-" + metadata.getGenerationId() + "-"
                + UUID.randomUUID()).normalize();
        assertInsideRoot(tempDirectory);

        try {
            Files.createDirectories(parentDirectory);
            Files.createDirectory(tempDirectory);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(
                    tempDirectory.resolve(requestFileName(metadata)).toFile(), request);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempDirectory.resolve(METADATA_FILE).toFile(),
                    metadata);

            if (pdfBytes != null) {
                Files.write(tempDirectory.resolve(PDF_FILE), pdfBytes);
            }

            moveDirectory(tempDirectory, generationDirectory);
            return metadata;
        } catch (IOException | RuntimeException exception) {
            deleteDirectoryIfExists(tempDirectory);
            throw toStorageException(exception);
        }
    }

    @Override
    public <T> Optional<T> readRequest(String generationId, Class<T> requestType) {
        if (requestType == null) {
            throw new StorageException("El tipo de solicitud es obligatorio");
        }

        return findByGenerationId(generationId)
                .map(metadata -> resolveGenerationDirectory(metadata).resolve(requestFileName(metadata)))
                .filter(Files::exists)
                .map(path -> readRequestFile(path, requestType));
    }

    @Override
    public Optional<CertificateGenerationMetadata> findByGenerationId(String generationId) {
        String safeGenerationId = storagePathSanitizer.sanitizeSegment(generationId);

        return readAllMetadata().stream()
                .filter(metadata -> safeGenerationId.equals(metadata.getGenerationId()))
                .findFirst();
    }

    @Override
    public List<CertificateGenerationMetadata> findHistoryByCertificateKey(String certificateKey) {
        String safeCertificateKey = storagePathSanitizer.sanitizeSegment(certificateKey);

        return readAllMetadata().stream()
                .filter(metadata -> safeCertificateKey.equals(metadata.getCertificateKey()))
                .sorted(Comparator.comparingInt(CertificateGenerationMetadata::getVersion))
                .toList();
    }

    @Override
    public Optional<CertificateGenerationMetadata> findLatestByCertificateKey(String certificateKey) {
        return findHistoryByCertificateKey(certificateKey).stream()
                .max(Comparator.comparingInt(CertificateGenerationMetadata::getVersion));
    }

    @Override
    public List<CertificateGenerationMetadata> findLatestByTeacherCode(String teacherCode) {
        String safeTeacherCode = storagePathSanitizer.sanitizeSegment(teacherCode);

        return readAllMetadata().stream()
                .filter(metadata -> safeTeacherCode.equals(metadata.getTeacherCode()))
                .collect(java.util.stream.Collectors.groupingBy(CertificateGenerationMetadata::getCertificateKey))
                .values().stream()
                .map(this::latestFromGroup)
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(CertificateGenerationMetadata::getCertificateKey))
                .toList();
    }

    @Override
    public List<CertificateGenerationMetadata> findByTeacherCodeAndSemester(String teacherCode, String semester) {
        String safeTeacherCode = storagePathSanitizer.sanitizeSegment(teacherCode);
        String safeSemester = storagePathSanitizer.sanitizeSegment(semester);

        return readAllMetadata().stream()
                .filter(metadata -> safeTeacherCode.equals(metadata.getTeacherCode()))
                .filter(metadata -> safeSemester.equals(metadata.getSemester()))
                .sorted(Comparator.comparing(CertificateGenerationMetadata::getCertificateKey)
                        .thenComparingInt(CertificateGenerationMetadata::getVersion))
                .toList();
    }

    @Override
    public int nextVersion(String certificateKey) {
        return findHistoryByCertificateKey(certificateKey).stream()
                .mapToInt(CertificateGenerationMetadata::getVersion)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public boolean existsApprovedByCertificateKey(String certificateKey) {
        return findHistoryByCertificateKey(certificateKey).stream()
                .anyMatch(metadata -> metadata.getStatus() == EstadoConstancia.APROBADO);
    }

    @Override
    public Optional<byte[]> readPdf(String generationId) {
        return findByGenerationId(generationId)
                .map(this::resolveGenerationDirectory)
                .map(path -> path.resolve(PDF_FILE))
                .filter(Files::exists)
                .map(this::readBytes);
    }

    private Optional<CertificateGenerationMetadata> latestFromGroup(List<CertificateGenerationMetadata> metadata) {
        return metadata.stream().max(Comparator.comparingInt(CertificateGenerationMetadata::getVersion));
    }

    private void validateMetadata(CertificateGenerationMetadata metadata) {
        if (metadata == null) {
            throw new StorageException("La metadata de generacion es obligatoria");
        }

        storagePathSanitizer.sanitizeSegment(metadata.getGenerationId());
        storagePathSanitizer.sanitizeSegment(metadata.getCertificateKey());
        storagePathSanitizer.sanitizeSegment(metadata.getTeacherCode());
        storagePathSanitizer.sanitizeSegment(metadata.getSemester());

        if (metadata.getVersion() < 1) {
            throw new StorageException("La version debe ser mayor o igual a 1");
        }

        if (metadata.getType() == TipoConstancia.CURSO) {
            storagePathSanitizer.sanitizeSegment(metadata.getCourseCode());
            storagePathSanitizer.sanitizeSegment(metadata.getSection());
        }
    }

    private String requestFileName(CertificateGenerationMetadata metadata) {
        String requestFile = metadata.getRequestFile();

        if (requestFile == null || requestFile.trim().isEmpty()) {
            return REQUEST_FILE;
        }

        return storagePathSanitizer.sanitizeSegment(requestFile);
    }

    private <T> T readRequestFile(Path requestPath, Class<T> requestType) {
        assertInsideRoot(requestPath.normalize());

        try {
            return objectMapper.readValue(requestPath.toFile(), requestType);
        } catch (IOException exception) {
            throw new StorageException("No se pudo leer la solicitud de constancia", exception);
        }
    }

    private Path resolveGenerationDirectory(CertificateGenerationMetadata metadata) {
        Path directory;
        String versionDirectory = versionDirectory(metadata.getVersion());

        if (metadata.getType() == TipoConstancia.CURSO) {
            directory = root.resolve(CERTIFICATES_DIR)
                    .resolve(COURSE_DIR)
                    .resolve(storagePathSanitizer.sanitizeSegment(metadata.getSemester()))
                    .resolve(storagePathSanitizer.sanitizeSegment(metadata.getTeacherCode()))
                    .resolve(storagePathSanitizer.sanitizeSegment(metadata.getCourseCode())
                            + "-" + storagePathSanitizer.sanitizeSegment(metadata.getSection()))
                    .resolve(versionDirectory);
        } else if (metadata.getType() == TipoConstancia.SEMESTRAL) {
            directory = root.resolve(CERTIFICATES_DIR)
                    .resolve(SEMESTER_DIR)
                    .resolve(storagePathSanitizer.sanitizeSegment(metadata.getSemester()))
                    .resolve(storagePathSanitizer.sanitizeSegment(metadata.getTeacherCode()))
                    .resolve(versionDirectory);
        } else {
            throw new StorageException("El tipo de constancia es obligatorio");
        }

        return assertInsideRoot(directory.normalize());
    }

    private String versionDirectory(int version) {
        if (version < 1) {
            throw new StorageException("La version debe ser mayor o igual a 1");
        }

        return "v" + String.format("%03d", version);
    }

    private List<CertificateGenerationMetadata> readAllMetadata() {
        Path certificatesRoot = root.resolve(CERTIFICATES_DIR).normalize();
        assertInsideRoot(certificatesRoot);

        if (!Files.exists(certificatesRoot)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.walk(certificatesRoot)) {
            return paths
                    .filter(path -> METADATA_FILE.equals(path.getFileName().toString()))
                    .filter(path -> !path.toString().contains(".tmp-"))
                    .map(this::readMetadata)
                    .toList();
        } catch (IOException exception) {
            throw new StorageException("No se pudo leer el almacenamiento de constancias", exception);
        }
    }

    private CertificateGenerationMetadata readMetadata(Path metadataPath) {
        assertInsideRoot(metadataPath.normalize());

        try {
            return objectMapper.readValue(metadataPath.toFile(), CertificateGenerationMetadata.class);
        } catch (IOException exception) {
            throw new StorageException("No se pudo leer metadata de constancia", exception);
        }
    }

    private byte[] readBytes(Path pdfPath) {
        assertInsideRoot(pdfPath.normalize());

        try {
            return Files.readAllBytes(pdfPath);
        } catch (IOException exception) {
            throw new StorageException("No se pudo leer el PDF de constancia", exception);
        }
    }

    private void moveDirectory(Path tempDirectory, Path generationDirectory) throws IOException {
        try {
            Files.move(tempDirectory, generationDirectory, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(tempDirectory, generationDirectory);
        }
    }

    private void deleteDirectoryIfExists(Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }

        assertInsideRoot(directory.normalize());

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(this::deletePath);
        } catch (IOException exception) {
            throw new StorageException("No se pudo limpiar una generacion temporal", exception);
        }
    }

    private void deletePath(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new StorageException("No se pudo limpiar una generacion temporal", exception);
        }
    }

    private Path assertInsideRoot(Path path) {
        Path normalizedPath = path.toAbsolutePath().normalize();

        if (!normalizedPath.startsWith(root)) {
            throw new InvalidStoragePathException("La ruta de almacenamiento no es valida");
        }

        return normalizedPath;
    }

    private StorageException toStorageException(Exception exception) {
        if (exception instanceof StorageException storageException) {
            return storageException;
        }

        return new StorageException("No se pudo guardar la generacion de constancia", exception);
    }
}
