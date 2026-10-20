package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CodigoRecordatorio;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoRecordatorio;
import co.edu.uniquindio.sga.domain.valueobject.TipoRecordatorio;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Raíz del agregado Recordatorio: aviso programado al titular antes de la entrada o de la salida (RN-23).
 * La unicidad por reserva y tipo (RN-25) y la cancelación en cadena (RN-24) las coordina GestionRecordatoriosService.
 */
public class Recordatorio {

    private final CodigoRecordatorio codigo;
    private final CodigoReserva reserva;
    private final TipoRecordatorio tipo;
    private LocalDateTime momentoEvento;
    private LocalDateTime fechaRecordatorio;
    private EstadoRecordatorio estado;
    private String ultimoError;

    private Recordatorio(CodigoRecordatorio codigo, CodigoReserva reserva, TipoRecordatorio tipo,
                         LocalDateTime momentoEvento, LocalDateTime fechaRecordatorio) {
        this.codigo = codigo;
        this.reserva = reserva;
        this.tipo = tipo;
        this.momentoEvento = momentoEvento;
        this.fechaRecordatorio = fechaRecordatorio;
        this.estado = EstadoRecordatorio.PROGRAMADO;
    }

    /** La fecha del recordatorio = momento del evento menos la antelación configurada. */
    public static Recordatorio programar(CodigoRecordatorio codigo, CodigoReserva reserva, TipoRecordatorio tipo,
                                         LocalDateTime momentoEvento, Duration antelacion) {
        if (codigo == null || reserva == null || tipo == null || momentoEvento == null || antelacion == null) {
            throw new ReglaDominioException("El recordatorio requiere código, reserva, tipo, evento y antelación.");
        }
        if (antelacion.isNegative() || antelacion.isZero()) {
            throw new ReglaDominioException("La antelación del recordatorio debe ser mayor que cero.");
        }
        return new Recordatorio(codigo, reserva, tipo, momentoEvento, momentoEvento.minus(antelacion));
    }

    /** Si cambia la fecha del evento (modificación de la reserva), el mismo recordatorio se reprograma. */
    public void reprogramar(LocalDateTime nuevoMomentoEvento, Duration antelacion) {
        if (estado != EstadoRecordatorio.PROGRAMADO) {
            throw new ReglaDominioException("Solo se reprograma un recordatorio PROGRAMADO.");
        }
        if (nuevoMomentoEvento == null || antelacion == null || antelacion.isNegative() || antelacion.isZero()) {
            throw new ReglaDominioException("La reprogramación requiere nuevo evento y antelación positiva.");
        }
        this.momentoEvento = nuevoMomentoEvento;
        this.fechaRecordatorio = nuevoMomentoEvento.minus(antelacion);
    }

    public void marcarEnviado() {
        transitarA(EstadoRecordatorio.ENVIADO);
        this.ultimoError = null;
    }

    /** Falla del correo: queda registrado el error y no afecta la reserva. */
    public void marcarFallido(String error) {
        if (error == null || error.isBlank()) {
            throw new ReglaDominioException("Debe registrarse el error del envío para su revisión.");
        }
        transitarA(EstadoRecordatorio.FALLIDO);
        this.ultimoError = error.trim();
    }

    /** RN-24: una reserva cancelada no conserva recordatorios pendientes. */
    public void cancelar() {
        transitarA(EstadoRecordatorio.CANCELADO);
    }

    public boolean estaPendiente() {
        return estado.estaPendiente();
    }

    public boolean debeEjecutarse(LocalDateTime ahora) {
        return estado == EstadoRecordatorio.PROGRAMADO && !ahora.isBefore(fechaRecordatorio);
    }

    /** Apoya RN-25: ¿este recordatorio ocupa el cupo del tipo para esa reserva? */
    public boolean ocupaCupo(CodigoReserva otraReserva, TipoRecordatorio otroTipo) {
        return reserva.equals(otraReserva) && tipo == otroTipo && estado != EstadoRecordatorio.CANCELADO;
    }

    private void transitarA(EstadoRecordatorio destino) {
        if (!estado.puedeTransitarA(destino)) {
            throw new ReglaDominioException("Transición inválida de recordatorio: " + estado + " → " + destino);
        }
        this.estado = destino;
    }

    public CodigoRecordatorio getCodigo() {
        return codigo;
    }

    public CodigoReserva getReserva() {
        return reserva;
    }

    public TipoRecordatorio getTipo() {
        return tipo;
    }

    public LocalDateTime getMomentoEvento() {
        return momentoEvento;
    }

    public LocalDateTime getFechaRecordatorio() {
        return fechaRecordatorio;
    }

    public EstadoRecordatorio getEstado() {
        return estado;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recordatorio that)) return false;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
