package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Rango continuo de noches [entrada, salida) (3.1). Se reemplaza, nunca se modifica. */
public record Estancia(LocalDate entrada, LocalDate salida) {

    public Estancia {
        if (entrada == null || salida == null) {
            throw new ReglaDominioException("La estancia requiere fecha de entrada y de salida.");
        }
        // RN-03: toda estancia tiene al menos una noche
        if (!salida.isAfter(entrada)) {
            throw new ReglaDominioException("La fecha de salida debe ser posterior a la de entrada.");
        }
    }

    public int numeroNoches() {
        return (int) ChronoUnit.DAYS.between(entrada, salida);
    }

    public List<Noche> noches() {
        return entrada.datesUntil(salida).map(Noche::new).toList();
    }

    public boolean contiene(Noche noche) {
        return !noche.fecha().isBefore(entrada) && noche.fecha().isBefore(salida);
    }

    /** 3.1: se solapan si entrada_A < salida_B y entrada_B < salida_A. */
    public boolean seSolapaCon(Estancia otra) {
        return entrada.isBefore(otra.salida) && otra.entrada.isBefore(salida);
    }

    /** RN-04: ayuda a validar que la entrada no sea anterior a hoy. */
    public boolean iniciaAntesDe(LocalDate fecha) {
        return entrada.isBefore(fecha);
    }
}
