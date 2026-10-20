package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Parámetros configurables del alojamiento (Ficha A.4, 7.3). Nada de esto se quema en el código. */
public record ConfiguracionAlojamiento(LocalTime horaEntrada, LocalTime horaSalida,
                                       TiempoPreparacion tiempoPreparacion, PlazoConfirmacion plazoConfirmacion,
                                       LocalTime horaLimiteNoShow, UmbralEdadFacturable umbralEdadFacturable,
                                       Porcentaje porcentajeAnticipo, Duration antelacionRecordatorioEntrada,
                                       Duration antelacionRecordatorioSalida) {

    public ConfiguracionAlojamiento {
        if (horaEntrada == null || horaSalida == null || tiempoPreparacion == null || plazoConfirmacion == null
                || horaLimiteNoShow == null || umbralEdadFacturable == null || porcentajeAnticipo == null
                || antelacionRecordatorioEntrada == null || antelacionRecordatorioSalida == null) {
            throw new ReglaDominioException("La configuración del alojamiento está incompleta.");
        }
        if (antelacionRecordatorioEntrada.isNegative() || antelacionRecordatorioEntrada.isZero()
                || antelacionRecordatorioSalida.isNegative() || antelacionRecordatorioSalida.isZero()) {
            throw new ReglaDominioException("La antelación de los recordatorios debe ser mayor que cero.");
        }
        // Coherencia: el no-show no puede declararse antes de que el grupo pueda registrarse
        if (!horaLimiteNoShow.isAfter(horaEntrada)) {
            throw new ReglaDominioException("La hora límite de no-show debe ser posterior a la hora de entrada.");
        }
    }

    public boolean exigeAnticipo() {
        return !porcentajeAnticipo.esCero();
    }

    public Dinero anticipoSobre(Dinero valorReserva) {
        return porcentajeAnticipo.aplicarA(valorReserva);
    }

    public boolean permiteEntradaElMismoDiaDeUnaSalida() {
        return tiempoPreparacion.cabeEntre(horaSalida, horaEntrada);
    }

    public LocalDateTime limiteNoShow(LocalDate fechaEntrada) {
        return fechaEntrada.atTime(horaLimiteNoShow);
    }
}
