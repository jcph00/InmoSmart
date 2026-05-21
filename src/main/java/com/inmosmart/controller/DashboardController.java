package com.inmosmart.controller;

import com.inmosmart.MainApp;
import com.inmosmart.model.Usuario;
import com.inmosmart.service.Inmobiliaria;
import com.inmosmart.service.Sesion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Controlador del panel principal de la aplicación.
 *
 * Responsabilidades:
 *  - Configurar sidebar según rol del usuario activo.
 *  - Gestionar la carga de contenido dinámico en contentArea.
 *  - Mantener el badge de alertas actualizado.
 *  - Delegar toda lógica de negocio a los controladores de cada sección.
 */
public class DashboardController {

    // ── Sidebar — información del usuario ────────────────────────────────────
    @FXML private Label  lblNombreUsuario;
    @FXML private Label  lblRolUsuario;
    @FXML private Label  lblRangoUsuario;
    @FXML private Label  lblPuntosUsuario;

    // ── Sidebar — botones comunes ─────────────────────────────────────────────
    @FXML private Button btnAlertas;
    @FXML private Label  lblBadgeAlertas;

    // ── Sidebar — botones específicos por rol ────────────────────────────────
    // Comprador
    @FXML private Button btnBuscarInmuebles;
    @FXML private Button btnMisOfertas;
    @FXML private Button btnRecomendaciones;

    // Vendedor
    @FXML private Button btnMisInmuebles;
    @FXML private Button btnOfertasRecibidas;
    @FXML private Button btnReportes;

    // ── Área de contenido dinámico ───────────────────────────────────────────
    @FXML private StackPane contentArea;

    // ── Seguimiento del botón activo ─────────────────────────────────────────
    private Button botonActivo;

    // ── Dependencia ──────────────────────────────────────────────────────────
    private Inmobiliaria inmobiliaria;

    // ════════════════════════════════════════════════════════════════════════
    // INICIALIZACIÓN
    // ════════════════════════════════════════════════════════════════════════

    public void inicializar(Inmobiliaria inmobiliaria) {
        this.inmobiliaria = inmobiliaria;

        configurarSidebarSegunRol();
        mostrarInfoUsuario();
        actualizarBadgeAlertas();
        cargarHomeContent();
    }

    private void configurarSidebarSegunRol() {
        boolean esComprador = Sesion.esComprador();

        // Botones del comprador
        btnBuscarInmuebles.setVisible(esComprador);
        btnBuscarInmuebles.setManaged(esComprador);
        btnMisOfertas.setVisible(esComprador);
        btnMisOfertas.setManaged(esComprador);
        btnRecomendaciones.setVisible(esComprador);
        btnRecomendaciones.setManaged(esComprador);

        // Botones del vendedor
        btnMisInmuebles.setVisible(!esComprador);
        btnMisInmuebles.setManaged(!esComprador);
        btnOfertasRecibidas.setVisible(!esComprador);
        btnOfertasRecibidas.setManaged(!esComprador);
        btnReportes.setVisible(!esComprador);
        btnReportes.setManaged(!esComprador);
    }

    private void mostrarInfoUsuario() {
        Usuario usuario = Sesion.getUsuarioActual();
        lblNombreUsuario.setText(usuario.getNombre());
        lblRolUsuario.setText(Sesion.esComprador() ? "Comprador" : "Vendedor");
        lblRangoUsuario.setText(usuario.getRango());
        lblPuntosUsuario.setText(usuario.getPuntosReputacion() + " pts");
    }

    // ════════════════════════════════════════════════════════════════════════
    // CARGA DE CONTENIDO DINÁMICO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Carga un FXML en el contentArea y devuelve su controlador.
     * Es el único lugar donde se manipula el contentArea.
     */
    private <T> T cargarContenido(Vista vista, Button boton) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource(vista.getRuta())
            );
            Parent contenido = loader.load();
            contentArea.getChildren().setAll(contenido);
            actualizarBotonActivo(boton);
            return loader.getController();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error cargando vista interna: " + vista.getRuta(), e
            );
        }
    }

    private void cargarHomeContent() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource(Vista.HOME_CONTENT.getRuta())
            );
            Parent contenido = loader.load();
            contentArea.getChildren().setAll(contenido);

            HomeContentController ctrl = loader.getController();
            ctrl.inicializar(inmobiliaria);

        } catch (IOException e) {
            throw new RuntimeException("Error cargando home content", e);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // EVENTOS FXML — Navegación sidebar
    // ════════════════════════════════════════════════════════════════════════

    // ── Comprador ────────────────────────────────────────────────────────────

    @FXML
    private void onBuscarInmuebles() {
        InmuebleController ctrl =
                cargarContenido(Vista.INMUEBLES_CONTENT, btnBuscarInmuebles);
        ctrl.inicializar(inmobiliaria);
    }

    @FXML
    private void onMisOfertas() {
        OfertaController ctrl =
                cargarContenido(Vista.OFERTAS_CONTENT, btnMisOfertas);
        ctrl.inicializar(inmobiliaria);
        ctrl.setDashboardController(this);   // ← inyección del badge
    }

    @FXML
    private void onRecomendaciones() {
        RecomendacionController ctrl =
                cargarContenido(Vista.RECOMENDACIONES_CONTENT, btnRecomendaciones);
        ctrl.inicializar(inmobiliaria);
    }

    // ── Vendedor ─────────────────────────────────────────────────────────────

    @FXML
    private void onMisInmuebles() {
        InmuebleController ctrl =
                cargarContenido(Vista.INMUEBLES_CONTENT, btnMisInmuebles);
        ctrl.inicializar(inmobiliaria);
    }

    @FXML
    private void onOfertasRecibidas() {
        OfertaController ctrl =
                cargarContenido(Vista.OFERTAS_CONTENT, btnOfertasRecibidas);
        ctrl.inicializar(inmobiliaria);
        ctrl.setDashboardController(this);   // ← inyección del badge
    }

    @FXML
    private void onReportes() {
        ReporteController ctrl =
                cargarContenido(Vista.REPORTES_CONTENT, btnReportes);
        ctrl.inicializar(inmobiliaria);
    }

    // ── Comunes ──────────────────────────────────────────────────────────────

    @FXML
    private void onAlertas() {
        AlertaController ctrl =
                cargarContenido(Vista.ALERTAS_CONTENT, btnAlertas);
        ctrl.inicializar(inmobiliaria);
        ctrl.setDashboardController(this);
        actualizarBadgeAlertas();
    }

    @FXML
    private void onCerrarSesion() {
        Sesion.cerrar();
        LoginController ctrl = NavegadorApp.irA(Vista.LOGIN);
        ctrl.inicializar(inmobiliaria);
    }

    // ════════════════════════════════════════════════════════════════════════
    // UTILIDADES PRIVADAS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Actualiza el estilo visual del botón activo en el sidebar.
     */
    private void actualizarBotonActivo(Button nuevoActivo) {
        if (botonActivo != null) {
            botonActivo.getStyleClass().remove("sidebar-btn-active");
        }
        if (nuevoActivo != null) {
            nuevoActivo.getStyleClass().add("sidebar-btn-active");
        }
        botonActivo = nuevoActivo;
    }

    /**
     * Actualiza el badge de alertas no leídas.
     * Se llama tras cargar el dashboard y tras marcar alertas como leídas.
     */
    public void actualizarBadgeAlertas() {
        if (!Sesion.hayUsuarioActivo()) return;

        int noLeidas = inmobiliaria.getAlertaService()
                .contarNoLeidas(Sesion.getUsuarioActual().getId());

        lblBadgeAlertas.setText(String.valueOf(noLeidas));
        lblBadgeAlertas.setVisible(noLeidas > 0);
        lblBadgeAlertas.setManaged(noLeidas > 0);
    }
}
