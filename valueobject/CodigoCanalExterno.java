package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Objeto de valor: El código del canal externo. Igualdad por valor, inmutable. */
public record CodigoCanalExterno(String valor) {

    public CodigoCanalExterno {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El código del canal externo es obligatorio.");
        }
        valor = valor.trim();
    }
}
