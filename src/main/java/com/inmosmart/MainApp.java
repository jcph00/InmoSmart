package com.inmosmart;

import com.inmosmart.controller.NavegadorApp;
import com.inmosmart.controller.Vista;
import com.inmosmart.controller.LoginController;
import com.inmosmart.service.Inmobiliaria;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación InmoSmart.
 *
 * Responsabilidades:
 *  - Crear la instancia única de Inmobiliaria.
 *  - Inicializar el Stage y el navegador.
 *  - Cargar la primera vista (login).
 *  - Inyectar Inmobiliaria al primer controlador.
 *
 * No contiene lógica de negocio ni de navegación posterior.
 */
public class MainApp extends Application {

    // Instancia única del núcleo del sistema — se comparte por inyección
    private static Inmobiliaria inmobiliaria;

    @Override
    public void start(Stage stage) {
        // 1. Crear el núcleo del sistema
        inmobiliaria = new Inmobiliaria("InmoSmart");

        // 2. Inicializar el navegador con el Stage principal
        NavegadorApp.inicializar(stage);

        // 3. Configurar la ventana principal
        stage.setTitle("InmoSmart — Plataforma Inmobiliaria");
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.setResizable(true);

        // 4. Cargar la vista de login e inyectar Inmobiliaria
        LoginController loginController = NavegadorApp.irA(Vista.LOGIN);
        loginController.inicializar(inmobiliaria);

        stage.centerOnScreen();
    }

    /**
     * Acceso global a Inmobiliaria para casos donde la inyección
     * directa no es posible (ej. inicialización de datos de prueba).
     */
    public static Inmobiliaria getInmobiliaria() {
        return inmobiliaria;
    }

    public static void main(String[] args) {
        launch();
    }
}
