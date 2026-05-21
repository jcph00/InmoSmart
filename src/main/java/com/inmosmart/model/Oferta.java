package com.inmosmart.model;

import com.inmosmart.model.enums.EstadoOferta;

import java.time.LocalDate;

public class Oferta {

    private String codigoOferta;
    private Comprador comprador;
    private Inmueble inmueble;
    private double valorOferta;
    private LocalDate fechaOferta;
    private EstadoOferta estado;

    public Oferta(String codigoOferta, Comprador comprador,
                  Inmueble inmueble, double valorOferta, LocalDate fechaOferta) {
        if (valorOferta <= 0)
            throw new IllegalArgumentException("El valor de la oferta debe ser mayor a 0");
        this.codigoOferta = codigoOferta;
        this.comprador    = comprador;
        this.inmueble     = inmueble;
        this.valorOferta  = valorOferta;
        this.fechaOferta  = fechaOferta;
        this.estado       = EstadoOferta.PENDIENTE;
    }

    public String getCodigoOferta()    { return codigoOferta; }
    public Comprador getComprador()    { return comprador; }
    public Inmueble getInmueble()      { return inmueble; }
    public double getValorOferta()     { return valorOferta; }
    public LocalDate getFechaOferta()  { return fechaOferta; }
    public EstadoOferta getEstado()    { return estado; }

    public void setEstado(EstadoOferta estado) {
        if (estado != null) this.estado = estado;
    }

    @Override
    public String toString() {
        return String.format("Oferta [%s] | Comprador: %s | Inmueble: %s | $%.0f | %s",
                codigoOferta, comprador.getNombre(),
                inmueble.getCodigo(), valorOferta, estado);
    }
}
