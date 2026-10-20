package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Objeto de valor: El motivo del bloqueo. */
public record MotivoBloqueo(String descripcion) {

    public MotivoBloqueo {
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaDominioException("El motivo del bloqueo es obligatorio.");
        }
        descripcion = descripcion.trim();
    }
}
