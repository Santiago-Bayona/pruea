package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.MonthDay;

/** Rango de fechas que se repite cada año, puede cruzar de año (ej. 1 dic - 11 ene). Ambos extremos incluidos. */
public record PeriodoAnual(MonthDay inicio, MonthDay fin) {

    public PeriodoAnual {
        if (inicio == null || fin == null) {
            throw new ReglaDominioException("El periodo requiere día y mes de inicio y de fin.");
        }
    }

    public boolean cruzaAnio() {
        return inicio.isAfter(fin);
    }

    public boolean contiene(MonthDay diaMes) {
        if (cruzaAnio()) {
            return !diaMes.isBefore(inicio) || !diaMes.isAfter(fin);
        }
        return !diaMes.isBefore(inicio) && !diaMes.isAfter(fin);
    }

    public boolean contiene(LocalDate fecha) {
        return contiene(MonthDay.from(fecha));
    }

    /** 7.4: las temporadas no pueden solaparse. Se evalúa sobre un año bisiesto de referencia. */
    public boolean seSolapaCon(PeriodoAnual otro) {
        return LocalDate.of(2000, 1, 1).datesUntil(LocalDate.of(2001, 1, 1))
                .anyMatch(dia -> contiene(dia) && otro.contiene(dia));
    }
}
