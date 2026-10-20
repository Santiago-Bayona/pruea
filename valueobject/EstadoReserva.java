package co.edu.uniquindio.sga.domain.valueobject;

/** Ciclo de vida de la reserva (sección 8). NO_SHOW es un valor de este enum, no una clase aparte. */
public enum EstadoReserva {
    PENDIENTE(true), CONFIRMADA(true), EN_CURSO(true), FINALIZADA(false), CANCELADA(false), NO_SHOW(false);

    private final boolean activa;

    EstadoReserva(boolean activa) {
        this.activa = activa;
    }

    /** Activa = retiene disponibilidad (RN-01, RN-12). */
    public boolean esActiva() {
        return activa;
    }

    public boolean esTerminal() {
        return !activa;
    }

    /** RN-08: toda transición no listada se rechaza. */
    public boolean puedeTransitarA(EstadoReserva destino) {
        return switch (this) {
            case PENDIENTE -> destino == CONFIRMADA || destino == CANCELADA;
            case CONFIRMADA -> destino == EN_CURSO || destino == CANCELADA || destino == NO_SHOW;
            case EN_CURSO -> destino == FINALIZADA;
            case FINALIZADA, CANCELADA, NO_SHOW -> false;
        };
    }
}
