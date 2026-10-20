package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Autorización explícita del administrador para cerrar un folio con saldo distinto de cero (RN-17). */
public record AutorizacionCierre(String autor, String motivo) {

    public AutorizacionCierre {
        if (autor == null || autor.isBlank() || motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("La autorización de cierre requiere autor y motivo.");
        }
        autor = autor.trim();
        motivo = motivo.trim();
    }
}
