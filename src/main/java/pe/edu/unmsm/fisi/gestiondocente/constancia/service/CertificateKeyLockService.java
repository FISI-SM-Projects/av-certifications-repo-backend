package pe.edu.unmsm.fisi.gestiondocente.constancia.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

@Component
public class CertificateKeyLockService {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public <T> T executeLocked(String certificateKey, Supplier<T> operation) {
        ReentrantLock lock = locks.computeIfAbsent(certificateKey, ignored -> new ReentrantLock());
        lock.lock();

        try {
            return operation.get();
        } finally {
            lock.unlock();
            // This is a local, single-JVM lock; future multi-instance deployments need distributed coordination.
            if (!lock.hasQueuedThreads()) {
                locks.remove(certificateKey, lock);
            }
        }
    }
}
