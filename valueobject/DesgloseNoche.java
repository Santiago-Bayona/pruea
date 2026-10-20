package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Línea de la cotización: fecha, temporada, tarifa aplicada, ocupantes facturables y subtotal (7.4). */
public record DesgloseNoche(Noche noche, IdTemporada temporada, Dinero tarifaAplicada,
                            int ocupantesFacturables, Dinero subtotal) {

    public DesgloseNoche {
        if (noche == null || temporada == null || tarifaAplicada == null || subtotal == null) {
            throw new ReglaDominioException("El desglose de la noche está incompleto.");
        }
        if (ocupantesFacturables < 0) {
            throw new ReglaDominioException("Los ocupantes facturables no pueden ser negativos.");
        }
    }

    public static DesgloseNoche calcular(Noche noche, Tarifa tarifa, int ocupantesFacturables) {
        return new DesgloseNoche(noche, tarifa.temporada(), tarifa.valorPorOcupante(),
                ocupantesFacturables, tarifa.costoNoche(ocupantesFacturables));
    }
}
