package co.edu.uniquindio.sga.domain.valueobject;

/** Condición física presente del apartamento (3.3). Valor, no entidad. */
public enum EstadoOperativo {
    PREPARADO, OCUPADO, PENDIENTE_PREPARACION, EN_PREPARACION, FUERA_DE_SERVICIO;

    /** RN-11: solo un apartamento PREPARADO puede recibir un grupo. */
    public boolean permiteRegistro() {
        return this == PREPARADO;
    }
}
