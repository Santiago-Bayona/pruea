package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.EnumSet;
import java.util.Set;

/** Resultado del cálculo de disponibilidad de un apartamento para un rango (3.3). Vacío de motivos = disponible. */
public record Disponibilidad(Set<MotivoNoDisponibilidad> motivos) {

    public static final Disponibilidad LIBRE = new Disponibilidad(Set.of());

    public Disponibilidad {
        motivos = (motivos == null || motivos.isEmpty()) ? Set.of() : Set.copyOf(EnumSet.copyOf(motivos));
    }

    public static Disponibilidad noDisponible(MotivoNoDisponibilidad primero, MotivoNoDisponibilidad... resto) {
        EnumSet<MotivoNoDisponibilidad> todos = EnumSet.of(primero, resto);
        return new Disponibilidad(todos);
    }

    public boolean estaDisponible() {
        return motivos.isEmpty();
    }

    public Disponibilidad combinarCon(Disponibilidad otra) {
        if (otra.estaDisponible()) {
            return this;
        }
        EnumSet<MotivoNoDisponibilidad> union = motivos.isEmpty()
                ? EnumSet.copyOf(otra.motivos) : EnumSet.copyOf(motivos);
        union.addAll(otra.motivos);
        return new Disponibilidad(union);
    }

    public void exigirDisponible() {
        if (!estaDisponible()) {
            throw new ReglaDominioException("El apartamento no está disponible: " + motivos);
        }
    }
}
