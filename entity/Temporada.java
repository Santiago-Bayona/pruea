package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.PeriodoAnual;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Periodo del calendario con tarifas propias (7.4). La temporada base cubre todo lo no asignado y no tiene rangos. */
public class Temporada {

    private final IdTemporada id;
    private final boolean base;
    private String nombre;
    private List<PeriodoAnual> periodos;

    private Temporada(IdTemporada id, String nombre, boolean base, List<PeriodoAnual> periodos) {
        if (id == null) {
            throw new ReglaDominioException("La temporada debe tener un identificador.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("La temporada debe tener nombre.");
        }
        this.id = id;
        this.base = base;
        this.nombre = nombre.trim();
        this.periodos = base ? List.of() : validarPeriodos(periodos);
    }

    public static Temporada base(IdTemporada id, String nombre) {
        return new Temporada(id, nombre, true, List.of());
    }

    public static Temporada especial(IdTemporada id, String nombre, List<PeriodoAnual> periodos) {
        return new Temporada(id, nombre, false, periodos);
    }

    public void renombrar(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new ReglaDominioException("La temporada debe tener nombre.");
        }
        this.nombre = nuevoNombre.trim();
    }

    public void redefinirPeriodos(List<PeriodoAnual> nuevos) {
        if (base) {
            throw new ReglaDominioException("La temporada base no tiene rangos: cubre todas las fechas no asignadas.");
        }
        this.periodos = validarPeriodos(nuevos);
    }

    /** Solo las temporadas especiales "cubren" fechas explícitamente; la base es el respaldo. */
    public boolean cubre(LocalDate fecha) {
        return !base && periodos.stream().anyMatch(p -> p.contiene(fecha));
    }

    /** 7.4: las temporadas no pueden solaparse entre sí. */
    public boolean seSolapaCon(Temporada otra) {
        if (base || otra.base) {
            return false;
        }
        return periodos.stream().anyMatch(p -> otra.periodos.stream().anyMatch(p::seSolapaCon));
    }

    private static List<PeriodoAnual> validarPeriodos(List<PeriodoAnual> periodos) {
        if (periodos == null || periodos.isEmpty()) {
            throw new ReglaDominioException("Una temporada especial requiere al menos un rango de fechas.");
        }
        for (int i = 0; i < periodos.size(); i++) {
            for (int j = i + 1; j < periodos.size(); j++) {
                if (periodos.get(i).seSolapaCon(periodos.get(j))) {
                    throw new ReglaDominioException("Los rangos de una misma temporada no pueden solaparse.");
                }
            }
        }
        return List.copyOf(periodos);
    }

    public IdTemporada getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean esBase() {
        return base;
    }

    public List<PeriodoAnual> getPeriodos() {
        return periodos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Temporada that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
