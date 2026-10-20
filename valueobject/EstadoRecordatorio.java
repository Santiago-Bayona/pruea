package co.edu.uniquindio.sga.domain.valueobject;

public enum EstadoRecordatorio {
    PROGRAMADO, ENVIADO, FALLIDO, CANCELADO;

    /** Pendiente = aún puede ejecutarse (RN-24 exige que una reserva cancelada no conserve ninguno). */
    public boolean estaPendiente() {
        return this == PROGRAMADO || this == FALLIDO;
    }

    public boolean puedeTransitarA(EstadoRecordatorio destino) {
        return switch (this) {
            case PROGRAMADO -> destino == ENVIADO || destino == FALLIDO || destino == CANCELADO;
            case FALLIDO -> destino == ENVIADO || destino == FALLIDO || destino == CANCELADO;
            case ENVIADO, CANCELADO -> false;
        };
    }
}
