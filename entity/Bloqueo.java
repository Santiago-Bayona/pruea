package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CodigoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Noche;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Raíz del agregado Bloqueo: decisión administrativa que impide vender un apartamento en un rango (RN-07).
 * Nunca se elimina físicamente: se levanta y queda en el historial.
 * Que el rango no tenga reservas activas (7.3) lo verifica el servicio, que consulta reservas.
 */
public class Bloqueo {

    private final CodigoBloqueo codigo;
    private final IdentificacionApartamento apartamento;
    private final LocalDateTime fechaRegistro;
    private Estancia rango;
    private MotivoBloqueo motivo;
    private boolean vigente;

    public Bloqueo(CodigoBloqueo codigo, IdentificacionApartamento apartamento, Estancia rango,
                   MotivoBloqueo motivo, LocalDateTime fechaRegistro) {
        if (codigo == null || apartamento == null || rango == null || motivo == null || fechaRegistro == null) {
            throw new ReglaDominioException("El bloqueo requiere código, apartamento, rango de fechas, motivo y fecha de registro.");
        }
        this.codigo = codigo;
        this.apartamento = apartamento;
        this.rango = rango;
        this.motivo = motivo;
        this.fechaRegistro = fechaRegistro;
        this.vigente = true;
    }

    /** RN-07: un bloqueo vigente sobre una noche la deja no disponible. */
    public boolean bloqueaNoche(Noche noche) {
        return vigente && rango.contiene(noche);
    }

    public boolean seSolapaCon(Estancia estancia) {
        return vigente && rango.seSolapaCon(estancia);
    }

    public void actualizarMotivo(MotivoBloqueo nuevoMotivo) {
        exigirVigente();
        if (nuevoMotivo == null) {
            throw new ReglaDominioException("El motivo del bloqueo es obligatorio.");
        }
        this.motivo = nuevoMotivo;
    }

    /** Reprogramar el rango no cambia la identidad del bloqueo; el servicio revalida contra reservas activas. */
    public void reprogramar(Estancia nuevoRango) {
        exigirVigente();
        if (nuevoRango == null) {
            throw new ReglaDominioException("El nuevo rango del bloqueo es obligatorio.");
        }
        this.rango = nuevoRango;
    }

    public void levantar() {
        exigirVigente();
        this.vigente = false;
    }

    private void exigirVigente() {
        if (!vigente) {
            throw new ReglaDominioException("El bloqueo ya fue levantado.");
        }
    }

    public CodigoBloqueo getCodigo() {
        return codigo;
    }

    public IdentificacionApartamento getApartamento() {
        return apartamento;
    }

    public Estancia getRango() {
        return rango;
    }

    public MotivoBloqueo getMotivo() {
        return motivo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public boolean estaVigente() {
        return vigente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bloqueo that)) return false;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
