package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/** Tarifa con el momento desde el que rige: una línea del histórico de tarifas (7.4). */
public record RegistroTarifa(Tarifa tarifa, LocalDateTime vigenteDesde) {

    public RegistroTarifa {
        if (tarifa == null || vigenteDesde == null) {
            throw new ReglaDominioException("El registro de tarifa requiere tarifa y fecha de vigencia.");
        }
    }
}
