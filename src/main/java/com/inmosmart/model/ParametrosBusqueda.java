package com.inmosmart.model;

import com.inmosmart.model.enums.TipoInmueble;
import java.time.LocalDate;

/**
 * Encapsula los criterios de una búsqueda realizada por un Comprador.
 * Reemplaza el List<String> anterior — ahora es procesable por RecomendacionService.
 */
public class ParametrosBusqueda {

    private final String ciudad;
    private final TipoInmueble tipo;
    private final double precioMin;
    private final double precioMax;
    private final double areaMin;
    private final LocalDate fecha;

    public ParametrosBusqueda(String ciudad, TipoInmueble tipo,
                              double precioMin, double precioMax, double areaMin) {
        this.ciudad    = ciudad;
        this.tipo      = tipo;
        this.precioMin = precioMin;
        this.precioMax = precioMax;
        this.areaMin   = areaMin;
        this.fecha     = LocalDate.now();
    }

    public String getCiudad()      { return ciudad; }
    public TipoInmueble getTipo()  { return tipo; }
    public double getPrecioMin()   { return precioMin; }
    public double getPrecioMax()   { return precioMax; }
    public double getAreaMin()     { return areaMin; }
    public LocalDate getFecha()    { return fecha; }

    @Override
    public String toString() {
        return String.format("[%s] %s en %s | $%.0f - $%.0f | min %.0fm²",
                fecha, tipo, ciudad, precioMin, precioMax, areaMin);
    }
}
