package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Objeto de valor: El código del conflicto de canal. Igualdad por valor, inmutable. */
public record CodigoConflictoCanal(String valor) {

    public CodigoConflictoCanal {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El código del conflicto de canal es obligatorio.");
        }
        valor = valor.trim();
    }
}
