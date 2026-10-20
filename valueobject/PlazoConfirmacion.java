package co.edu.uniquindio.sga.domain.valueobject;

import java.time.LocalDateTime;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Plazo, en horas, para confirmar una reserva PENDIENTE (L-14, RN-21).
 * Es configuración del alojamiento, no una constante del código.
 */
public record PlazoConfirmacion(int horas) {
    public PlazoConfirmacion {
        if (horas < 1) {
            throw new ReglaDominioException("El plazo de confirmación debe ser de al menos una hora");
        }
    }

    public boolean venceAntesDe(LocalDateTime creacion, LocalDateTime momento) {
        return momento.isAfter(creacion.plusHours(horas));
    }
}