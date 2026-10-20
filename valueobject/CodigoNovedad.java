package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Objeto de valor: El código de la novedad. Igualdad por valor, inmutable. */
public record CodigoNovedad(String valor) {

    public CodigoNovedad {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El código de la novedad es obligatorio.");
        }
        valor = valor.trim();
    }
}
