package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CodigoAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.Ocupante;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import co.edu.uniquindio.sga.domain.valueobject.Ubicacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Raíz del agregado Alojamiento: el negocio completo, su configuración y sus temporadas.
 * Los apartamentos son otro agregado (se referencian por identificación, no se contienen aquí).
 */
public class Alojamiento {

    private static final int MINIMO_TEMPORADAS_ESPECIALES = 2;

    private final CodigoAlojamiento codigo;
    private String nombre;
    private String descripcion;
    private Ubicacion ubicacion;
    private ConfiguracionAlojamiento configuracion;
    private List<Temporada> temporadas;
    private List<String> normasConvivencia = List.of();
    private List<ServicioAdicional> serviciosAdicionales = List.of();

    public Alojamiento(CodigoAlojamiento codigo, String nombre, String descripcion, Ubicacion ubicacion,
                       ConfiguracionAlojamiento configuracion, List<Temporada> temporadas) {
        if (codigo == null) {
            throw new ReglaDominioException("El alojamiento debe tener un código.");
        }
        validarDatosBasicos(nombre, descripcion, ubicacion, configuracion);
        this.codigo = codigo;
        this.nombre = nombre.trim();
        this.descripcion = descripcion.trim();
        this.ubicacion = ubicacion;
        this.configuracion = configuracion;
        this.temporadas = validarTemporadas(temporadas);
    }

    /** Ninguna fecha reservable queda sin temporada: si ninguna especial la cubre, aplica la base. */
    public Temporada obtenerTemporada(LocalDate fecha) {
        return temporadas.stream()
                .filter(t -> t.cubre(fecha))
                .findFirst()
                .orElseGet(this::temporadaBase);
    }

    public Temporada temporadaBase() {
        return temporadas.stream().filter(Temporada::esBase).findFirst().orElseThrow();
    }

    public void agregarTemporada(Temporada nueva) {
        if (nueva == null) {
            throw new ReglaDominioException("La temporada a agregar es obligatoria.");
        }
        if (nueva.esBase()) {
            throw new ReglaDominioException("Ya existe una temporada base y solo puede haber una.");
        }
        List<Temporada> actualizadas = new ArrayList<>(temporadas);
        actualizadas.add(nueva);
        this.temporadas = validarTemporadas(actualizadas);
    }

    public void retirarTemporada(IdTemporada id) {
        Temporada objetivo = temporadas.stream().filter(t -> t.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ReglaDominioException("La temporada indicada no existe en el alojamiento."));
        if (objetivo.esBase()) {
            throw new ReglaDominioException("La temporada base es obligatoria y no se puede retirar.");
        }
        List<Temporada> restantes = new ArrayList<>(temporadas);
        restantes.remove(objetivo);
        this.temporadas = validarTemporadas(restantes);
    }

    public void actualizarConfiguracion(ConfiguracionAlojamiento nueva) {
        if (nueva == null) {
            throw new ReglaDominioException("La configuración es obligatoria.");
        }
        this.configuracion = nueva;
    }

    public void cambiarUbicacion(Ubicacion nueva) {
        if (nueva == null) {
            throw new ReglaDominioException("La ubicación es obligatoria.");
        }
        this.ubicacion = nueva;
    }

    public void actualizarDescripcion(String nombre, String descripcion) {
        validarDatosBasicos(nombre, descripcion, ubicacion, configuracion);
        this.nombre = nombre.trim();
        this.descripcion = descripcion.trim();
    }

    public void definirNormasConvivencia(List<String> normas) {
        if (normas == null || normas.stream().anyMatch(n -> n == null || n.isBlank())) {
            throw new ReglaDominioException("Las normas de convivencia no pueden estar vacías.");
        }
        this.normasConvivencia = List.copyOf(normas);
    }

    public void agregarServicioAdicional(ServicioAdicional servicio) {
        if (servicio == null) {
            throw new ReglaDominioException("El servicio adicional es obligatorio.");
        }
        boolean repetido = serviciosAdicionales.stream()
                .anyMatch(s -> s.nombre().equalsIgnoreCase(servicio.nombre()));
        if (repetido) {
            throw new ReglaDominioException("Ya existe un servicio adicional con ese nombre.");
        }
        List<ServicioAdicional> actualizados = new ArrayList<>(serviciosAdicionales);
        actualizados.add(servicio);
        this.serviciosAdicionales = List.copyOf(actualizados);
    }

    /** RN-06 con el umbral configurado del alojamiento. */
    public boolean esFacturable(Ocupante ocupante, LocalDate fechaEntrada) {
        return ocupante.esFacturableEn(fechaEntrada, configuracion.umbralEdadFacturable());
    }

    private static void validarDatosBasicos(String nombre, String descripcion, Ubicacion ubicacion,
                                            ConfiguracionAlojamiento configuracion) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El alojamiento debe tener nombre.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaDominioException("El alojamiento debe tener descripción.");
        }
        if (ubicacion == null) {
            throw new ReglaDominioException("El alojamiento debe tener ubicación.");
        }
        if (configuracion == null) {
            throw new ReglaDominioException("El alojamiento debe tener su configuración definida.");
        }
    }

    /** Invariantes: una sola base, al menos 2 especiales, sin ids repetidos y sin solapamientos. */
    private static List<Temporada> validarTemporadas(List<Temporada> candidatas) {
        if (candidatas == null) {
            throw new ReglaDominioException("El alojamiento requiere sus temporadas.");
        }
        long bases = candidatas.stream().filter(Temporada::esBase).count();
        if (bases != 1) {
            throw new ReglaDominioException("Debe existir exactamente una temporada base.");
        }
        if (candidatas.size() - bases < MINIMO_TEMPORADAS_ESPECIALES) {
            throw new ReglaDominioException("Debe haber al menos dos temporadas además de la base.");
        }
        for (int i = 0; i < candidatas.size(); i++) {
            for (int j = i + 1; j < candidatas.size(); j++) {
                if (candidatas.get(i).equals(candidatas.get(j))) {
                    throw new ReglaDominioException("No puede haber temporadas con el mismo identificador.");
                }
                if (candidatas.get(i).seSolapaCon(candidatas.get(j))) {
                    throw new ReglaDominioException("Las temporadas no pueden solaparse entre sí.");
                }
            }
        }
        return List.copyOf(candidatas);
    }

    public CodigoAlojamiento getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public ConfiguracionAlojamiento getConfiguracion() {
        return configuracion;
    }

    public List<Temporada> getTemporadas() {
        return temporadas;
    }

    public List<String> getNormasConvivencia() {
        return normasConvivencia;
    }

    public List<ServicioAdicional> getServiciosAdicionales() {
        return serviciosAdicionales;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alojamiento that)) return false;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
