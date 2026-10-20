package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Objeto de valor: El identificador externo del canal. Igualdad por valor, inmutable. */
public record IdentificadorExterno(String valor) {

    public IdentificadorExterno {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El identificador externo del canal es obligatorio.");
        }
        valor = valor.trim();
    }
}
