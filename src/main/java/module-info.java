module com.inmosmart.inmosmart {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.inmosmart.inmosmart to javafx.fxml;
    exports com.inmosmart.inmosmart;
}