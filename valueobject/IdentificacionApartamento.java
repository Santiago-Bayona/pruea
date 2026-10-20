package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.Locale;

/** Objeto de valor: La identificación del apartamento. Igualdad por valor, inmutable. */
public record IdentificacionApartamento(String valor) {

    public IdentificacionApartamento {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("La identificación del apartamento es obligatorio.");
        }
        valor = valor.trim().toUpperCase(Locale.ROOT);
    }
}
