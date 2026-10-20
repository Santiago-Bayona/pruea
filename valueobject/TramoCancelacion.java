package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Tramo de la política: desde cierta antelación mínima (días) aplica un porcentaje de retención del anticipo. */
public record TramoCancelacion(int diasAntelacionMinima, Porcentaje retencion) {

    public TramoCancelacion {
        if (diasAntelacionMinima < 0 || retencion == null) {
            throw new ReglaDominioException("El tramo requiere antelación no negativa y retención.");
        }
    }

    public boolean aplicaA(long diasAntelacion) {
        return diasAntelacion >= diasAntelacionMinima;
    }
}
