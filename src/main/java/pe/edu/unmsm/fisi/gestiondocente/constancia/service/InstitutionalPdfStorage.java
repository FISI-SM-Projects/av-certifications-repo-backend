package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import java.io.IOException;
import java.nio.file.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Profile("!test")
public class InstitutionalPdfStorage {
    private final Path root;
    public InstitutionalPdfStorage(@Value("${app.storage.root:storage}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }
    public String newDocumentPath() { return "certificates/institutional/" + java.util.UUID.randomUUID() + ".pdf"; }
    public void write(String documentPath, byte[] pdf) {
        if (pdf == null || pdf.length < 5 || !new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-")) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El generador no produjo un PDF valido");
        }
        Path temp = null;
        try {
            Path target = resolve(documentPath);
            Files.createDirectories(target.getParent());
            ensureContained(target.getParent());
            temp = Files.createTempFile(target.getParent(), ".pending-", ".tmp");
            Files.write(temp, pdf);
            if (Files.exists(target)) throw new FileAlreadyExistsException(target.toString());
            try { Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) {
                // El movimiento normal conserva la publicacion al final, sin reemplazar archivos.
                Files.move(temp, target);
            }
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar el PDF", ex);
        } finally {
            if (temp != null) removeTemporary(temp);
        }
    }
    private Path resolve(String stored) {
        if (stored == null || stored.isBlank()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PDF no disponible");
        Path candidate = Path.of(stored);
        Path target = (candidate.isAbsolute() ? candidate : root.resolve(candidate)).normalize();
        if (!target.startsWith(root)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PDF no disponible en el almacenamiento configurado");
        return target;
    }
    private void ensureContained(Path path) throws IOException {
        if (!path.toRealPath().startsWith(root.toRealPath())) throw new IOException("Ruta fuera de almacenamiento");
    }
    public boolean available(String stored) {
        try { Path path = resolve(stored); ensureContained(path); return Files.isRegularFile(path); }
        catch (IOException | RuntimeException ex) { return false; }
    }
    public byte[] read(String stored) {
        if (!available(stored)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El documento PDF no esta disponible");
        try { return Files.readAllBytes(resolve(stored)); }
        catch (IOException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se pudo leer el PDF", ex); }
    }
    public void removeUncommitted(String stored) { removeTemporary(resolve(stored)); }
    private void removeTemporary(Path path) {
        try { Files.deleteIfExists(path); }
        catch (IOException ex) { org.slf4j.LoggerFactory.getLogger(getClass()).warn("No se pudo limpiar archivo temporal institucional", ex); }
    }
}
