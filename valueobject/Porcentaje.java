package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Porcentaje entero 0..100 (retenciones, anticipo). */
public record Porcentaje(int valor) {

    public static final Porcentaje CERO = new Porcentaje(0);
    public static final Porcentaje CIEN = new Porcentaje(100);

    public Porcentaje {
        if (valor < 0 || valor > 100) {
            throw new ReglaDominioException("El porcentaje debe estar entre 0 y 100.");
        }
    }

    public Dinero aplicarA(Dinero base) {
        return new Dinero(base.valor()
                .multiply(BigDecimal.valueOf(valor))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP));
    }

    public Porcentaje complemento() {
        return new Porcentaje(100 - valor);
    }

    public boolean esCero() {
        return valor == 0;
    }
}
