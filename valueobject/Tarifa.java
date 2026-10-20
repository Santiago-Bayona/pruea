package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Valor por ocupante facturable, por noche, de un apartamento en una temporada (7.4). Se reemplaza al cambiar. */
public record Tarifa(IdTemporada temporada, Dinero valorPorOcupante) {

    public Tarifa {
        if (temporada == null) {
            throw new ReglaDominioException("La tarifa debe pertenecer a una temporada.");
        }
        if (valorPorOcupante == null || valorPorOcupante.esCero()) {
            throw new ReglaDominioException("La tarifa debe tener un valor mayor que cero.");
        }
    }

    /** RN-05: subtotal de una noche. */
    public Dinero costoNoche(int ocupantesFacturables) {
        return valorPorOcupante.multiplicarPor(ocupantesFacturables);
    }
}
