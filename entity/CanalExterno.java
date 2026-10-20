package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CodigoCanalExterno;

import java.util.Objects;

/** Canal de venta externo registrado, con su tipo y credencial de acceso (7.8). */
public class CanalExterno {

    private final CodigoCanalExterno codigo;
    private final String tipo;
    private String credencial;
    private boolean activo;

    public CanalExterno(CodigoCanalExterno codigo, String tipo, String credencial) {
        if (codigo == null || tipo == null || tipo.isBlank()) {
            throw new ReglaDominioException("El canal externo requiere código y tipo.");
        }
        this.codigo = codigo;
        this.tipo = tipo.trim();
        this.credencial = validarCredencial(credencial);
        this.activo = true;
    }

    public void rotarCredencial(String nuevaCredencial) {
        this.credencial = validarCredencial(nuevaCredencial);
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    /** Un canal desactivado no puede vender sobre el inventario. */
    public boolean puedeVender() {
        return activo;
    }

    private static String validarCredencial(String credencial) {
        if (credencial == null || credencial.isBlank()) {
            throw new ReglaDominioException("El canal externo requiere una credencial.");
        }
        return credencial.trim();
    }

    public CodigoCanalExterno getCodigo() {
        return codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCredencial() {
        return credencial;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CanalExterno that)) return false;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
