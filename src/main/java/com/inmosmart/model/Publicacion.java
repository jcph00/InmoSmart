package com.inmosmart.model;

import java.time.LocalDate;

public class Publicacion {

    private String codigoPublicacion;
    private LocalDate fechaPublicacion;
    private String descripcion;
    private Inmueble inmueble;

    public Publicacion(String codigoPublicacion, String descripcion, Inmueble inmueble) {
        if (inmueble == null)
            throw new IllegalArgumentException("Una publicación debe tener un inmueble");
        this.codigoPublicacion = codigoPublicacion;
        this.descripcion       = descripcion;
        this.inmueble          = inmueble;
        this.fechaPublicacion  = LocalDate.now();
    }

    public String getCodigoPublicacion()   { return codigoPublicacion; }
    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public String getDescripcion()         { return descripcion; }
    public Inmueble getInmueble()          { return inmueble; }

    @Override
    public String toString() {
        return String.format("Publicación [%s] — %s | Fecha: %s",
                codigoPublicacion, inmueble.getCodigo(), fechaPublicacion);
    }
}
