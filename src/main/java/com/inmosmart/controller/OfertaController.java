package com.inmosmart.controller;

import com.inmosmart.model.Comprador;
import com.inmosmart.model.Oferta;
import com.inmosmart.model.Transaccion;
import com.inmosmart.model.enums.EstadoOferta;
import com.inmosmart.model.enums.TipoOperacion;
import com.inmosmart.service.Inmobiliaria;
import com.inmosmart.service.Sesion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controlador de la vista de ofertas.
 *
 * Modos:
 *  - COMPRADOR: visualiza sus ofertas y su estado actual.
 *  - VENDEDOR:  gestiona ofertas recibidas (aceptar / rechazar).
 */
public class OfertaController {

    // ── Encabezado dinámico ──────────────────────────────────────────────────
    @FXML private Label lblTituloSeccion;

    // ── Panel de resumen ─────────────────────────────────────────────────────
    @FXML private Label lblTotalOfertas;
    @FXML private Label lblPendientes;
    @FXML private Label lblAceptadas;
    @FXML private Label lblRechazadas;

    // ── Tabla de ofertas ─────────────────────────────────────────────────────
    @FXML private TableView<Oferta>           tablaOfertas;
    @FXML private TableColumn<Oferta, String> colCodigo;
    @FXML private TableColumn<Oferta, String> colInmueble;
    @FXML private TableColumn<Oferta, String> colValorOferta;
    @FXML private TableColumn<Oferta, String> colPrecioBase;
    @FXML private TableColumn<Oferta, String> colDiferencia;
    @FXML private TableColumn<Oferta, String> colFecha;
    @FXML private TableColumn<Oferta, String> colEstado;
    @FXML private TableColumn<Oferta, String> colComprador;  // solo vendedor

    // ── Panel de acción — solo vendedor ─────────────────────────────────────
    @FXML private VBox  panelAccionVendedor;
    @FXML private ComboBox<String> cmbTipoOperacion;
    @FXML private Button btnAceptar;
    @FXML private Button btnRechazar;
    @FXML private Label  lblInfoOfertaSeleccionada;

    // ── Estado / feedback ────────────────────────────────────────────────────
    @FXML private Label lblEstado;

    // ── Dependencias ─────────────────────────────────────────────────────────
    private Inmobiliaria        inmobiliaria;
    private DashboardController dashboardController; // opcional, para badge

    // ════════════════════════════════════════════════════════════════════════
    // INICIALIZACIÓN
    // ════════════════════════════════════════════════════════════════════════

    public void inicializar(Inmobiliaria inmobiliaria) {
        this.inmobiliaria = inmobiliaria;

        configurarTabla();
        configurarModoSegunRol();
        cargarOfertas();
        limpiarEstado();
    }

    /**
     * Inyección opcional del DashboardController.
     * Permite actualizar el badge de alertas tras procesar una oferta.
     */
    public void setDashboardController(DashboardController dashboard) {
        this.dashboardController = dashboard;
    }

    // ── Configuración de tabla ────────────────────────────────────────────────

    private void configurarTabla() {
        colCodigo.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getCodigoOferta()));
        colInmueble.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getInmueble().getCodigo()
                        + " — " + c.getValue().getInmueble().getCiudad()));
        colValorOferta.setCellValueFactory(
                c -> new SimpleStringProperty(
                        String.format("$%,.0f", c.getValue().getValorOferta())));
        colPrecioBase.setCellValueFactory(
                c -> new SimpleStringProperty(
                        String.format("$%,.0f",
                                c.getValue().getInmueble().getPrecio())));
        colDiferencia.setCellValueFactory(c -> {
            double diff = c.getValue().getInmueble().getPrecio()
                    - c.getValue().getValorOferta();
            String signo = diff >= 0 ? "-$" : "+$";
            return new SimpleStringProperty(
                    signo + String.format("%,.0f", Math.abs(diff)));
        });
        colFecha.setCellValueFactory(
                c -> new SimpleStringProperty(
                        c.getValue().getFechaOferta().toString()));
        colEstado.setCellValueFactory(
                c -> new SimpleStringProperty(
                        c.getValue().getEstado().name()));
        colComprador.setCellValueFactory(
                c -> new SimpleStringProperty(
                        c.getValue().getComprador().getNombre()));

        // Color de fila según estado
        tablaOfertas.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Oferta item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                    return;
                }
                switch (item.getEstado()) {
                    case PENDIENTE  -> setStyle("-fx-background-color: #fffbeb;");
                    case ACEPTADA   -> setStyle("-fx-background-color: #f0fdf4;");
                    case RECHAZADA  -> setStyle("-fx-background-color: #fef2f2;");
                }
            }
        });

        // Al seleccionar fila: actualiza panel de acción si es vendedor
        tablaOfertas.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, seleccionada) -> {
                    if (Sesion.esVendedor()) {
                        actualizarPanelAccion(seleccionada);
                    }
                });
    }

    private void configurarModoSegunRol() {
        boolean esVendedor = Sesion.esVendedor();

        // Título
        lblTituloSeccion.setText(esVendedor
                ? "Ofertas recibidas"
                : "Mis ofertas");

        // Columna comprador solo visible para vendedor
        colComprador.setVisible(esVendedor);

        // Panel de acción solo para vendedor
        panelAccionVendedor.setVisible(esVendedor);
        panelAccionVendedor.setManaged(esVendedor);

        if (esVendedor) {
            cmbTipoOperacion.getItems()
                    .addAll(TipoOperacion.VENTA.name(),
                            TipoOperacion.ARRIENDO.name());
            cmbTipoOperacion.setValue(TipoOperacion.VENTA.name());
            btnAceptar.setDisable(true);
            btnRechazar.setDisable(true);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // CARGA DE DATOS
    // ════════════════════════════════════════════════════════════════════════

    private void cargarOfertas() {
        List<Oferta> ofertas = obtenerOfertasSegunRol();
        tablaOfertas.setItems(FXCollections.observableArrayList(ofertas));
        actualizarResumen(ofertas);
    }

    private List<Oferta> obtenerOfertasSegunRol() {
        if (Sesion.esComprador()) {
            Comprador comprador = Sesion.getCompradorActual();
            return comprador.getOfertasRealizadas();
        }

        // Vendedor: filtra las ofertas globales por sus inmuebles
        return inmobiliaria.getListaOfertas().stream()
                .filter(o -> o.getInmueble().getVendedor()
                        .getId()
                        .equals(Sesion.getUsuarioActual().getId()))
                .toList();
    }

    private void actualizarResumen(List<Oferta> ofertas) {
        long pendientes = contarPorEstado(ofertas, EstadoOferta.PENDIENTE);
        long aceptadas  = contarPorEstado(ofertas, EstadoOferta.ACEPTADA);
        long rechazadas = contarPorEstado(ofertas, EstadoOferta.RECHAZADA);

        lblTotalOfertas.setText(String.valueOf(ofertas.size()));
        lblPendientes.setText(String.valueOf(pendientes));
        lblAceptadas.setText(String.valueOf(aceptadas));
        lblRechazadas.setText(String.valueOf(rechazadas));
    }

    private long contarPorEstado(List<Oferta> ofertas, EstadoOferta estado) {
        return ofertas.stream()
                .filter(o -> o.getEstado() == estado)
                .count();
    }

    // ════════════════════════════════════════════════════════════════════════
    // PANEL DE ACCIÓN — Vendedor
    // ════════════════════════════════════════════════════════════════════════

    private void actualizarPanelAccion(Oferta oferta) {
        if (oferta == null) {
            lblInfoOfertaSeleccionada.setText("Selecciona una oferta de la tabla");
            btnAceptar.setDisable(true);
            btnRechazar.setDisable(true);
            return;
        }

        boolean esPendiente = oferta.getEstado() == EstadoOferta.PENDIENTE;

        lblInfoOfertaSeleccionada.setText(String.format(
                "Inmueble: %s  |  Oferta: $%,.0f  |  Precio base: $%,.0f  |  Comprador: %s",
                oferta.getInmueble().getCodigo(),
                oferta.getValorOferta(),
                oferta.getInmueble().getPrecio(),
                oferta.getComprador().getNombre()
        ));

        btnAceptar.setDisable(!esPendiente);
        btnRechazar.setDisable(!esPendiente);
    }

    // ════════════════════════════════════════════════════════════════════════
    // EVENTOS FXML — Vendedor
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void onAceptarOferta() {
        Oferta seleccionada = tablaOfertas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Selecciona una oferta de la tabla.");
            return;
        }

        TipoOperacion tipo = TipoOperacion.valueOf(cmbTipoOperacion.getValue());

        try {
            Transaccion transaccion = inmobiliaria.procesarOferta(
                    seleccionada.getCodigoOferta(), true, tipo
            );

            mostrarExito(String.format(
                    "✓ Oferta aceptada. Transacción %s registrada por $%,.0f.",
                    transaccion.codigoTransaccion(),
                    transaccion.valorFinal()
            ));

            cargarOfertas();
            notificarDashboard();

        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onRechazarOferta() {
        Oferta seleccionada = tablaOfertas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Selecciona una oferta de la tabla.");
            return;
        }

        // Confirmación antes de rechazar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Rechazar oferta");
        confirmacion.setHeaderText("¿Confirmas el rechazo de esta oferta?");
        confirmacion.setContentText(
                "Comprador: " + seleccionada.getComprador().getNombre()
                        + "\nValor: $" + String.format("%,.0f",
                        seleccionada.getValorOferta())
        );

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    inmobiliaria.procesarOferta(
                            seleccionada.getCodigoOferta(), false, null
                    );
                    mostrarExito("Oferta rechazada. El inmueble volvió a estado disponible.");
                    cargarOfertas();
                    notificarDashboard();

                } catch (IllegalArgumentException | IllegalStateException e) {
                    mostrarError(e.getMessage());
                }
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // UTILIDADES PRIVADAS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Notifica al dashboard para refrescar el badge de alertas.
     * No lanza excepción si la referencia no fue inyectada.
     */
    private void notificarDashboard() {
        if (dashboardController != null) {
            dashboardController.actualizarBadgeAlertas();
        }
    }

    private void mostrarError(String mensaje) {
        lblEstado.setText("⚠ " + mensaje);
        lblEstado.setStyle("-fx-text-fill: #DC2626;");
    }

    private void mostrarExito(String mensaje) {
        lblEstado.setText(mensaje);
        lblEstado.setStyle("-fx-text-fill: #16A34A;");
    }

    private void limpiarEstado() {
        lblEstado.setText("");
    }
}
