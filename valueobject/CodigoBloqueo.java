package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Objeto de valor: El código del bloqueo. Igualdad por valor, inmutable. */
public record CodigoBloqueo(String valor) {

    public CodigoBloqueo {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El código del bloqueo es obligatorio.");
        }
        valor = valor.trim();
    }
}
