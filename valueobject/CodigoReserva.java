package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Objeto de valor: El código de la reserva. Igualdad por valor, inmutable. */
public record CodigoReserva(String valor) {

    public CodigoReserva {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El código de la reserva es obligatorio.");
        }
        valor = valor.trim();
    }
}
