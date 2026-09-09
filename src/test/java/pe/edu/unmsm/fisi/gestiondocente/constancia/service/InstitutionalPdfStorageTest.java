package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.web.server.ResponseStatusException;

class InstitutionalPdfStorageTest {
    @TempDir Path root;
    @Test void invalidPdfDoesNotLeaveFiles() throws Exception {
        var storage = new InstitutionalPdfStorage(root.toString());
        for (byte[] invalid : new byte[][] { null, new byte[0], "invalid".getBytes() }) {
            assertThrows(ResponseStatusException.class, () -> storage.write(storage.newDocumentPath(), invalid));
        }
        try (var files = Files.list(root)) { assertEquals(0, files.count()); }
    }
    @Test void writesAndReadsOnlyWithinRoot() {
        var storage = new InstitutionalPdfStorage(root.toString());
        String path = storage.newDocumentPath(); byte[] pdf = "%PDF-1.7 test".getBytes();
        storage.write(path, pdf); assertArrayEquals(pdf, storage.read(path));
        assertTrue(storage.available(path));
        assertThrows(ResponseStatusException.class, () -> storage.read("../outside.pdf"));
        assertFalse(storage.available("../outside.pdf"));
    }
    @Test void noOverwriteAndNoPendingFiles() throws Exception {
        var storage = new InstitutionalPdfStorage(root.toString());
        String path = storage.newDocumentPath(); byte[] pdf = "%PDF-original".getBytes();
        storage.write(path, pdf);
        assertThrows(ResponseStatusException.class, () -> storage.write(path, "%PDF-replacement".getBytes()));
        assertArrayEquals(pdf, storage.read(path));
        try (var files = Files.walk(root)) { assertFalse(files.anyMatch(p -> p.getFileName().toString().startsWith(".pending-"))); }
    }
    @Test void rollbackRemovesOnlyNewDocument() {
        var storage = new InstitutionalPdfStorage(root.toString());
        String keep = storage.newDocumentPath(), discard = storage.newDocumentPath();
        storage.write(keep, "%PDF-keep".getBytes()); storage.write(discard, "%PDF-discard".getBytes());
        storage.removeUncommitted(discard);
        assertTrue(storage.available(keep)); assertFalse(storage.available(discard));
    }
}
