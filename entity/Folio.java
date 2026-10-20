package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoFolio;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Pago;
import co.edu.uniquindio.sga.domain.valueobject.Saldo;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Raíz del agregado Folio: cuenta de una sola reserva (7.7). Cargos y pagos son append-only (RN-16):
 * toda corrección es un movimiento inverso.
 */
public class Folio {

    private final CodigoFolio codigo;
    private final CodigoReserva reserva;
    private List<Cargo> cargos;
    private List<Pago> pagos = List.of();
    private boolean cerrado;
    private AutorizacionCierre autorizacionCierre;

    private Folio(CodigoFolio codigo, CodigoReserva reserva, Cargo cargoAlojamiento) {
        this.codigo = codigo;
        this.reserva = reserva;
        this.cargos = List.of(cargoAlojamiento);
    }

    /** 7.7: el folio se abre al crear la reserva, con el cargo de alojamiento calculado. */
    public static Folio abrir(CodigoFolio codigo, CodigoReserva reserva, Cargo cargoAlojamiento) {
        if (codigo == null || reserva == null || cargoAlojamiento == null) {
            throw new ReglaDominioException("El folio requiere código, reserva y cargo de alojamiento.");
        }
        if (cargoAlojamiento.tipo() != TipoCargo.ALOJAMIENTO || cargoAlojamiento.inverso()) {
            throw new ReglaDominioException("El folio se abre con un cargo de alojamiento (no inverso).");
        }
        return new Folio(codigo, reserva, cargoAlojamiento);
    }

    public void agregarCargo(Cargo cargo) {
        exigirAbierto();
        if (cargo == null || cargo.inverso()) {
            throw new ReglaDominioException("Use registrarMovimientoInverso para corregir cargos.");
        }
        List<Cargo> actualizados = new ArrayList<>(cargos);
        actualizados.add(cargo);
        this.cargos = List.copyOf(actualizados);
    }

    /** RN-15: todo pago se registra contra el folio con su medio y su fecha. */
    public void registrarPago(Pago pago) {
        exigirAbierto();
        if (pago == null || pago.inverso()) {
            throw new ReglaDominioException("Use registrarMovimientoInverso para corregir pagos.");
        }
        List<Pago> actualizados = new ArrayList<>(pagos);
        actualizados.add(pago);
        this.pagos = List.copyOf(actualizados);
    }

    /** RN-16: corrige un cargo sin editarlo ni borrarlo. */
    public void registrarMovimientoInverso(Cargo original, LocalDate fecha) {
        exigirAbierto();
        if (original == null || original.inverso()) {
            throw new ReglaDominioException("No hay un cargo vigente que corregir con ese movimiento inverso.");
        }
        long originales = cargos.stream().filter(c -> c.equals(original)).count();
        long revertidos = cargos.stream().filter(c -> c.esInversoDe(original)).count();
        if (originales <= revertidos) {
            throw new ReglaDominioException("No hay un cargo vigente que corregir con ese movimiento inverso.");
        }
        List<Cargo> actualizados = new ArrayList<>(cargos);
        actualizados.add(original.invertido(fecha));
        this.cargos = List.copyOf(actualizados);
    }

    /** RN-16: corrige un pago sin editarlo ni borrarlo. */
    public void registrarMovimientoInverso(Pago original, LocalDate fecha) {
        exigirAbierto();
        if (original == null || original.inverso()) {
            throw new ReglaDominioException("No hay un pago vigente que corregir con ese movimiento inverso.");
        }
        long originales = pagos.stream().filter(p -> p.equals(original)).count();
        long revertidos = pagos.stream().filter(p -> p.esInversoDe(original)).count();
        if (originales <= revertidos) {
            throw new ReglaDominioException("No hay un pago vigente que corregir con ese movimiento inverso.");
        }
        List<Pago> actualizados = new ArrayList<>(pagos);
        actualizados.add(original.invertido(fecha));
        this.pagos = List.copyOf(actualizados);
    }

    /** 3.5 / RN-14: la diferencia (positiva o negativa) de una modificación se registra como ajuste. */
    public void registrarAjuste(BigDecimal diferencia, LocalDate fecha) {
        exigirAbierto();
        if (diferencia == null || diferencia.signum() == 0) {
            throw new ReglaDominioException("No hay diferencia que registrar como ajuste.");
        }
        List<Cargo> actualizados = new ArrayList<>(cargos);
        actualizados.add(new Cargo(TipoCargo.AJUSTE, "Ajuste por modificación de la reserva",
                new Dinero(diferencia.abs()), fecha, diferencia.signum() < 0));
        this.cargos = List.copyOf(actualizados);
    }

    /**
     * 7.5: al cancelar (o declarar no-show) se anulan los cargos vigentes y se registra la retención como
     * penalidad. Si lo pagado supera la retención, el saldo queda a favor: la devolución se calcula y se
     * deja registrada, el desembolso ocurre fuera del sistema (3.4).
     */
    public void liquidarRetencion(Dinero retencion, LocalDate fecha) {
        exigirAbierto();
        if (retencion == null || fecha == null) {
            throw new ReglaDominioException("La retención y la fecha son obligatorias.");
        }
        List<Cargo> actualizados = new ArrayList<>(cargos);
        Dinero vigentes = totalCargos();
        if (!vigentes.esCero()) {
            actualizados.add(new Cargo(TipoCargo.AJUSTE, "Anulación de cargos por cancelación o no-show",
                    vigentes, fecha, true));
        }
        if (!retencion.esCero()) {
            actualizados.add(Cargo.nuevo(TipoCargo.PENALIDAD, "Retención según política de cancelación",
                    retencion, fecha));
        }
        this.cargos = List.copyOf(actualizados);
    }

    public Dinero totalCargos() {
        BigDecimal total = cargos.stream()
                .map(c -> c.inverso() ? c.monto().valor().negate() : c.monto().valor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Dinero(total);
    }

    public Dinero totalPagos() {
        BigDecimal total = pagos.stream()
                .map(p -> p.inverso() ? p.monto().valor().negate() : p.monto().valor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Dinero(total);
    }

    /** El saldo es la diferencia entre cargos y pagos; puede ser positivo, cero o a favor. */
    public Saldo calcularSaldo() {
        return Saldo.entre(totalCargos(), totalPagos());
    }

    /** RN-17: sin autorización explícita, solo se cierra con saldo cero. */
    public void cerrar() {
        exigirAbierto();
        if (!calcularSaldo().esCero()) {
            throw new ReglaDominioException("El folio no puede cerrarse con saldo distinto de cero sin autorización.");
        }
        this.cerrado = true;
    }

    public void cerrarConAutorizacion(AutorizacionCierre autorizacion) {
        exigirAbierto();
        if (autorizacion == null) {
            throw new ReglaDominioException("La autorización de cierre es obligatoria.");
        }
        this.autorizacionCierre = autorizacion;
        this.cerrado = true;
    }

    private void exigirAbierto() {
        if (cerrado) {
            throw new ReglaDominioException("El folio está cerrado y no admite más movimientos.");
        }
    }

    public CodigoFolio getCodigo() {
        return codigo;
    }

    public CodigoReserva getReserva() {
        return reserva;
    }

    public List<Cargo> getCargos() {
        return cargos;
    }

    public List<Pago> getPagos() {
        return pagos;
    }

    public boolean estaCerrado() {
        return cerrado;
    }

    public AutorizacionCierre getAutorizacionCierre() {
        return autorizacionCierre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Folio that)) return false;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
