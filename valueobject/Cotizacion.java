package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.util.List;

/** Valor de una estancia con su desglose noche por noche. Es lo que la reserva congela (RN-22, 3.5). */
public record Cotizacion(List<DesgloseNoche> desglose) {

    public Cotizacion {
        if (desglose == null || desglose.isEmpty()) {
            throw new ReglaDominioException("La cotización requiere al menos una noche.");
        }
        desglose = List.copyOf(desglose);
    }

    public Dinero total() {
        return desglose.stream().map(DesgloseNoche::subtotal).reduce(Dinero.CERO, Dinero::sumar);
    }

    public int numeroNoches() {
        return desglose.size();
    }

    /** RN-14: diferencia (positiva o negativa) frente a una cotización anterior, para registrar como ajuste. */
    public BigDecimal diferenciaFrente(Cotizacion anterior) {
        return total().valor().subtract(anterior.total().valor());
    }
}
