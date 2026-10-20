package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CodigoPolitica;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.Porcentaje;
import co.edu.uniquindio.sga.domain.valueobject.TramoCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Raíz del agregado PoliticaCancelacion: regla única y versionada (F-07). Una versión anterior nunca se modifica;
 * actualizar la política genera una versión nueva (RN-13). El contenido es inmutable; solo cambia si sigue vigente.
 */
public class PoliticaCancelacion {

    private static final int MINIMO_TRAMOS = 2;

    private final CodigoPolitica codigo;
    private final VersionPolitica version;
    private final List<TramoCancelacion> tramos; // ordenados de mayor a menor antelación
    private final Porcentaje retencionNoShow;
    private final LocalDateTime creadaEn;
    private boolean vigente;

    public PoliticaCancelacion(CodigoPolitica codigo, VersionPolitica version, List<TramoCancelacion> tramos,
                               Porcentaje retencionNoShow, LocalDateTime creadaEn) {
        if (codigo == null || version == null || retencionNoShow == null || creadaEn == null) {
            throw new ReglaDominioException("La política requiere código, versión, consecuencia de no-show y fecha.");
        }
        this.codigo = codigo;
        this.version = version;
        this.tramos = validarTramos(tramos);
        this.retencionNoShow = retencionNoShow;
        this.creadaEn = creadaEn;
        this.vigente = true;
    }

    /** Retención sobre el anticipo según la antelación con que se cancela (7.5). */
    public Dinero calcularRetencionPorCancelacion(Dinero anticipo, LocalDate hoy, Estancia estancia) {
        long dias = Math.max(0, ChronoUnit.DAYS.between(hoy, estancia.entrada()));
        TramoCancelacion aplicable = tramos.stream()
                .filter(t -> t.aplicaA(dias))
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException("La política no cubre la antelación indicada."));
        return aplicable.retencion().aplicarA(anticipo);
    }

    public Dinero calcularDevolucionPorCancelacion(Dinero anticipo, LocalDate hoy, Estancia estancia) {
        return anticipo.restar(calcularRetencionPorCancelacion(anticipo, hoy, estancia));
    }

    /** Consecuencia del no-show según esta versión (RN-13). */
    public Dinero calcularRetencionPorNoShow(Dinero anticipo) {
        return retencionNoShow.aplicarA(anticipo);
    }

    /** Genera la versión siguiente y deja esta como no vigente. La versión anterior no se edita. */
    public PoliticaCancelacion publicarNuevaVersion(List<TramoCancelacion> nuevosTramos,
                                                    Porcentaje nuevaRetencionNoShow, LocalDateTime ahora) {
        if (!vigente) {
            throw new ReglaDominioException("Solo la versión vigente de la política puede actualizarse.");
        }
        PoliticaCancelacion siguiente =
                new PoliticaCancelacion(codigo, version.siguiente(), nuevosTramos, nuevaRetencionNoShow, ahora);
        this.vigente = false;
        return siguiente;
    }

    private static List<TramoCancelacion> validarTramos(List<TramoCancelacion> candidatos) {
        if (candidatos == null || candidatos.size() < MINIMO_TRAMOS) {
            throw new ReglaDominioException("La política requiere al menos dos tramos de antelación.");
        }
        long distintos = candidatos.stream().map(TramoCancelacion::diasAntelacionMinima).distinct().count();
        if (distintos != candidatos.size()) {
            throw new ReglaDominioException("Los tramos no pueden repetir la misma antelación.");
        }
        if (candidatos.stream().noneMatch(t -> t.diasAntelacionMinima() == 0)) {
            throw new ReglaDominioException("Debe existir un tramo desde 0 días para cubrir toda cancelación.");
        }
        return candidatos.stream()
                .sorted(Comparator.comparingInt(TramoCancelacion::diasAntelacionMinima).reversed())
                .toList();
    }

    public CodigoPolitica getCodigo() {
        return codigo;
    }

    public VersionPolitica getVersion() {
        return version;
    }

    public List<TramoCancelacion> getTramos() {
        return tramos;
    }

    public Porcentaje getRetencionNoShow() {
        return retencionNoShow;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }

    public boolean estaVigente() {
        return vigente;
    }

    /** Identidad = código + versión: cada versión es un registro distinto del historial. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PoliticaCancelacion that)) return false;
        return Objects.equals(codigo, that.codigo) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo, version);
    }
}
