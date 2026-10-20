package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;

/** Concepto que suma al folio. Inmutable: se corrige con un movimiento inverso (RN-16). */
public record Cargo(TipoCargo tipo, String concepto, Dinero monto, LocalDate fecha, boolean inverso) {

    public Cargo {
        if (tipo == null || fecha == null || monto == null) {
            throw new ReglaDominioException("El cargo requiere tipo, monto y fecha.");
        }
        if (concepto == null || concepto.isBlank()) {
            throw new ReglaDominioException("El cargo requiere un concepto.");
        }
        if (monto.esCero()) {
            throw new ReglaDominioException("El monto del cargo debe ser mayor que cero.");
        }
        concepto = concepto.trim();
    }

    public static Cargo nuevo(TipoCargo tipo, String concepto, Dinero monto, LocalDate fecha) {
        return new Cargo(tipo, concepto, monto, fecha, false);
    }

    public Cargo invertido(LocalDate fechaCorreccion) {
        return new Cargo(tipo, concepto, monto, fechaCorreccion, true);
    }

    public boolean esInversoDe(Cargo otro) {
        return inverso && !otro.inverso && tipo == otro.tipo
                && concepto.equals(otro.concepto) && monto.equals(otro.monto);
    }
}
