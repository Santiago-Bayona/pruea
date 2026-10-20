package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;

/** Unidad mínima de venta (3.1): la noche que inicia en la fecha dada. */
public record Noche(LocalDate fecha) {

    public Noche {
        if (fecha == null) {
            throw new ReglaDominioException("La noche requiere una fecha.");
        }
    }

    public Noche siguiente() {
        return new Noche(fecha.plusDays(1));
    }

    public boolean esAnteriorA(Noche otra) {
        return fecha.isBefore(otra.fecha);
    }
}
