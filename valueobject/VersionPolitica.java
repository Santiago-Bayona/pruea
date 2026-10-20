package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Número de versión de la política de cancelación (RN-13). */
public record VersionPolitica(int numero) {

    public VersionPolitica {
        if (numero < 1) {
            throw new ReglaDominioException("La versión de la política debe ser mayor o igual a 1.");
        }
    }

    public VersionPolitica siguiente() {
        return new VersionPolitica(numero + 1);
    }
}
