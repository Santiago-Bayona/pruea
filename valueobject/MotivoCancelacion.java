package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Objeto de valor: El motivo de cancelación. */
public record MotivoCancelacion(String descripcion) {

    public MotivoCancelacion {
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaDominioException("El motivo de cancelación es obligatorio.");
        }
        descripcion = descripcion.trim();
    }
}
