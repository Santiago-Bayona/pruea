package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Objeto de valor: El código del titular. Igualdad por valor, inmutable. */
public record CodigoTitular(String valor) {

    public CodigoTitular {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El código del titular es obligatorio.");
        }
        valor = valor.trim();
    }
}
