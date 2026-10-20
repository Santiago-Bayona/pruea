package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.Duration;
import java.time.LocalTime;

/** Tiempo requerido entre una salida y la siguiente entrada (RN-20, 3.3). */
public record TiempoPreparacion(Duration valor) {

    public TiempoPreparacion {
        if (valor == null || valor.isNegative()) {
            throw new ReglaDominioException("El tiempo de preparación no puede ser nulo ni negativo.");
        }
    }

    /** 3.3: si excede la ventana salida→entrada del mismo día, no hay rotación el mismo día. */
    public boolean cabeEntre(LocalTime horaSalida, LocalTime horaEntrada) {
        return !Duration.between(horaSalida, horaEntrada).minus(valor).isNegative();
    }
}
