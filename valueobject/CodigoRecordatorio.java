package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Objeto de valor: El código del recordatorio. Igualdad por valor, inmutable. */
public record CodigoRecordatorio(String valor) {

    public CodigoRecordatorio {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El código del recordatorio es obligatorio.");
        }
        valor = valor.trim();
    }
}
