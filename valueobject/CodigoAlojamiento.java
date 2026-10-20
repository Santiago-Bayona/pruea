package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Objeto de valor: El código del alojamiento. Igualdad por valor, inmutable. */
public record CodigoAlojamiento(String valor) {

    public CodigoAlojamiento {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El código del alojamiento es obligatorio.");
        }
        valor = valor.trim();
    }
}
