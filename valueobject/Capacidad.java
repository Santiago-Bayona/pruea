package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Número máximo de personas de un apartamento: tope rígido (F-03, RN-02). Si cambia, se reemplaza por otro valor. */
public record Capacidad(int valor) {

    public Capacidad {
        if (valor < 1) {
            throw new ReglaDominioException("La capacidad debe admitir al menos una persona.");
        }
    }

    /** Todos los ocupantes cuentan, sean facturables o no (3.2). */
    public boolean admite(int totalOcupantes) {
        return totalOcupantes <= valor;
    }

    public boolean esMenorQue(Capacidad otra) {
        return valor < otra.valor;
    }
}
