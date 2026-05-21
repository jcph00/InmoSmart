package com.inmosmart.controller;

import com.inmosmart.model.Comprador;
import com.inmosmart.model.Inmueble;
import com.inmosmart.service.Inmobiliaria;
import com.inmosmart.service.RecomendacionService;
import com.inmosmart.service.Sesion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

/**
 * Presenta inmuebles recomendados según el historial del comprador.
 * Solo accesible para Comprador.
 * No modifica estado — solo lectura.
 */
public class RecomendacionController {

    // ── Resumen superior ─────────────────────────────────────────────────────
    @FXML private Label lblTotalRecomendaciones;
    @FXML private Label lblBusquedasAnalizadas;
    @FXML private Label lblMensajeContexto;

    // ── Tabla de recomendaciones ─────────────────────────────────────────────
    @FXML private TableView<Inmueble>           tablaRecomendaciones;
    @FXML private TableColumn<Inmueble, String> colCodigo;
    @FXML private TableColumn<Inmueble, String> colTipo;
    @FXML private TableColumn<Inmueble, String> colCiudad;
    @FXML private TableColumn<Inmueble, String> colPrecio;
    @FXML private TableColumn<Inmueble, String> colArea;
    @FXML private TableColumn<Inmueble, String> colVendedor;

    private Inmobiliaria inmobiliaria;

    // ════════════════════════════════════════════════════════════════════════
    // INICIALIZACIÓN
    // ════════════════════════════════════════════════════════════════════════

    public void inicializar(Inmobiliaria inmobiliaria) {
        this.inmobiliaria = inmobiliaria;
        configurarTabla();
        cargarRecomendaciones();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colTipo.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getTipo().name()));
        colCiudad.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getCiudad()));
        colPrecio.setCellValueFactory(
                c -> new SimpleStringProperty(
                        String.format("$%,.0f", c.getValue().getPrecio())));
        colArea.setCellValueFactory(
                c -> new SimpleStringProperty(
                        String.format("%.0f m²", c.getValue().getArea())));
        colVendedor.setCellValueFactory(
                c -> new SimpleStringProperty(
                        c.getValue().getVendedor().getNombre()));
    }

    private void cargarRecomendaciones() {
        Comprador comprador = Sesion.getCompradorActual();
        int busquedas = comprador.getHistorialBusquedas().size();

        lblBusquedasAnalizadas.setText(busquedas + " búsquedas analizadas");

        if (busquedas == 0) {
            lblMensajeContexto.setText(
                    "Realiza búsquedas de inmuebles para recibir recomendaciones.");
            lblTotalRecomendaciones.setText("0");
            return;
        }

        RecomendacionService service = new RecomendacionService(inmobiliaria);
        List<Inmueble> recomendados = service.recomendar(comprador);

        lblTotalRecomendaciones.setText(String.valueOf(recomendados.size()));
        lblMensajeContexto.setText(recomendados.isEmpty()
                ? "No encontramos inmuebles similares a tus búsquedas recientes."
                : "Basado en tus últimas búsquedas, estos inmuebles podrían interesarte.");

        tablaRecomendaciones.setItems(
                FXCollections.observableArrayList(recomendados));
    }
}
