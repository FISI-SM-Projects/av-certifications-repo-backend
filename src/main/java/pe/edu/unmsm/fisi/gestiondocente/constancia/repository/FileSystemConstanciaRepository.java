package pe.edu.unmsm.fisi.gestiondocente.constancia.repository;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.CertificateGenerationMetadata;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.EstadoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.entity.TipoConstancia;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.GenerationAlreadyExistsException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidPdfContentException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.InvalidStoragePathException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.exception.StorageException;
import pe.edu.unmsm.fisi.gestiondocente.constancia.validation.StoragePathSanitizer;

@Repository("fileSystemConstanciaRepository")
public class FileSystemConstanciaRepository implements CertificateGenerationRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemConstanciaRepository.class);
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
        this.objectMapper = objectMapper.copy()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.storagePathSanitizer = storagePathSanitizer;
        try {
            Files.createDirectories(this.root);
        } catch (IOException exception) {
            throw new StorageException("No se pudo preparar la raiz de almacenamiento", exception);
        }
        LOGGER.info("Raiz de almacenamiento de constancias: {}", this.root);
    }

    @Override
    public CertificateGenerationMetadata saveGeneration(Object request,
            CertificateGenerationMetadata metadata, byte[] pdfBytes) {
        validateMetadata(metadata);
        validatePdfBytes(pdfBytes);

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

            Files.write(tempDirectory.resolve(PDF_FILE), pdfBytes);
            validateTemporaryGeneration(tempDirectory, metadata);

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

        Optional<CertificateGenerationMetadata> found = readAllMetadata().stream()
                .filter(metadata -> safeGenerationId.equals(metadata.getGenerationId()))
                .findFirst();
        if (found.isPresent()) {
            return found;
        }

        throwIfCorruptMetadataAppearsToReference(safeGenerationId);
        return Optional.empty();
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
                .map(metadata -> resolveGenerationDirectory(metadata).resolve(safePdfFileName(metadata)))
                .filter(Files::exists)
                .map(this::readBytes);
    }

    private Optional<CertificateGenerationMetadata> latestFromGroup(List<CertificateGenerationMetadata> metadata) {
        if (metadata == null) {
            return Optional.empty();
        }

        return metadata.stream()
                .filter(this::isValidLoadedMetadata)
                .max(Comparator.comparingInt(CertificateGenerationMetadata::getVersion));
    }

    private void validateMetadata(CertificateGenerationMetadata metadata) {
        if (metadata == null) {
            throw new StorageException("La metadata de generacion es obligatoria");
        }

        requireText(metadata.getGenerationId(), "El identificador de generacion es obligatorio");
        requireText(metadata.getCertificateKey(), "La clave de constancia es obligatoria");
        requireText(metadata.getTeacherCode(), "El codigo docente es obligatorio");
        requireText(metadata.getSemester(), "El semestre es obligatorio");
        requireText(metadata.getRequestFile(), "El archivo de solicitud es obligatorio");
        requireText(metadata.getPdfFile(), "El archivo PDF es obligatorio");

        storagePathSanitizer.sanitizeSegment(metadata.getGenerationId());
        storagePathSanitizer.sanitizeSegment(metadata.getCertificateKey());
        storagePathSanitizer.sanitizeSegment(metadata.getTeacherCode());
        storagePathSanitizer.sanitizeSegment(metadata.getSemester());
        storagePathSanitizer.sanitizeSegment(metadata.getRequestFile());
        storagePathSanitizer.sanitizeSegment(metadata.getPdfFile());

        if (metadata.getVersion() < 1) {
            throw new StorageException("La version debe ser mayor o igual a 1");
        }

        if (metadata.getType() == null) {
            throw new StorageException("El tipo de constancia es obligatorio");
        }

        if (metadata.getStatus() == null) {
            throw new StorageException("El estado de constancia es obligatorio");
        }
        if (metadata.getGeneratedAt() == null) {
            throw new StorageException("La fecha de generacion es obligatoria");
        }
        if (!PDF_FILE.equals(metadata.getPdfFile())) {
            throw new StorageException("El archivo PDF de metadata no es valido");
        }

        if (metadata.getType() == TipoConstancia.CURSO) {
            storagePathSanitizer.sanitizeSegment(metadata.getCourseCode());
            storagePathSanitizer.sanitizeSegment(metadata.getSection());
            requireText(metadata.getCourseCode(), "El codigo de curso es obligatorio");
            requireText(metadata.getSection(), "La seccion es obligatoria");
            if (!REQUEST_FILE.equals(metadata.getRequestFile())) {
                throw new StorageException("El archivo de solicitud de curso no es valido");
            }
            validateCourseKey(metadata);
        } else if (metadata.getType() == TipoConstancia.SEMESTRAL) {
            if (metadata.getCourseCode() != null || metadata.getSection() != null) {
                throw new StorageException("La metadata semestral no debe incluir curso ni seccion");
            }
            if (!"source-summary.json".equals(metadata.getRequestFile())) {
                throw new StorageException("El archivo fuente semestral no es valido");
            }
            validateSemesterKey(metadata);
        } else {
            throw new StorageException("El tipo de constancia es obligatorio");
        }

        String expectedGenerationId = metadata.getCertificateKey() + "-v" + String.format("%03d", metadata.getVersion());
        if (!expectedGenerationId.equals(metadata.getGenerationId())) {
            throw new StorageException("El identificador de generacion no coincide con la version");
        }
    }

    private String requestFileName(CertificateGenerationMetadata metadata) {
        String requestFile = metadata.getRequestFile();

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
                    .map(this::readMetadataSafely)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (IOException exception) {
            throw new StorageException("No se pudo leer el almacenamiento de constancias", exception);
        }
    }

    private Optional<CertificateGenerationMetadata> readMetadataSafely(Path metadataPath) {
        assertInsideRoot(metadataPath.normalize());

        try {
            CertificateGenerationMetadata metadata = objectMapper.readValue(metadataPath.toFile(),
                    CertificateGenerationMetadata.class);
            validateLoadedMetadata(metadata);
            return Optional.of(metadata);
        } catch (IOException exception) {
            logCorruptMetadata(metadataPath, exception);
            return Optional.empty();
        } catch (RuntimeException exception) {
            logCorruptMetadata(metadataPath, exception);
            return Optional.empty();
        }
    }

    private void validateLoadedMetadata(CertificateGenerationMetadata metadata) {
        try {
            validateMetadata(metadata);
        } catch (StorageException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new StorageException("La metadata de constancia no es valida", exception);
        }
    }

    private boolean isValidLoadedMetadata(CertificateGenerationMetadata metadata) {
        if (metadata == null) {
            return false;
        }

        validateLoadedMetadata(metadata);
        return true;
    }

    private byte[] readBytes(Path pdfPath) {
        assertInsideRoot(pdfPath.normalize());

        try {
            byte[] bytes = Files.readAllBytes(pdfPath);
            validatePdfBytes(bytes);
            return bytes;
        } catch (IOException exception) {
            throw new StorageException("No se pudo leer el PDF de constancia", exception);
        }
    }

    private void moveDirectory(Path tempDirectory, Path generationDirectory) throws IOException {
        try {
            Files.move(tempDirectory, generationDirectory, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            // Some filesystems do not support atomic directory moves; keep the no-overwrite fallback.
            if (Files.exists(generationDirectory)) {
                throw new GenerationAlreadyExistsException(generationDirectory.getFileName().toString());
            }
            Files.move(tempDirectory, generationDirectory);
        }
    }

    private void validateTemporaryGeneration(Path tempDirectory, CertificateGenerationMetadata metadata)
            throws IOException {
        Path requestPath = tempDirectory.resolve(requestFileName(metadata));
        Path metadataPath = tempDirectory.resolve(METADATA_FILE);
        Path pdfPath = tempDirectory.resolve(PDF_FILE);

        if (!Files.isRegularFile(requestPath) || Files.size(requestPath) == 0
                || !Files.isRegularFile(metadataPath) || Files.size(metadataPath) == 0
                || !Files.isRegularFile(pdfPath) || Files.size(pdfPath) == 0) {
            throw new StorageException("La generacion temporal esta incompleta");
        }
        validatePdfBytes(Files.readAllBytes(pdfPath));
    }

    private void validatePdfBytes(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new InvalidPdfContentException("El PDF de constancia es obligatorio");
        }
        if (pdfBytes.length < 5
                || pdfBytes[0] != '%'
                || pdfBytes[1] != 'P'
                || pdfBytes[2] != 'D'
                || pdfBytes[3] != 'F'
                || pdfBytes[4] != '-') {
            throw new InvalidPdfContentException("El contenido PDF de constancia no es valido");
        }
    }

    private String safePdfFileName(CertificateGenerationMetadata metadata) {
        validateMetadata(metadata);
        return storagePathSanitizer.sanitizeSegment(metadata.getPdfFile());
    }

    private void validateCourseKey(CertificateGenerationMetadata metadata) {
        String expectedKey = metadata.getTeacherCode() + "-" + metadata.getCourseCode()
                + "-" + metadata.getSection() + "-" + metadata.getSemester();
        if (!expectedKey.equals(metadata.getCertificateKey())) {
            throw new StorageException("La clave de constancia por curso no es coherente");
        }
    }

    private void validateSemesterKey(CertificateGenerationMetadata metadata) {
        String expectedKey = metadata.getTeacherCode() + "-" + metadata.getSemester();
        if (!expectedKey.equals(metadata.getCertificateKey())) {
            throw new StorageException("La clave de constancia semestral no es coherente");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new StorageException(message);
        }
    }

    private void logCorruptMetadata(Path metadataPath, Exception exception) {
        LOGGER.warn("Metadata de constancia corrupta ignorada: {}", relativeToRoot(metadataPath), exception);
    }

    private String relativeToRoot(Path path) {
        Path normalized = assertInsideRoot(path.normalize());
        return root.relativize(normalized).toString();
    }

    private void throwIfCorruptMetadataAppearsToReference(String generationId) {
        Path certificatesRoot = root.resolve(CERTIFICATES_DIR).normalize();
        if (!Files.exists(certificatesRoot)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(certificatesRoot)) {
            List<Path> matchingCorruptMetadata = paths
                    .filter(path -> METADATA_FILE.equals(path.getFileName().toString()))
                    .filter(path -> !path.toString().contains(".tmp-"))
                    .filter(path -> metadataFileContains(path, generationId))
                    .filter(path -> readMetadataSafely(path).isEmpty())
                    .collect(Collectors.toList());
            if (!matchingCorruptMetadata.isEmpty()) {
                throw new StorageException("La metadata de constancia esta corrupta");
            }
        } catch (IOException exception) {
            throw new StorageException("No se pudo leer el almacenamiento de constancias", exception);
        }
    }

    private boolean metadataFileContains(Path path, String value) {
        try {
            return Files.readString(path).contains(value);
        } catch (IOException exception) {
            logCorruptMetadata(path, exception);
            return false;
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
