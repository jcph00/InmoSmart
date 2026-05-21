package com.inmosmart.controller;

import com.inmosmart.model.Transaccion;
import com.inmosmart.model.enums.EstadoInmueble;
import com.inmosmart.service.Inmobiliaria;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * Presenta reportes agregados del sistema.
 * Solo accesible para Vendedor.
 * No modifica estado — solo lectura.
 */
public class ReporteController {

    // ── Resumen superior ─────────────────────────────────────────────────────
    @FXML private Label lblTotalInmuebles;
    @FXML private Label lblDisponibles;
    @FXML private Label lblTransacciones;
    @FXML private Label lblVolumenTotal;

    // ── Listas de reporte ─────────────────────────────────────────────────────
    @FXML private ListView<String> lstReporteInmuebles;
    @FXML private ListView<String> lstReporteUsuarios;
    @FXML private ListView<String> lstReporteTransacciones;

    private Inmobiliaria inmobiliaria;

    // ════════════════════════════════════════════════════════════════════════
    // INICIALIZACIÓN
    // ════════════════════════════════════════════════════════════════════════

    public void inicializar(Inmobiliaria inmobiliaria) {
        this.inmobiliaria = inmobiliaria;
        cargarResumen();
        cargarReportes();
    }

    private void cargarResumen() {
        long disponibles = inmobiliaria.getListaInmuebles().stream()
                .filter(i -> i.getEstado() == EstadoInmueble.DISPONIBLE)
                .count();

        double volumen = inmobiliaria.getListaTransacciones().stream()
                .mapToDouble(Transaccion::valorFinal)
                .sum();

        lblTotalInmuebles.setText(
                String.valueOf(inmobiliaria.getListaInmuebles().size()));
        lblDisponibles.setText(String.valueOf(disponibles));
        lblTransacciones.setText(
                String.valueOf(inmobiliaria.getListaTransacciones().size()));
        lblVolumenTotal.setText(String.format("$%,.0f", volumen));
    }

    private void cargarReportes() {
        cargarEnLista(lstReporteInmuebles,
                inmobiliaria.generarReporteInmuebles());
        cargarEnLista(lstReporteUsuarios,
                inmobiliaria.generarReporteUsuarios());
        cargarEnLista(lstReporteTransacciones,
                inmobiliaria.generarReporteTransacciones());
    }

    /**
     * Divide el texto del reporte por líneas y llena la lista.
     * Cada línea del reporte es un item independiente.
     */
    private void cargarEnLista(ListView<String> lista, String reporte) {
        lista.getItems().clear();
        for (String linea : reporte.split("\n")) {
            if (!linea.isBlank()) lista.getItems().add(linea);
        }
    }
}