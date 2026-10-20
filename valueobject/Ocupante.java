package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.Period;

/** Persona incluida en una reserva. Se guarda la fecha de nacimiento; la edad se calcula, nunca se almacena (3.2). */
public record Ocupante(String nombre, LocalDate fechaNacimiento) {

    public Ocupante {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El ocupante debe tener nombre.");
        }
        if (fechaNacimiento == null) {
            throw new ReglaDominioException("El ocupante debe tener fecha de nacimiento.");
        }
        nombre = nombre.trim();
    }

    public int edadEn(LocalDate fecha) {
        if (fecha.isBefore(fechaNacimiento)) {
            throw new ReglaDominioException("La fecha de referencia es anterior al nacimiento del ocupante.");
        }
        return Period.between(fechaNacimiento, fecha).getYears();
    }

    /** RN-06: facturable si a la fecha de entrada alcanza el umbral; no cambia a mitad de la estancia. */
    public boolean esFacturableEn(LocalDate fechaEntrada, UmbralEdadFacturable umbral) {
        return umbral.alcanza(edadEn(fechaEntrada));
    }
}
