package com.inmosmart.model;

import com.inmosmart.interfaces.IComprador;
import com.inmosmart.model.enums.TipoInmueble;
import com.inmosmart.model.enums.TipoUsuario;
import com.inmosmart.model.ParametrosBusqueda;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Comprador extends Usuario implements IComprador {

    private List<Oferta> ofertasRealizadas;
    private List<ParametrosBusqueda> historialBusquedas;

    public Comprador(String id, String nombre, String identificacion,
                     String telefono, String correo) {
        super(id, nombre, identificacion, telefono, correo, TipoUsuario.COMPRADOR);
        this.ofertasRealizadas = new ArrayList<>();
        this.historialBusquedas = new ArrayList<>();
    }

    // ── IComprador ───────────────────────────────────────────────
    @Override
    public Oferta construirOferta(Inmueble inmueble, double valorOferta) {
        if (inmueble == null || valorOferta <= 0)
            throw new IllegalArgumentException("Inmueble inválido o valor <= 0");
        return new Oferta(
                UUID.randomUUID().toString(),
                this,
                inmueble,
                valorOferta,
                LocalDate.now()
        );
    }

    @Override
    public void registrarBusqueda(ParametrosBusqueda params) {
        if (params != null) historialBusquedas.add(params);
    }

    @Override
    public double calcularAhorro(Oferta oferta) {
        if (oferta == null) return 0;
        return oferta.getInmueble().getPrecio() - oferta.getValorOferta();
    }

    // ── Getters / utilidades ─────────────────────────────────────
    public List<Oferta> getOfertasRealizadas()   { return ofertasRealizadas; }
    public List<ParametrosBusqueda> getHistorialBusquedas() { return historialBusquedas;}

    @Override
    public String toString() {
        return "Comprador | " + super.toString();
    }
}
