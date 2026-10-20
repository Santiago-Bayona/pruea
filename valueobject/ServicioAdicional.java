package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Servicio adicional del alojamiento, indicando si genera cargo y su valor (7.3). */
public record ServicioAdicional(String nombre, Dinero valor, boolean generaCargo) {

    public ServicioAdicional {
        if (nombre == null || nombre.isBlank() || valor == null) {
            throw new ReglaDominioException("El servicio adicional requiere nombre y valor.");
        }
        if (generaCargo && valor.esCero()) {
            throw new ReglaDominioException("Un servicio que genera cargo debe tener valor mayor que cero.");
        }
        nombre = nombre.trim();
    }

    public Dinero costo() {
        return generaCargo ? valor : Dinero.CERO;
    }
}
