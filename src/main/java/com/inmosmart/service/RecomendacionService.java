package com.inmosmart.service;

import com.inmosmart.model.Comprador;
import com.inmosmart.model.Inmueble;
import com.inmosmart.model.ParametrosBusqueda;

import java.util.Collections;
import java.util.List;

/**
 * Analiza el historial de búsquedas de un Comprador y devuelve
 * inmuebles disponibles que coincidan con sus preferencias.
 *
 * Solo lectura — no modifica estado de ninguna entidad.
 */
public class RecomendacionService {

    private final Inmobiliaria inmobiliaria;

    // Margen de precio para considerar un inmueble "similar" (±20%)
    private static final double MARGEN_PRECIO = 0.20;

    public RecomendacionService(Inmobiliaria inmobiliaria) {
        if (inmobiliaria == null)
            throw new IllegalArgumentException("Inmobiliaria no puede ser nula");
        this.inmobiliaria = inmobiliaria;
    }

    // ════════════════════════════════════════════════════════════════════════
    // API PÚBLICA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Devuelve inmuebles disponibles similares al historial del comprador.
     * Si el comprador no tiene historial, devuelve lista vacía.
     *
     * Criterio de similitud: al menos dos de tres coinciden
     * (ciudad, tipo, rango de precio ±20%).
     */
    public List<Inmueble> recomendar(Comprador comprador) {
        if (comprador == null) return Collections.emptyList();

        List<ParametrosBusqueda> historial = comprador.getHistorialBusquedas();
        if (historial.isEmpty()) return Collections.emptyList();

        // Toma los últimos 3 parámetros de búsqueda para el análisis
        List<ParametrosBusqueda> recientes = historial.stream()
                .skip(Math.max(0, historial.size() - 3))
                .toList();

        return inmobiliaria.getListaInmuebles().stream()
                .filter(Inmueble::estaDisponible)
                .filter(inmueble -> puntajeCoincidencia(inmueble, recientes) >= 2)
                .toList();
    }

    // ════════════════════════════════════════════════════════════════════════
    // LÓGICA PRIVADA DE COINCIDENCIA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Calcula cuántos criterios cumple el inmueble respecto al historial.
     * Máximo 3 puntos: ciudad(1) + tipo(1) + precio(1).
     */
    private int puntajeCoincidencia(Inmueble inmueble,
                                    List<ParametrosBusqueda> recientes) {
        int puntaje = 0;

        long coincideCiudad = recientes.stream()
                .filter(p -> p.getCiudad() != null &&
                        p.getCiudad().equalsIgnoreCase(inmueble.getCiudad()))
                .count();

        long coincideTipo = recientes.stream()
                .filter(p -> p.getTipo() != null &&
                        p.getTipo() == inmueble.getTipo())
                .count();

        long coincidePrecio = recientes.stream()
                .filter(p -> enRangoPrecio(inmueble.getPrecio(), p))
                .count();

        if (coincideCiudad > 0) puntaje++;
        if (coincideTipo   > 0) puntaje++;
        if (coincidePrecio > 0) puntaje++;

        return puntaje;
    }

    /**
     * Verifica si el precio del inmueble está dentro del rango buscado,
     * con un margen de tolerancia del 20% hacia arriba.
     */
    private boolean enRangoPrecio(double precio, ParametrosBusqueda params) {
        double minEfectivo = params.getPrecioMin();
        double maxEfectivo = params.getPrecioMax() > 0
                ? params.getPrecioMax() * (1 + MARGEN_PRECIO)
                : Double.MAX_VALUE;
        return precio >= minEfectivo && precio <= maxEfectivo;
    }
}
