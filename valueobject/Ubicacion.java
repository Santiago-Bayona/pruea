package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;

/** Ciudad, dirección y coordenadas del alojamiento. Se reemplaza completa si cambia. */
public record Ubicacion(String ciudad, String direccion, BigDecimal latitud, BigDecimal longitud) {

    public Ubicacion {
        if (ciudad == null || ciudad.isBlank() || direccion == null || direccion.isBlank()) {
            throw new ReglaDominioException("La ubicación requiere ciudad y dirección.");
        }
        if (latitud == null || longitud == null
                || latitud.abs().compareTo(BigDecimal.valueOf(90)) > 0
                || longitud.abs().compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new ReglaDominioException("Latitud (±90) y longitud (±180) válidas son obligatorias.");
        }
        ciudad = ciudad.trim();
        direccion = direccion.trim();
    }
}
