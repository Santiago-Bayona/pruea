package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Capacidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.GaleriaImagenes;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.RegistroTarifa;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Entidad Apartamento (Guía 04), raíz del agregado Apartamento (Guía 05) */
public class Apartamento {

    private final IdentificacionApartamento identificacion;
    private String nombre;
    private String descripcion;
    private int dormitorios;
    private Capacidad capacidad;
    private GaleriaImagenes galeria;
    private EstadoOperativo estadoOperativo;
    private boolean activo;
    // RN de oro #9: colecciones internas siempre de solo lectura; toda modificación reemplaza la referencia
    private List<String> dotacion = List.of();
    private List<Tarifa> tarifas = List.of();
    private List<RegistroTarifa> historialTarifas = List.of();

    /** Nace inactivo: 7.4 impide activar un apartamento sin tarifas completas, ver {@link #activar(Collection)}. */
    public Apartamento(IdentificacionApartamento identificacion, String nombre, String descripcion,
                       int dormitorios, Capacidad capacidad, GaleriaImagenes galeria) {
        if (identificacion == null) {
            throw new ReglaDominioException("El apartamento debe tener una identificación.");
        }
        validarDatos(nombre, descripcion, dormitorios);
        if (capacidad == null) {
            throw new ReglaDominioException("El apartamento debe tener capacidad.");
        }
        if (galeria == null) {
            throw new ReglaDominioException("El apartamento debe tener entre 1 y 10 imágenes.");
        }
        this.identificacion = identificacion;
        this.nombre = nombre.trim();
        this.descripcion = descripcion.trim();
        this.dormitorios = dormitorios;
        this.capacidad = capacidad;
        this.galeria = galeria;
        this.estadoOperativo = EstadoOperativo.PREPARADO;
        this.activo = false;
    }

    /** RN-02: la capacidad es un tope rígido, sin excepciones. Todos los ocupantes cuentan. */
    public boolean admite(int totalOcupantes) {
        return capacidad.admite(totalOcupantes);
    }

    /** 7.5 condición 3: solo un apartamento activo admite reservas nuevas. */
    public void verificarQueAceptaReservas() {
        if (!activo) {
            throw new ReglaDominioException("El apartamento no está activo y no admite reservas.");
        }
    }

    /** RN-11: solo puede recibir un grupo si está activo y en estado operativo PREPARADO. */
    public boolean puedeRecibirGrupo() {
        return activo && estadoOperativo.permiteRegistro();
    }

    /** RN-05: tarifa vigente del apartamento para una temporada dada. */
    public Optional<Tarifa> tarifaEn(IdTemporada temporada) {
        return tarifas.stream()
                .filter(tarifa -> tarifa.temporada().equals(temporada))
                .findFirst();
    }

    /** 7.4: define o reemplaza la tarifa de una temporada y deja constancia en el histórico. */
    public void definirTarifa(Tarifa tarifa, LocalDateTime vigenteDesde) {
        if (tarifa == null) {
            throw new ReglaDominioException("La tarifa a definir es obligatoria.");
        }
        List<Tarifa> actualizadas = new ArrayList<>(tarifas);
        actualizadas.removeIf(t -> t.temporada().equals(tarifa.temporada()));
        actualizadas.add(tarifa);
        List<RegistroTarifa> historial = new ArrayList<>(historialTarifas);
        historial.add(new RegistroTarifa(tarifa, vigenteDesde));
        this.tarifas = List.copyOf(actualizadas);
        this.historialTarifas = List.copyOf(historial);
    }

    public boolean tieneTarifasCompletas(Collection<IdTemporada> temporadas) {
        return !temporadas.isEmpty() && temporadas.stream().allMatch(t -> tarifaEn(t).isPresent());
    }

    /** 7.4: el sistema impide activar un apartamento sin tarifas en todas las temporadas. */
    public void activar(Collection<IdTemporada> temporadasDelAlojamiento) {
        if (!tieneTarifasCompletas(temporadasDelAlojamiento)) {
            throw new ReglaDominioException("No se puede activar el apartamento sin tarifa en todas las temporadas.");
        }
        this.activo = true;
    }

    /**
     * 7.3: baja lógica, solo sin reservas activas ni futuras (la consulta la hace el servicio).
     * Las reservas históricas siguen referenciando el apartamento.
     */
    public void retirarDeVenta(boolean tieneReservasActivasOFuturas) {
        if (tieneReservasActivasOFuturas) {
            throw new ReglaDominioException("No se puede retirar de la venta un apartamento con reservas activas o futuras.");
        }
        this.activo = false;
    }

    /**
     * 7.3: cambiar la capacidad no afecta las reservas ya creadas (RN-22). Devuelve true si disminuyó,
     * para que el servicio advierta al administrador y liste las reservas que quedaron por encima.
     */
    public boolean cambiarCapacidad(Capacidad nueva) {
        if (nueva == null) {
            throw new ReglaDominioException("La nueva capacidad es obligatoria.");
        }
        boolean disminuyo = nueva.esMenorQue(capacidad);
        this.capacidad = nueva;
        return disminuyo;
    }

    public void actualizarDatos(String nombre, String descripcion, int dormitorios) {
        validarDatos(nombre, descripcion, dormitorios);
        this.nombre = nombre.trim();
        this.descripcion = descripcion.trim();
        this.dormitorios = dormitorios;
    }

    public void reemplazarGaleria(GaleriaImagenes nueva) {
        if (nueva == null) {
            throw new ReglaDominioException("La galería de imágenes es obligatoria.");
        }
        this.galeria = nueva;
    }

    public void definirDotacion(List<String> nuevaDotacion) {
        if (nuevaDotacion == null || nuevaDotacion.stream().anyMatch(d -> d == null || d.isBlank())) {
            throw new ReglaDominioException("La dotación no puede contener elementos vacíos.");
        }
        this.dotacion = List.copyOf(nuevaDotacion);
    }

    // F-09: solo se ocupa un apartamento que está PREPARADO
    public void marcarOcupado() {
        if (estadoOperativo != EstadoOperativo.PREPARADO) {
            throw new ReglaDominioException("Solo se puede ocupar un apartamento que está PREPARADO.");
        }
        estadoOperativo = EstadoOperativo.OCUPADO;
    }

    // F-09: tras la salida del grupo, el apartamento OCUPADO queda pendiente de preparación
    public void marcarPendientePreparacion() {
        if (estadoOperativo != EstadoOperativo.OCUPADO) {
            throw new ReglaDominioException(
                    "Solo se puede marcar pendiente de preparación un apartamento OCUPADO.");
        }
        estadoOperativo = EstadoOperativo.PENDIENTE_PREPARACION;
    }

    // F-09: la preparación solo inicia si el apartamento estaba pendiente de ella
    public void marcarEnPreparacion() {
        if (estadoOperativo != EstadoOperativo.PENDIENTE_PREPARACION) {
            throw new ReglaDominioException(
                    "Solo se puede iniciar la preparación de un apartamento PENDIENTE_PREPARACION.");
        }
        estadoOperativo = EstadoOperativo.EN_PREPARACION;
    }

    // F-09: queda preparado al terminar la preparación, o al volver de estar fuera de servicio
    public void marcarPreparado() {
        if (estadoOperativo != EstadoOperativo.EN_PREPARACION && estadoOperativo != EstadoOperativo.FUERA_DE_SERVICIO) {
            throw new ReglaDominioException(
                    "Solo se puede preparar un apartamento que está EN_PREPARACION o FUERA_DE_SERVICIO.");
        }
        estadoOperativo = EstadoOperativo.PREPARADO;
    }

    // 7.6: fuera de servicio desde cualquier estado no ocupado (la restricción a administrador es del caso de uso)
    public void declararFueraDeServicio() {
        if (estadoOperativo == EstadoOperativo.OCUPADO) {
            throw new ReglaDominioException("No se puede declarar fuera de servicio un apartamento OCUPADO.");
        }
        if (estadoOperativo == EstadoOperativo.FUERA_DE_SERVICIO) {
            throw new ReglaDominioException("El apartamento ya está fuera de servicio.");
        }
        estadoOperativo = EstadoOperativo.FUERA_DE_SERVICIO;
    }

    private static void validarDatos(String nombre, String descripcion, int dormitorios) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El apartamento debe tener un nombre.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaDominioException("El apartamento debe tener una descripción.");
        }
        if (dormitorios < 1) {
            throw new ReglaDominioException("El apartamento debe tener al menos un dormitorio.");
        }
    }

    public IdentificacionApartamento getIdentificacion() {
        return identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getDormitorios() {
        return dormitorios;
    }

    public Capacidad getCapacidad() {
        return capacidad;
    }

    public GaleriaImagenes getGaleria() {
        return galeria;
    }

    public List<String> getDotacion() {
        return dotacion;
    }

    public EstadoOperativo getEstadoOperativo() {
        return estadoOperativo;
    }

    public boolean estaActivo() {
        return activo;
    }

    public List<Tarifa> getTarifas() {
        return tarifas;
    }

    public List<RegistroTarifa> getHistorialTarifas() {
        return historialTarifas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Apartamento that)) return false;
        return Objects.equals(identificacion, that.identificacion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identificacion);
    }
}
