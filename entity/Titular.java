package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CodigoTitular;
import co.edu.uniquindio.sga.domain.valueobject.Ocupante;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;

import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

/** Responsable de la reserva y de su pago (3.6). Existe aunque nunca tenga usuario: el dominio no conoce "usuario". */
public class Titular {

    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final CodigoTitular codigo;
    private final String nombre;
    private final LocalDate fechaNacimiento;
    private String telefono;
    private String correo;

    public Titular(CodigoTitular codigo, String nombre, LocalDate fechaNacimiento, String telefono, String correo) {
        if (codigo == null) {
            throw new ReglaDominioException("El titular debe tener un código.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El titular debe tener nombre.");
        }
        if (fechaNacimiento == null) {
            throw new ReglaDominioException("El titular debe tener fecha de nacimiento.");
        }
        this.codigo = codigo;
        this.nombre = nombre.trim();
        this.fechaNacimiento = fechaNacimiento;
        actualizarContacto(telefono, correo);
    }

    /** Cambiar el contacto no cambia la identidad del titular. */
    public final void actualizarContacto(String telefono, String correo) {
        if (telefono == null || telefono.isBlank()) {
            throw new ReglaDominioException("El titular debe tener teléfono.");
        }
        if (correo == null || !CORREO.matcher(correo.trim()).matches()) {
            throw new ReglaDominioException("El titular debe tener un correo válido (recibe los recordatorios).");
        }
        this.telefono = telefono.trim();
        this.correo = correo.trim();
    }

    public int edadEn(LocalDate fecha) {
        return comoOcupante().edadEn(fecha);
    }

    /** 3.2: el titular es siempre un ocupante de su reserva. */
    public Ocupante comoOcupante() {
        return new Ocupante(nombre, fechaNacimiento);
    }

    public boolean esFacturableEn(LocalDate fechaEntrada, UmbralEdadFacturable umbral) {
        return comoOcupante().esFacturableEn(fechaEntrada, umbral);
    }

    public CodigoTitular getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Titular that)) return false;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
