package com.inmosmart.model;

import java.time.LocalDate;

/**
 * Registra cada acción que generó puntos de reputación para un usuario.
 * Permite auditar el historial completo de reputación.
 */
public class EventoReputacion {

    private final String descripcion;
    private final int puntos;
    private final LocalDate fecha;

    public EventoReputacion(String descripcion, int puntos) {
        this.descripcion = descripcion;
        this.puntos      = puntos;
        this.fecha       = LocalDate.now();
    }

    public String getDescripcion() { return descripcion; }
    public int getPuntos()         { return puntos; }
    public LocalDate getFecha()    { return fecha; }

    @Override
    public String toString() {
        return String.format("[%s] +%d pts — %s", fecha, puntos, descripcion);
    }
}
