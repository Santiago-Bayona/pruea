package co.edu.uniquindio.sga.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Diferencia entre cargos y pagos del folio. Positivo: debe el huésped; negativo: saldo a favor (7.7). */
public record Saldo(BigDecimal valor) {

    public static final Saldo CERO = new Saldo(BigDecimal.ZERO);

    public Saldo {
        valor = (valor == null ? BigDecimal.ZERO : valor).setScale(0, RoundingMode.HALF_UP);
    }

    public static Saldo entre(Dinero cargos, Dinero pagos) {
        return new Saldo(cargos.valor().subtract(pagos.valor()));
    }

    public boolean esCero() {
        return valor.signum() == 0;
    }

    public boolean debeHuesped() {
        return valor.signum() > 0;
    }

    public boolean esAFavor() {
        return valor.signum() < 0;
    }
}
