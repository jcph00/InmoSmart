module com.inmosmart.inmosmart {

    // ── Dependencias JavaFX ──────────────────────────────────────────────
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // ── Dependencia Gson (persistencia JSON) ─────────────────────────────
    requires com.google.gson;

    // ── Exports — acceso en compilación ──────────────────────────────────
    // Solo MainApp necesita ser visible para el launcher de JavaFX
    exports com.inmosmart;

    // ── Opens — acceso reflectivo en tiempo de ejecución ─────────────────
    // FXMLLoader necesita abrir TODOS los paquetes que contienen:
    //   · controladores referenciados desde fx:controller en FXML
    //   · campos @FXML (aunque sean private)
    //   · métodos de evento (onLogin, onBuscar, etc.)
    opens com.inmosmart.controller to javafx.fxml;

    // El modelo no tiene controladores pero Gson necesita acceso
    // reflectivo para serializar/deserializar cuando implementes persistencia
    opens com.inmosmart.model         to com.google.gson;
    opens com.inmosmart.model.enums   to com.google.gson;
    opens com.inmosmart.service       to com.google.gson;
}