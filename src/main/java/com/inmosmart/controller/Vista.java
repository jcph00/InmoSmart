package com.inmosmart.controller;

public enum Vista {

    // Vistas de navegación principal (reemplazo de escena completa)
    LOGIN               ("fxml/login-view.fxml"),
    REGISTRO            ("fxml/registro-view.fxml"),
    DASHBOARD           ("fxml/dashboard-view.fxml"),

    // Vistas de contenido interno del dashboard (carga en contentArea)

    HOME_CONTENT        ("fxml/home-content.fxml"),
    INMUEBLES_CONTENT   ("fxml/inmueble-view.fxml"),
    OFERTAS_CONTENT     ("fxml/oferta-view.fxml"),
    REPORTES_CONTENT    ("fxml/reporte-view.fxml"),
    RECOMENDACIONES_CONTENT ("fxml/recomendacion-view.fxml"),
    ALERTAS_CONTENT     ("fxml/alerta-view.fxml");

    private final String ruta;
    Vista(String ruta) { this.ruta = ruta; }
    public String getRuta() { return ruta; }
}
