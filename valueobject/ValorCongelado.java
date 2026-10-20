package co.edu.uniquindio.sga.domain.valueobject;

import java.util.List;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Valor de una estancia con su desglose, congelado al crear la reserva (RN-22, 3.5).
 * El redondeo se aplica AL FINAL del cálculo, nunca noche por noche (3.4).
 */
public record ValorCongelado(List<CargoNoche> detalle) {
    public ValorCongelado {
        if (detalle == null || detalle.isEmpty()) {
            throw new ReglaDominioException("El valor de la reserva debe tener desglose por noche");
        }
        detalle = List.copyOf(detalle); // copia inmutable: el desglose no se altera después
    }

    public Dinero total() {
        return detalle.stream()
                .map(CargoNoche::subtotal)
                .reduce(Dinero.CERO, Dinero::sumar);
    }

    public int noches() {
        return detalle.size();
    }
}