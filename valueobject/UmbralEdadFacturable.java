package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Edad mínima (en años) desde la cual un ocupante genera cargo. Configuración del alojamiento. */
public record UmbralEdadFacturable(int anios) {

    public UmbralEdadFacturable {
        if (anios < 0) {
            throw new ReglaDominioException("El umbral de edad facturable no puede ser negativo.");
        }
    }

    public boolean alcanza(int edad) {
        return edad >= anios;
    }
}
