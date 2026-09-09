package pe.edu.unmsm.fisi.gestiondocente.auth.entity;

public record AuthenticatedAccount(String ldapUid, Long accountId) implements java.security.Principal {
    @Override public String getName() { return ldapUid; }
}
