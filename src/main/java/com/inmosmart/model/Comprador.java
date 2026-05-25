package com.inmosmart.model;

import com.inmosmart.interfaces.IComprador;
import com.inmosmart.model.enums.TipoUsuario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Comprador extends Usuario implements IComprador {

    private List<Oferta>            ofertasRealizadas;
    private List<ParametrosBusqueda> historialBusquedas;

    public Comprador(String id, String nombre, String identificacion,
                     String telefono, String correo) {
        super(id, nombre, identificacion, telefono, correo, TipoUsuario.COMPRADOR);
        this.ofertasRealizadas  = new ArrayList<>();
        this.historialBusquedas = new ArrayList<>();
    }

    // ── IComprador ───────────────────────────────────────────────────────────

    /**
     * Construye una Oferta sin efectos secundarios.
     * No la registra en ninguna lista — eso lo hace Inmobiliaria.
     */
    @Override
    public Oferta construirOferta(Inmueble inmueble, double valorOferta) {
        if (inmueble == null || valorOferta <= 0)
            throw new IllegalArgumentException("Inmueble inválido o valor <= 0");
        return new Oferta(
                UUID.randomUUID().toString(),
                this, inmueble, valorOferta, LocalDate.now()
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

    // ── Gestión interna — llamado solo por Inmobiliaria ──────────────────────

    /**
     * Registra una oferta en la lista propia del comprador.
     * Solo Inmobiliaria debe llamar este método, tras validar
     * y registrar la oferta en el sistema.
     */
    public void agregarOferta(Oferta oferta) {
        if (oferta != null) ofertasRealizadas.add(oferta);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    /**
     * Lista de solo lectura — nadie modifica ofertasRealizadas
     * desde fuera del comprador.
     */
    public List<Oferta> getOfertasRealizadas() {
        return Collections.unmodifiableList(ofertasRealizadas);
    }

    public List<ParametrosBusqueda> getHistorialBusquedas() {
        return Collections.unmodifiableList(historialBusquedas);
    }

    @Override
    public String toString() {
        return "Comprador | " + super.toString();
    }
}
