package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Momento en que el grupo toma posesión del apartamento (check-in). */
public record Registro(LocalDateTime momento) {

    public Registro {
        if (momento == null) {
            throw new ReglaDominioException("El registro requiere fecha y hora.");
        }
    }

    public LocalDate fecha() {
        return momento.toLocalDate();
    }
}
