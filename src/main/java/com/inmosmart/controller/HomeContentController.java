package com.inmosmart.controller;

import com.inmosmart.model.EventoReputacion;
import com.inmosmart.model.Usuario;
import com.inmosmart.service.Inmobiliaria;
import com.inmosmart.service.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HomeContentController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblFecha;
    @FXML private Label lblTotalInmuebles;
    @FXML private Label lblTotalUsuarios;
    @FXML private Label lblTotalTransacciones;
    @FXML private Label lblReputacion;
    @FXML private Label lblRango;
    @FXML private ListView<String> lstHistorialReputacion;

    public void inicializar(Inmobiliaria inmobiliaria) {
        Usuario usuario = Sesion.getUsuarioActual();

        lblBienvenida.setText("Bienvenido, " + usuario.getNombre());
        lblFecha.setText(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy",
                        new java.util.Locale("es", "CO"))));

        lblTotalInmuebles.setText(
                String.valueOf(inmobiliaria.getListaInmuebles().size()));
        lblTotalUsuarios.setText(
                String.valueOf(inmobiliaria.getListaUsuarios().size()));
        lblTotalTransacciones.setText(
                String.valueOf(inmobiliaria.getListaTransacciones().size()));

        lblReputacion.setText(String.valueOf(usuario.getPuntosReputacion()));
        lblRango.setText(usuario.getRango());

        cargarHistorialReputacion(usuario);
    }

    private void cargarHistorialReputacion(Usuario usuario) {
        lstHistorialReputacion.getItems().clear();

        if (usuario.getHistorialReputacion().isEmpty()) {
            lstHistorialReputacion.getItems().add("Sin actividad registrada aún.");
            return;
        }

        usuario.getHistorialReputacion().stream()
                .map(EventoReputacion::toString)
                .forEach(lstHistorialReputacion.getItems()::add);
    }
}
