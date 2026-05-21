package com.inmosmart.controller;

import com.inmosmart.model.Comprador;
import com.inmosmart.model.Inmueble;
import com.inmosmart.model.Oferta;
import com.inmosmart.model.ParametrosBusqueda;
import com.inmosmart.model.Vendedor;
import com.inmosmart.model.enums.EstadoInmueble;
import com.inmosmart.model.enums.TipoInmueble;
import com.inmosmart.service.Inmobiliaria;
import com.inmosmart.service.Sesion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.UUID;

/**
 * Controlador de la vista de inmuebles.
 * Opera en dos modos según el rol del usuario activo:
 *  - COMPRADOR: buscar y hacer ofertas.
 *  - VENDEDOR:  registrar y gestionar sus inmuebles.
 */
public class InmuebleController {

    // ── Panel comprador (búsqueda) ───────────────────────────────────────────
    @FXML private VBox        panelComprador;
    @FXML private TextField   txtCiudadFiltro;
    @FXML private ComboBox<String> cmbTipoFiltro;
    @FXML private TextField   txtPrecioMin;
    @FXML private TextField   txtPrecioMax;
    @FXML private TextField   txtAreaMin;

    // ── Panel vendedor (registro) ────────────────────────────────────────────
    @FXML private VBox        panelVendedor;
    @FXML private ComboBox<String> cmbTipoInmueble;
    @FXML private TextField   txtDireccion;
    @FXML private TextField   txtCiudad;
    @FXML private TextField   txtArea;
    @FXML private TextField   txtPrecio;
    @FXML private TextField   txtDescripcion;

    // ── Tabla compartida ─────────────────────────────────────────────────────
    @FXML private TableView<Inmueble>          tablaInmuebles;
    @FXML private TableColumn<Inmueble, String> colCodigo;
    @FXML private TableColumn<Inmueble, String> colTipo;
    @FXML private TableColumn<Inmueble, String> colCiudad;
    @FXML private TableColumn<Inmueble, String> colPrecio;
    @FXML private TableColumn<Inmueble, String> colArea;
    @FXML private TableColumn<Inmueble, String> colEstado;
    @FXML private TableColumn<Inmueble, String> colVendedor;

    // ── Acciones sobre selección ─────────────────────────────────────────────
    @FXML private Button btnAccionPrincipal;   // "Hacer oferta" o "Registrar"
    @FXML private Button btnActualizarPrecio;  // solo vendedor
    @FXML private TextField txtNuevoPrecio;    // solo vendedor

    // ── Título y estado ──────────────────────────────────────────────────────
    @FXML private Label lblTituloSeccion;
    @FXML private Label lblEstado;

    // ── Dependencia ──────────────────────────────────────────────────────────
    private Inmobiliaria inmobiliaria;

    // ════════════════════════════════════════════════════════════════════════
    // INICIALIZACIÓN
    // ════════════════════════════════════════════════════════════════════════

    public void inicializar(Inmobiliaria inmobiliaria) {
        this.inmobiliaria = inmobiliaria;

        configurarTabla();
        configurarCombos();
        configurarModoSegunRol();
        cargarDatosIniciales();
        limpiarEstado();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colTipo.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getTipo().name()));
        colCiudad.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getCiudad()));
        colPrecio.setCellValueFactory(
                c -> new SimpleStringProperty(
                        String.format("$%,.0f", c.getValue().getPrecio())));
        colArea.setCellValueFactory(
                c -> new SimpleStringProperty(
                        String.format("%.0f m²", c.getValue().getArea())));
        colEstado.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getEstado().name()));
        colVendedor.setCellValueFactory(
                c -> new SimpleStringProperty(
                        c.getValue().getVendedor().getNombre()));

        // Colorear filas según estado
        tablaInmuebles.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Inmueble item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getEstado() == EstadoInmueble.DISPONIBLE) {
                    setStyle("-fx-background-color: #f0fdf4;");
                } else if (item.getEstado() == EstadoInmueble.RESERVADO) {
                    setStyle("-fx-background-color: #fffbeb;");
                } else {
                    setStyle("-fx-background-color: #fef2f2;");
                }
            }
        });
    }

    private void configurarCombos() {
        // Filtro de búsqueda (comprador)
        cmbTipoFiltro.getItems().add("Todos");
        for (TipoInmueble tipo : TipoInmueble.values())
            cmbTipoFiltro.getItems().add(tipo.name());
        cmbTipoFiltro.setValue("Todos");

        // Tipo al registrar (vendedor)
        for (TipoInmueble tipo : TipoInmueble.values())
            cmbTipoInmueble.getItems().add(tipo.name());
        cmbTipoInmueble.setValue(TipoInmueble.CASA.name());
    }

    private void configurarModoSegunRol() {
        boolean esComprador = Sesion.esComprador();

        // Paneles superiores
        panelComprador.setVisible(esComprador);
        panelComprador.setManaged(esComprador);
        panelVendedor.setVisible(!esComprador);
        panelVendedor.setManaged(!esComprador);

        // Acciones sobre tabla
        btnActualizarPrecio.setVisible(!esComprador);
        btnActualizarPrecio.setManaged(!esComprador);
        txtNuevoPrecio.setVisible(!esComprador);
        txtNuevoPrecio.setManaged(!esComprador);

        // Título y botón principal
        if (esComprador) {
            lblTituloSeccion.setText("Buscar inmuebles");
            btnAccionPrincipal.setText("Hacer oferta");
        } else {
            lblTituloSeccion.setText("Mis inmuebles");
            btnAccionPrincipal.setText("Registrar inmueble");
        }
    }

    private void cargarDatosIniciales() {
        if (Sesion.esComprador()) {
            // Comprador ve todos los disponibles al abrir
            poblarTabla(inmobiliaria.getListaInmuebles().stream()
                    .filter(Inmueble::estaDisponible)
                    .toList());
        } else {
            // Vendedor ve solo los suyos
            poblarTabla(Sesion.getVendedorActual().getInmueblesPropios());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // EVENTOS FXML — Comprador
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void onBuscar() {
        try {
            Comprador comprador = Sesion.getCompradorActual();
            ParametrosBusqueda params = construirParametros();
            List<Inmueble> resultados =
                    inmobiliaria.buscarInmuebles(comprador, params);
            poblarTabla(resultados);
            mostrarExito("Se encontraron " + resultados.size() + " inmuebles.");
        } catch (NumberFormatException e) {
            mostrarError("Verifica que los valores de precio y área sean numéricos.");
        }
    }

    @FXML
    private void onHacerOferta() {
        Inmueble seleccionado = tablaInmuebles.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un inmueble de la tabla.");
            return;
        }
        if (!seleccionado.estaDisponible()) {
            mostrarError("Este inmueble no está disponible.");
            return;
        }

        // Diálogo simple para ingresar el valor de la oferta
        TextInputDialog dialogo = new TextInputDialog(
                String.format("%.0f", seleccionado.getPrecio()));
        dialogo.setTitle("Hacer oferta");
        dialogo.setHeaderText("Inmueble: " + seleccionado.getCodigo()
                + " — Precio: $" + String.format("%,.0f", seleccionado.getPrecio()));
        dialogo.setContentText("Tu oferta ($):");

        dialogo.showAndWait().ifPresent(valorStr -> {
            try {
                double valor = Double.parseDouble(valorStr.trim());
                Comprador comprador = Sesion.getCompradorActual();
                Oferta oferta = comprador.construirOferta(seleccionado, valor);
                inmobiliaria.recibirOferta(oferta);
                mostrarExito("Oferta enviada correctamente. El vendedor la revisará.");
                cargarDatosIniciales(); // refresca tabla (inmueble pasa a RESERVADO)
            } catch (NumberFormatException e) {
                mostrarError("Ingresa un valor numérico válido.");
            } catch (IllegalStateException | IllegalArgumentException e) {
                mostrarError(e.getMessage());
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // EVENTOS FXML — Vendedor
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void onRegistrarInmueble() {
        try {
            Vendedor vendedor = Sesion.getVendedorActual();
            Inmueble inmueble = construirInmueble(vendedor);

            inmobiliaria.registrarInmueble(inmueble, vendedor);
            inmobiliaria.publicarInmueble(inmueble, txtDescripcion.getText().trim());

            mostrarExito("Inmueble registrado y publicado correctamente.");
            limpiarFormularioVendedor();
            cargarDatosIniciales();

        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onActualizarPrecio() {
        Inmueble seleccionado = tablaInmuebles.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un inmueble para actualizar su precio.");
            return;
        }
        try {
            double nuevoPrecio = Double.parseDouble(txtNuevoPrecio.getText().trim());
            inmobiliaria.actualizarPrecioInmueble(seleccionado, nuevoPrecio);
            mostrarExito("Precio actualizado. Se generó una alerta automática.");
            txtNuevoPrecio.clear();
            cargarDatosIniciales();
        } catch (NumberFormatException e) {
            mostrarError("Ingresa un precio numérico válido.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarError(e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // UTILIDADES PRIVADAS
    // ════════════════════════════════════════════════════════════════════════

    private ParametrosBusqueda construirParametros() {
        String ciudad  = txtCiudadFiltro.getText().trim();
        String tipoStr = cmbTipoFiltro.getValue();
        TipoInmueble tipo = "Todos".equals(tipoStr)
                ? null : TipoInmueble.valueOf(tipoStr);

        double precioMin = parsearDouble(txtPrecioMin.getText(), 0);
        double precioMax = parsearDouble(txtPrecioMax.getText(), 0);
        double areaMin   = parsearDouble(txtAreaMin.getText(),   0);

        return new ParametrosBusqueda(
                ciudad.isEmpty() ? null : ciudad,
                tipo, precioMin, precioMax, areaMin
        );
    }

    private Inmueble construirInmueble(Vendedor vendedor) {
        TipoInmueble tipo = TipoInmueble.valueOf(cmbTipoInmueble.getValue());
        String direccion  = txtDireccion.getText().trim();
        String ciudad     = txtCiudad.getText().trim();
        double area       = Double.parseDouble(txtArea.getText().trim());
        double precio     = Double.parseDouble(txtPrecio.getText().trim());

        if (direccion.isEmpty() || ciudad.isEmpty())
            throw new IllegalArgumentException(
                    "Dirección y ciudad son obligatorias.");

        return new Inmueble(
                generarCodigoInmueble(tipo),
                tipo, direccion, ciudad, area, precio, vendedor
        );
    }

    private void poblarTabla(List<Inmueble> inmuebles) {
        tablaInmuebles.setItems(FXCollections.observableArrayList(inmuebles));
    }

    private double parsearDouble(String texto, double valorDefecto) {
        try {
            return texto == null || texto.isBlank()
                    ? valorDefecto
                    : Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            return valorDefecto;
        }
    }

    private String generarCodigoInmueble(TipoInmueble tipo) {
        return tipo.name().substring(0, 3).toUpperCase()
                + "-" + UUID.randomUUID().toString()
                .substring(0, 6).toUpperCase();
    }

    private void limpiarFormularioVendedor() {
        txtDireccion.clear();
        txtCiudad.clear();
        txtArea.clear();
        txtPrecio.clear();
        txtDescripcion.clear();
        cmbTipoInmueble.setValue(TipoInmueble.CASA.name());
    }

    private void mostrarError(String mensaje) {
        lblEstado.setText("⚠ " + mensaje);
        lblEstado.setStyle("-fx-text-fill: #DC2626;");
    }

    private void mostrarExito(String mensaje) {
        lblEstado.setText("✓ " + mensaje);
        lblEstado.setStyle("-fx-text-fill: #16A34A;");
    }

    private void limpiarEstado() {
        lblEstado.setText("");
    }
}
