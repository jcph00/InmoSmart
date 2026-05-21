package com.inmosmart.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.inmosmart.MainApp;

import java.io.IOException;

/**
 * Gestor central de navegación entre vistas.
 *
 * Responsabilidades:
 *  - Mantener referencia al Stage principal.
 *  - Cargar FXMLs y cambiar la escena activa.
 *  - Proveer acceso al controlador recién cargado
 *    para inyectar dependencias antes de mostrar la vista.
 *
 * Los controladores nunca manipulan el Stage directamente.
 * Siempre navegan a través de NavegadorApp.
 */
public class NavegadorApp {

    private static Stage stagePrincipal;

    // Constructor privado — solo métodos estáticos
    private NavegadorApp() {}

    /**
     * Inicializa el navegador con el Stage principal de la aplicación.
     * Se llama una única vez desde MainApp.start().
     */
    public static void inicializar(Stage stage) {
        stagePrincipal = stage;
    }

    /**
     * Navega a una vista y devuelve su controlador ya inicializado.
     * Permite inyectar datos al controlador antes de que la vista sea visible.
     *
     * Uso típico en un controlador:
     *   DashboardController ctrl = NavegadorApp.irA(Vista.DASHBOARD);
     *   ctrl.inicializar(inmobiliaria);
     */
    public static <T> T irA(Vista vista) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource(vista.getRuta())
            );
            Parent root = loader.load();

            // Reutiliza la Scene existente si ya hay una — más eficiente
            if (stagePrincipal.getScene() == null) {
                Scene scene = new Scene(root);
                scene.getStylesheets().add(
                        MainApp.class.getResource("css/styles.css").toExternalForm()
                );
                stagePrincipal.setScene(scene);
            } else {
                stagePrincipal.getScene().setRoot(root);
            }

            stagePrincipal.show();
            return loader.getController();

        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo cargar la vista: " + vista.getRuta(), e
            );
        }
    }

    /**
     * Devuelve el Stage principal. Útil para configurar título o tamaño
     * desde un controlador sin exponer el Stage globalmente.
     */
    public static Stage getStage() {
        return stagePrincipal;
    }
}
