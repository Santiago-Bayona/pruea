package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Objeto de valor: El código de la política de cancelación. Igualdad por valor, inmutable. */
public record CodigoPolitica(String valor) {

    public CodigoPolitica {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El código de la política de cancelación es obligatorio.");
        }
        valor = valor.trim();
    }
}
