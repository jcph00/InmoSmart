package com.inmosmart.model;

import com.inmosmart.model.enums.EstadoInmueble;
import com.inmosmart.model.enums.TipoInmueble;

public class Inmueble {

    private String codigo;
    private TipoInmueble tipo;
    private String direccion;
    private String ciudad;
    private double area;
    private double precio;
    private EstadoInmueble estado;
    private Vendedor vendedor;

    public Inmueble(String codigo, TipoInmueble tipo, String direccion,
                    String ciudad, double area, double precio, Vendedor vendedor) {
        if (precio <= 0) throw new IllegalArgumentException("El precio debe ser mayor a 0");
        if (area  <= 0) throw new IllegalArgumentException("El área debe ser mayor a 0");

        this.codigo    = codigo;
        this.tipo      = tipo;
        this.direccion = direccion;
        this.ciudad    = ciudad;
        this.area      = area;
        this.precio    = precio;
        this.estado    = EstadoInmueble.DISPONIBLE;
        this.vendedor  = vendedor;
    }

    // ── Getters ──────────────────────────────────────────────────
    public String getCodigo()           { return codigo; }
    public TipoInmueble getTipo()       { return tipo; }
    public String getDireccion()        { return direccion; }
    public String getCiudad()           { return ciudad; }
    public double getArea()             { return area; }
    public double getPrecio()           { return precio; }
    public EstadoInmueble getEstado()   { return estado; }
    public Vendedor getVendedor()       { return vendedor; }

    // ── Setters controlados ──────────────────────────────────────
    public void setPrecio(double precio) {
        if (precio > 0) this.precio = precio;
    }
    public void setEstado(EstadoInmueble estado) {
        if (estado != null) this.estado = estado;
    }

    public boolean estaDisponible() {
        return this.estado == EstadoInmueble.DISPONIBLE;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s | $%.0f | %s m² | %s",
                tipo, codigo, ciudad, precio, area, estado);
    }
}