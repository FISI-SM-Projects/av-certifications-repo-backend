package pe.edu.unmsm.fisi.gestiondocente.constancia.entity;

public enum CertificationStatus {
    GENERADA,
    FIRMADA,

    // Compatibilidad temporal para datos historicos previos a reglas v2.
    EMITIDO,
    VERIFICADO,
    EN_REVISION,
    NO_EMITIDO,
    REVOCADO;

    public boolean isGenerated() {
        return this == GENERADA || this == EMITIDO;
    }

    public boolean isSigned() {
        return this == FIRMADA || this == VERIFICADO;
    }

    public CertificationStatus official() {
        if (isSigned()) return FIRMADA;
        if (isGenerated()) return GENERADA;
        return this;
    }
}
