package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Raíz del agregado Reserva: compromiso de ocupar un apartamento durante una estancia (sección 8).
 * Las reglas que necesitan otros agregados (disponibilidad, tarifas, bloqueos) las orquestan los servicios de dominio.
 */
public class Reserva {

    private final CodigoReserva codigo;
    private final CodigoTitular titular;
    private final Ocupante titularComoOcupante;
    private final VersionPolitica politicaVersionCongelada; // RN-22 / RN-13: nunca cambia, ni al modificar la reserva
    private final CanalOrigen canalOrigen;
    private final IdentificadorExterno identificadorExterno;
    private final LocalDateTime fechaCreacion;

    private IdentificacionApartamento apartamento;
    private Estancia estancia;
    private List<Ocupante> ocupantes;
    private Cotizacion cotizacionCongelada;
    private LocalTime horaLlegadaEstimada;
    private EstadoReserva estado;
    private Registro registro;
    private Salida salida;
    private MotivoCancelacion motivoCancelacion;

    private Reserva(CodigoReserva codigo, Titular titular, IdentificacionApartamento apartamento, Estancia estancia,
                    List<Ocupante> ocupantes, LocalTime horaLlegadaEstimada, Cotizacion cotizacion,
                    VersionPolitica politica, CanalOrigen canal, IdentificadorExterno identificadorExterno,
                    LocalDateTime ahora) {
        this.codigo = codigo;
        this.titular = titular.getCodigo();
        this.titularComoOcupante = titular.comoOcupante();
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.ocupantes = List.copyOf(ocupantes);
        this.horaLlegadaEstimada = horaLlegadaEstimada;
        this.cotizacionCongelada = cotizacion;
        this.politicaVersionCongelada = politica;
        this.canalOrigen = canal;
        this.identificadorExterno = identificadorExterno;
        this.fechaCreacion = ahora;
        this.estado = EstadoReserva.PENDIENTE; // 7.5: la reserva nace en PENDIENTE
    }

    /**
     * Crea una reserva PENDIENTE validando las condiciones propias (7.5: 1, 2 y 4).
     * Las condiciones 3, 5 y 6 (apartamento activo con tarifas, solapamientos, preparación) las verifican los servicios.
     */
    public static Reserva crear(CodigoReserva codigo, Titular titular, IdentificacionApartamento apartamento,
                                Capacidad capacidadApartamento, Estancia estancia, List<Ocupante> ocupantes,
                                LocalTime horaLlegadaEstimada, Cotizacion cotizacion, VersionPolitica politica,
                                CanalOrigen canal, IdentificadorExterno identificadorExterno,
                                UmbralEdadFacturable umbral, LocalDateTime ahora) {
        if (codigo == null || titular == null || apartamento == null || politica == null || ahora == null) {
            throw new ReglaDominioException("La reserva requiere código, titular, apartamento, política y fecha de creación.");
        }
        if (canal == null) {
            throw new ReglaDominioException("La reserva requiere canal de origen.");
        }
        // RN-19: toda reserva externa llega con identificador propio del canal
        if (canal.esExterno() != (identificadorExterno != null)) {
            throw new ReglaDominioException("Solo las reservas de canal externo llevan identificador externo, y todas deben llevarlo.");
        }
        validarContenido(titular.comoOcupante(), capacidadApartamento, estancia, ocupantes, cotizacion, umbral, ahora);
        return new Reserva(codigo, titular, apartamento, estancia, ocupantes, horaLlegadaEstimada, cotizacion,
                politica, canal, identificadorExterno, ahora);
    }

    // ---- Comportamiento de negocio ----

    public void definirHoraLlegadaEstimada(LocalTime hora) {
        if (hora == null) {
            throw new ReglaDominioException("La hora estimada de llegada es obligatoria.");
        }
        exigirModificable("definir la hora de llegada");
        this.horaLlegadaEstimada = hora;
    }

    /** RN-09: no se confirma sin hora estimada de llegada. */
    public void confirmar() {
        if (horaLlegadaEstimada == null) {
            throw new ReglaDominioException("Toda reserva debe registrar hora estimada de llegada antes de confirmarse.");
        }
        transitarA(EstadoReserva.CONFIRMADA);
    }

    /**
     * RN-10: solo desde CONFIRMADA y no antes de la fecha de entrada.
     * RN-11 (apartamento PREPARADO) es precondición externa que verifica el servicio.
     */
    public void registrarLlegada(Registro registro) {
        if (registro == null) {
            throw new ReglaDominioException("El registro de llegada es obligatorio.");
        }
        if (estado != EstadoReserva.CONFIRMADA) {
            throw new ReglaDominioException("Solo se puede registrar la llegada de una reserva CONFIRMADA.");
        }
        if (registro.fecha().isBefore(estancia.entrada())) {
            throw new ReglaDominioException("No se permite el registro antes de la fecha de entrada.");
        }
        transitarA(EstadoReserva.EN_CURSO);
        this.registro = registro;
    }

    public void registrarSalida(Salida salida) {
        if (salida == null) {
            throw new ReglaDominioException("El registro de salida es obligatorio.");
        }
        if (estado != EstadoReserva.EN_CURSO) {
            throw new ReglaDominioException("Solo puede salir un grupo con reserva EN_CURSO.");
        }
        if (salida.momento().isBefore(registro.momento())) {
            throw new ReglaDominioException("La salida no puede ser anterior al registro de llegada.");
        }
        transitarA(EstadoReserva.FINALIZADA);
        this.salida = salida;
    }

    /** 7.5: desde PENDIENTE o CONFIRMADA, siempre antes del registro. RN-12: deja de ser activa y libera sus noches. */
    public void cancelar(MotivoCancelacion motivo) {
        if (motivo == null) {
            throw new ReglaDominioException("La cancelación requiere un motivo.");
        }
        transitarA(EstadoReserva.CANCELADA);
        this.motivoCancelacion = motivo;
    }

    /** RN-21: una reserva PENDIENTE que supera el plazo de confirmación se cancela automáticamente. */
    public boolean haSuperadoPlazoConfirmacion(LocalDateTime ahora, PlazoConfirmacion plazoConfirmacion) {
        return estado == EstadoReserva.PENDIENTE && plazoConfirmacion.venceAntesDe(fechaCreacion, ahora);
    }

    public void cancelarPorVencimiento(LocalDateTime ahora, PlazoConfirmacion plazoConfirmacion) {
        if (!haSuperadoPlazoConfirmacion(ahora, plazoConfirmacion)) {
            throw new ReglaDominioException("La reserva no está PENDIENTE o aún está dentro del plazo de confirmación.");
        }
        cancelar(new MotivoCancelacion("Vencimiento del plazo de confirmación"));
    }

    /** Se declara a partir de la hora límite del día de entrada y solo sobre reservas CONFIRMADAS. */
    public void declararNoShow(LocalDateTime ahora, LocalTime horaLimite) {
        if (ahora.isBefore(estancia.entrada().atTime(horaLimite))) {
            throw new ReglaDominioException("El no-show solo puede declararse desde la hora límite del día de entrada.");
        }
        transitarA(EstadoReserva.NO_SHOW);
    }

    /**
     * RN-14: repite las validaciones de creación y reemplaza fechas/ocupantes/apartamento/valor.
     * Conserva la política congelada (RN-22). Devuelve la diferencia (+/-) que el folio registra como ajuste.
     */
    public BigDecimal modificar(IdentificacionApartamento nuevoApartamento, Capacidad capacidadNuevoApartamento,
                                Estancia nuevaEstancia, List<Ocupante> nuevosOcupantes,
                                Cotizacion nuevaCotizacion, UmbralEdadFacturable umbral, LocalDateTime ahora) {
        if (nuevoApartamento == null) {
            throw new ReglaDominioException("El apartamento es obligatorio.");
        }
        exigirModificable("modificar");
        validarContenido(titularComoOcupante, capacidadNuevoApartamento, nuevaEstancia, nuevosOcupantes,
                nuevaCotizacion, umbral, ahora);
        BigDecimal diferencia = nuevaCotizacion.diferenciaFrente(cotizacionCongelada);
        this.apartamento = nuevoApartamento;
        this.estancia = nuevaEstancia;
        this.ocupantes = List.copyOf(nuevosOcupantes);
        this.cotizacionCongelada = nuevaCotizacion;
        return diferencia;
    }

    // ---- Consultas ----

    /** Activa = PENDIENTE, CONFIRMADA o EN_CURSO: retiene disponibilidad. */
    public boolean estaActiva() {
        return estado.esActiva();
    }

    /** RN-01: solapamiento con otra estancia, solo cuenta si la reserva está activa. */
    public boolean seSolapaCon(Estancia otra) {
        return estaActiva() && estancia.seSolapaCon(otra);
    }

    public boolean ocupa(Noche noche) {
        return estaActiva() && estancia.contiene(noche);
    }

    public int totalOcupantes() {
        return ocupantes.size();
    }

    public long ocupantesFacturables(UmbralEdadFacturable umbral) {
        return ocupantes.stream().filter(o -> o.esFacturableEn(estancia.entrada(), umbral)).count();
    }

    public Dinero getValorCongelado() {
        return cotizacionCongelada.total();
    }

    // ---- Internos ----

    private void transitarA(EstadoReserva destino) {
        if (!estado.puedeTransitarA(destino)) {
            throw new ReglaDominioException("Transición inválida de reserva: " + estado + " → " + destino);
        }
        this.estado = destino;
    }

    private void exigirModificable(String accion) {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            throw new ReglaDominioException("Solo se puede " + accion + " una reserva PENDIENTE o CONFIRMADA.");
        }
    }

    private static void validarContenido(Ocupante titularOcupante, Capacidad capacidad, Estancia estancia,
                                         List<Ocupante> ocupantes, Cotizacion cotizacion,
                                         UmbralEdadFacturable umbral, LocalDateTime ahora) {
        if (estancia == null || capacidad == null || cotizacion == null || umbral == null) {
            throw new ReglaDominioException("Estancia, capacidad, cotización y umbral son obligatorios.");
        }
        // RN-04: no se crean (ni se mueven) reservas con entrada anterior a hoy
        if (estancia.iniciaAntesDe(ahora.toLocalDate())) {
            throw new ReglaDominioException("La fecha de entrada no puede ser anterior a hoy.");
        }
        if (ocupantes == null || ocupantes.isEmpty()) {
            throw new ReglaDominioException("La reserva requiere al menos un ocupante.");
        }
        // RN-02: tope rígido, todos los ocupantes cuentan
        if (!capacidad.admite(ocupantes.size())) {
            throw new ReglaDominioException("El total de ocupantes excede la capacidad del apartamento.");
        }
        // 3.2: el titular es siempre un ocupante facturable
        if (!ocupantes.contains(titularOcupante)
                || !titularOcupante.esFacturableEn(estancia.entrada(), umbral)) {
            throw new ReglaDominioException("El titular debe ser un ocupante facturable de la reserva.");
        }
        if (cotizacion.numeroNoches() != estancia.numeroNoches()) {
            throw new ReglaDominioException("La cotización debe cubrir exactamente las noches de la estancia.");
        }
    }

    // ---- Getters (sin setters públicos) ----

    public CodigoReserva getCodigo() {
        return codigo;
    }

    public CodigoTitular getTitular() {
        return titular;
    }

    public IdentificacionApartamento getApartamento() {
        return apartamento;
    }

    public Estancia getEstancia() {
        return estancia;
    }

    public List<Ocupante> getOcupantes() {
        return ocupantes;
    }

    public LocalTime getHoraLlegadaEstimada() {
        return horaLlegadaEstimada;
    }

    public Cotizacion getCotizacionCongelada() {
        return cotizacionCongelada;
    }

    public VersionPolitica getPoliticaVersionCongelada() {
        return politicaVersionCongelada;
    }

    public CanalOrigen getCanalOrigen() {
        return canalOrigen;
    }

    public IdentificadorExterno getIdentificadorExterno() {
        return identificadorExterno;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public Registro getRegistro() {
        return registro;
    }

    public Salida getSalida() {
        return salida;
    }

    public MotivoCancelacion getMotivoCancelacion() {
        return motivoCancelacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reserva that)) return false;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
