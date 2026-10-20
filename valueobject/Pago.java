package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;

/** Abono registrado contra un folio, con medio y fecha (RN-15). Inmutable (RN-16). */
public record Pago(Dinero monto, MedioPago medio, LocalDate fecha, boolean inverso) {

    public Pago {
        if (monto == null || medio == null || fecha == null) {
            throw new ReglaDominioException("Todo pago requiere monto, medio y fecha.");
        }
        if (monto.esCero()) {
            throw new ReglaDominioException("El monto del pago debe ser mayor que cero.");
        }
    }

    public static Pago nuevo(Dinero monto, MedioPago medio, LocalDate fecha) {
        return new Pago(monto, medio, fecha, false);
    }

    public Pago invertido(LocalDate fechaCorreccion) {
        return new Pago(monto, medio, fechaCorreccion, true);
    }

    public boolean esInversoDe(Pago otro) {
        return inverso && !otro.inverso && medio == otro.medio && monto.equals(otro.monto);
    }
}
