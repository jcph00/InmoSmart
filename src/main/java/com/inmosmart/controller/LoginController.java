package com.inmosmart.controller;

import com.inmosmart.model.Usuario;
import com.inmosmart.model.enums.TipoUsuario;
import com.inmosmart.service.Inmobiliaria;
import com.inmosmart.service.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

/**
 * Controlador de la vista de login.
 *
 * Responsabilidad: leer campos, delegar autenticación a Inmobiliaria,
 * iniciar sesión y navegar al dashboard.
 *
 * No contiene lógica de negocio ni acceso directo al modelo.
 */
public class LoginController {

    // ── Componentes FXML ─────────────────────────────────────────────────────
    @FXML private TextField     txtIdentificacion;
    @FXML private ComboBox<String> cmbRol;
    @FXML private Label         lblEstado;
    @FXML private PasswordField pwdContrasena;

    // ── Dependencia ──────────────────────────────────────────────────────────
    private Inmobiliaria inmobiliaria;

    // ════════════════════════════════════════════════════════════════════════
    // INICIALIZACIÓN
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Punto de entrada del controlador.
     * Inyecta la dependencia y prepara los componentes visuales.
     */
    public void inicializar(Inmobiliaria inmobiliaria) {
        this.inmobiliaria = inmobiliaria;
        configurarComboRol();
        limpiarEstado();
    }

    private void configurarComboRol() {
        cmbRol.getItems().addAll("Comprador", "Vendedor");
        cmbRol.setValue("Comprador");
    }

    // ════════════════════════════════════════════════════════════════════════
    // EVENTOS FXML
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Maneja el intento de login.
     * Valida campos, delega búsqueda a Inmobiliaria,
     * inicia sesión y navega al dashboard.
     */
    @FXML
    private void onLogin() {
        String identificacion = txtIdentificacion.getText().trim();
        String contrasena     = pwdContrasena.getText();          // ← nuevo
        TipoUsuario rol       = resolverRol(cmbRol.getValue());

        if (identificacion.isEmpty()) {
            mostrarError("Ingresa tu número de identificación");
            return;
        }
        if (contrasena.isEmpty()) {
            mostrarError("Ingresa tu contraseña");                // ← nuevo
            return;
        }

        // Usa autenticar() en lugar de buscarUsuarioPorIdentificacion()
        Optional<Usuario> resultado =
                inmobiliaria.autenticar(identificacion, contrasena, rol);  // ← cambia

        if (resultado.isEmpty()) {
            mostrarError("Credenciales incorrectas. Verifica tus datos.");  // ← mensaje actualizado
            return;
        }

        Sesion.iniciar(resultado.get());
        navegarAlDashboard();
    }

    /**
     * Limpia los campos del formulario.
     */
    @FXML
    private void onLimpiar() {
        txtIdentificacion.clear();
        pwdContrasena.clear();     // ← nuevo
        cmbRol.setValue("Comprador");
        limpiarEstado();
        txtIdentificacion.requestFocus();
    }

    // ════════════════════════════════════════════════════════════════════════
    // NAVEGACIÓN
    // ════════════════════════════════════════════════════════════════════════


    @FXML
    private void onIrARegistro() {
        RegistroController ctrl = NavegadorApp.irA(Vista.REGISTRO);
        ctrl.inicializar(inmobiliaria);
    }
    private void navegarAlDashboard() {
        DashboardController ctrl = NavegadorApp.irA(Vista.DASHBOARD);
        ctrl.inicializar(inmobiliaria);
    }

    // ════════════════════════════════════════════════════════════════════════
    // UTILIDADES PRIVADAS
    // ════════════════════════════════════════════════════════════════════════

    private TipoUsuario resolverRol(String seleccion) {
        return "Vendedor".equals(seleccion)
                ? TipoUsuario.VENDEDOR
                : TipoUsuario.COMPRADOR;
    }

    private void mostrarError(String mensaje) {
        lblEstado.setText(mensaje);
        lblEstado.setStyle("-fx-text-fill: #e74c3c;");
    }

    private void mostrarExito(String mensaje) {
        lblEstado.setText(mensaje);
        lblEstado.setStyle("-fx-text-fill: #27ae60;");
    }

    private void limpiarEstado() {
        lblEstado.setText("");
    }
}
