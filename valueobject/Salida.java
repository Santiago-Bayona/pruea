package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/** Momento en que el apartamento se libera y queda pendiente de preparación (check-out). */
public record Salida(LocalDateTime momento) {

    public Salida {
        if (momento == null) {
            throw new ReglaDominioException("La salida requiere fecha y hora.");
        }
    }
}
