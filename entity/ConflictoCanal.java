package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoConflictoCanal;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;

import java.time.LocalDateTime;
import java.util.Objects;

/** Reserva externa rechazada por colisionar con una reserva vigente (RN-18). Se registra para revisión del administrador. */
public class ConflictoCanal {

    private final CodigoConflictoCanal codigo;
    private final CanalOrigen canal;
    private final IdentificadorExterno identificadorExterno;
    private final IdentificacionApartamento apartamento;
    private final Estancia estanciaSolicitada;
    private final CodigoReserva reservaVigente;
    private final LocalDateTime fechaDeteccion;
    private boolean revisado;
    private String notaRevision;

    public ConflictoCanal(CodigoConflictoCanal codigo, CanalOrigen canal, IdentificadorExterno identificadorExterno,
                          IdentificacionApartamento apartamento, Estancia estanciaSolicitada,
                          CodigoReserva reservaVigente, LocalDateTime fechaDeteccion) {
        if (codigo == null || identificadorExterno == null || apartamento == null || estanciaSolicitada == null
                || reservaVigente == null || fechaDeteccion == null) {
            throw new ReglaDominioException("El conflicto de canal requiere todos sus datos de detección.");
        }
        if (canal == null || !canal.esExterno()) {
            throw new ReglaDominioException("Solo una reserva de canal externo puede generar un conflicto de canal.");
        }
        this.codigo = codigo;
        this.canal = canal;
        this.identificadorExterno = identificadorExterno;
        this.apartamento = apartamento;
        this.estanciaSolicitada = estanciaSolicitada;
        this.reservaVigente = reservaVigente;
        this.fechaDeteccion = fechaDeteccion;
    }

    public void marcarRevisado(String nota) {
        if (revisado) {
            throw new ReglaDominioException("El conflicto ya fue revisado.");
        }
        if (nota == null || nota.isBlank()) {
            throw new ReglaDominioException("La revisión del conflicto requiere una nota.");
        }
        this.revisado = true;
        this.notaRevision = nota.trim();
    }

    public CodigoConflictoCanal getCodigo() {
        return codigo;
    }

    public CanalOrigen getCanal() {
        return canal;
    }

    public IdentificadorExterno getIdentificadorExterno() {
        return identificadorExterno;
    }

    public IdentificacionApartamento getApartamento() {
        return apartamento;
    }

    public Estancia getEstanciaSolicitada() {
        return estanciaSolicitada;
    }

    public CodigoReserva getReservaVigente() {
        return reservaVigente;
    }

    public LocalDateTime getFechaDeteccion() {
        return fechaDeteccion;
    }

    public boolean estaRevisado() {
        return revisado;
    }

    public String getNotaRevision() {
        return notaRevision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConflictoCanal that)) return false;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
