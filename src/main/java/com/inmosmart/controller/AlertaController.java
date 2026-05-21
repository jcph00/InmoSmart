package com.inmosmart.controller;

import com.inmosmart.model.Alerta;
import com.inmosmart.service.AlertaService;
import com.inmosmart.service.Inmobiliaria;
import com.inmosmart.service.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;

/**
 * Presenta la bandeja de alertas del usuario activo.
 * Ambos roles la usan.
 * Marca alertas como leídas al abrir la vista.
 */
public class AlertaController {

    // ── Resumen ───────────────────────────────────────────────────────────────
    @FXML private Label lblTotalAlertas;
    @FXML private Label lblNoLeidas;

    // ── Lista de alertas ─────────────────────────────────────────────────────
    @FXML private ListView<Alerta> lstAlertas;

    // ── Referencia al dashboard para actualizar badge ────────────────────────
    private DashboardController dashboardController;
    private Inmobiliaria        inmobiliaria;

    // ════════════════════════════════════════════════════════════════════════
    // INICIALIZACIÓN
    // ════════════════════════════════════════════════════════════════════════

    public void inicializar(Inmobiliaria inmobiliaria) {
        this.inmobiliaria = inmobiliaria;
        configurarCeldas();
        cargarAlertas();
    }

    public void setDashboardController(DashboardController dashboard) {
        this.dashboardController = dashboard;
    }

    private void configurarCeldas() {
        lstAlertas.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Alerta alerta, boolean empty) {
                super.updateItem(alerta, empty);
                if (alerta == null || empty) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(alerta.toString());

                // Alertas no leídas resaltadas
                setStyle(alerta.isLeida()
                        ? "-fx-text-fill: #64748B;"
                        : "-fx-font-weight: bold; -fx-text-fill: #1E293B;");
            }
        });
    }

    private void cargarAlertas() {
        String userId    = Sesion.getUsuarioActual().getId();
        AlertaService service = inmobiliaria.getAlertaService();

        List<Alerta> alertas = service.getAlertasPorUsuario(userId);
        int noLeidas = service.contarNoLeidas(userId);

        lblTotalAlertas.setText(String.valueOf(alertas.size()));
        lblNoLeidas.setText(String.valueOf(noLeidas));

        lstAlertas.getItems().setAll(alertas);

        // Marcar como leídas y refrescar badge
        service.marcarTodasLeidas(userId);
        if (dashboardController != null) {
            dashboardController.actualizarBadgeAlertas();
        }
    }

    @FXML
    private void onLimpiarLeidas() {
        String userId = Sesion.getUsuarioActual().getId();
        inmobiliaria.getAlertaService().limpiarAlertasLeidas(userId);
        cargarAlertas();
    }
}
