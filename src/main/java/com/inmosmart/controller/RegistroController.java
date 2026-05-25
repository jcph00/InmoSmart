package com.inmosmart.controller;

import com.inmosmart.model.Comprador;
import com.inmosmart.model.Vendedor;
import com.inmosmart.service.Inmobiliaria;
import com.inmosmart.service.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.UUID;

/**
 * Controlador del formulario de registro de usuarios.
 *
 * Responsabilidades:
 *  - Capturar y validar formato de campos en UI.
 *  - Construir Comprador o Vendedor según rol seleccionado.
 *  - Delegar registro a Inmobiliaria.
 *  - Iniciar sesión automáticamente tras registro exitoso.
 *  - Navegar al Dashboard o regresar al Login.
 *
 * No contiene lógica de negocio ni validaciones de dominio.
 */
public class RegistroController {

    // ── Campos del formulario ────────────────────────────────────────────────
    @FXML private TextField     txtNombre;
    @FXML private TextField     txtIdentificacion;
    @FXML private TextField     txtTelefono;
    @FXML private TextField     txtCorreo;
    @FXML private PasswordField pwdContrasena;
    @FXML private PasswordField pwdConfirmacion;

    // ── Selección de rol ─────────────────────────────────────────────────────
    @FXML private RadioButton   rbComprador;
    @FXML private RadioButton   rbVendedor;
    @FXML private ToggleGroup   toggleRol;

    // ── Feedback ─────────────────────────────────────────────────────────────
    @FXML private Label         lblEstado;

    // ── Dependencia ──────────────────────────────────────────────────────────
    private Inmobiliaria inmobiliaria;

    // ════════════════════════════════════════════════════════════════════════
    // INICIALIZACIÓN
    // ════════════════════════════════════════════════════════════════════════

    public void inicializar(Inmobiliaria inmobiliaria) {
        this.inmobiliaria = inmobiliaria;
        rbComprador.setToggleGroup(toggleRol);
        rbVendedor.setToggleGroup(toggleRol);
        rbComprador.setSelected(true);
        limpiarEstado();
    }

    // ════════════════════════════════════════════════════════════════════════
    // EVENTOS FXML
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void onRegistrar() {
        String nombre         = txtNombre.getText().trim();
        String identificacion = txtIdentificacion.getText().trim();
        String telefono       = txtTelefono.getText().trim();
        String correo         = txtCorreo.getText().trim();
        String contrasena     = pwdContrasena.getText();         // ← nuevo
        String confirmacion   = pwdConfirmacion.getText();       // ← nuevo

        // Validaciones de UI existentes
        if (nombre.isEmpty() || identificacion.isEmpty()
                || telefono.isEmpty() || correo.isEmpty()) {
            mostrarError("Todos los campos son obligatorios.");
            return;
        }
        if (!correo.contains("@") || !correo.contains(".")) {
            mostrarError("Ingresa un correo electrónico válido.");
            return;
        }
        if (identificacion.length() < 6) {
            mostrarError("La identificación debe tener al menos 6 caracteres.");
            return;
        }

        // Validaciones de contraseña — nuevas
        if (contrasena.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        if (!contrasena.equals(confirmacion)) {
            mostrarError("Las contraseñas no coinciden.");
            return;
        }

        try {
            String id = UUID.randomUUID().toString();

            if (rbComprador.isSelected()) {
                Comprador comprador = new Comprador(
                        id, nombre, identificacion,
                        telefono, correo, contrasena   // ← nuevo parámetro
                );
                inmobiliaria.registrarComprador(comprador);
                Sesion.iniciar(comprador);
            } else {
                Vendedor vendedor = new Vendedor(
                        id, nombre, identificacion,
                        telefono, correo, contrasena   // ← nuevo parámetro
                );
                inmobiliaria.registrarVendedor(vendedor);
                Sesion.iniciar(vendedor);
            }

            DashboardController ctrl = NavegadorApp.irA(Vista.DASHBOARD);
            ctrl.inicializar(inmobiliaria);

        } catch (IllegalStateException | IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        LoginController ctrl = NavegadorApp.irA(Vista.LOGIN);
        ctrl.inicializar(inmobiliaria);
    }

    @FXML
    private void onLimpiar() {
        txtNombre.clear();
        txtIdentificacion.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        pwdContrasena.clear();     // ← nuevo
        pwdConfirmacion.clear();   // ← nuevo
        rbComprador.setSelected(true);
        limpiarEstado();
        txtNombre.requestFocus();
    }

    // ════════════════════════════════════════════════════════════════════════
    // UTILIDADES PRIVADAS
    // ════════════════════════════════════════════════════════════════════════

    private void mostrarError(String mensaje) {
        lblEstado.setText("⚠  " + mensaje);
        lblEstado.setStyle("-fx-text-fill: #DC2626;");
    }

    private void mostrarExito(String mensaje) {
        lblEstado.setText("✓  " + mensaje);
        lblEstado.setStyle("-fx-text-fill: #16A34A;");
    }

    private void limpiarEstado() {
        lblEstado.setText("");
    }
}
