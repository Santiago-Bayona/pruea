package co.edu.uniquindio.sga.domain.valueobject;

/** Origen de la reserva: portal, directo o externo. */
public enum CanalOrigen {
    PORTAL, DIRECTO, EXTERNO;

    public boolean esExterno() {
        return this == EXTERNO;
    }
}
