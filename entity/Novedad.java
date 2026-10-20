package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CodigoNovedad;
import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.time.LocalDateTime;
import java.util.Objects;

/** Raíz del agregado Novedad: daño, faltante o situación en un apartamento (7.6). Nunca se elimina; se resuelve. */
public class Novedad {

    private final CodigoNovedad codigo;
    private final IdentificacionApartamento apartamento;
    private final LocalDateTime fecha;
    private final String autor;
    private String descripcion;
    private GravedadNovedad gravedad;
    private boolean resuelta;
    private LocalDateTime fechaResolucion;

    public Novedad(CodigoNovedad codigo, IdentificacionApartamento apartamento, LocalDateTime fecha, String autor,
                   String descripcion, GravedadNovedad gravedad) {
        if (codigo == null || apartamento == null || fecha == null || gravedad == null) {
            throw new ReglaDominioException("La novedad requiere código, apartamento, fecha y gravedad.");
        }
        if (autor == null || autor.isBlank() || descripcion == null || descripcion.isBlank()) {
            throw new ReglaDominioException("La novedad requiere autor y descripción.");
        }
        this.codigo = codigo;
        this.apartamento = apartamento;
        this.fecha = fecha;
        this.autor = autor.trim();
        this.descripcion = descripcion.trim();
        this.gravedad = gravedad;
    }

    public void reclasificar(GravedadNovedad nuevaGravedad) {
        exigirPendiente();
        if (nuevaGravedad == null) {
            throw new ReglaDominioException("La gravedad es obligatoria.");
        }
        this.gravedad = nuevaGravedad;
    }

    public void precisarDescripcion(String nuevaDescripcion) {
        exigirPendiente();
        if (nuevaDescripcion == null || nuevaDescripcion.isBlank()) {
            throw new ReglaDominioException("La descripción es obligatoria.");
        }
        this.descripcion = nuevaDescripcion.trim();
    }

    public void resolver(LocalDateTime momento) {
        exigirPendiente();
        if (momento == null || momento.isBefore(fecha)) {
            throw new ReglaDominioException("La resolución no puede ser anterior al reporte de la novedad.");
        }
        this.resuelta = true;
        this.fechaResolucion = momento;
    }

    private void exigirPendiente() {
        if (resuelta) {
            throw new ReglaDominioException("La novedad ya está resuelta y forma parte del historial.");
        }
    }

    public CodigoNovedad getCodigo() {
        return codigo;
    }

    public IdentificacionApartamento getApartamento() {
        return apartamento;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getAutor() {
        return autor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public GravedadNovedad getGravedad() {
        return gravedad;
    }

    public boolean estaResuelta() {
        return resuelta;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Novedad that)) return false;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
