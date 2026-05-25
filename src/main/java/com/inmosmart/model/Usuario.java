package com.inmosmart.model;

import java.util.ArrayList;
import java.util.List;
import com.inmosmart.model.enums.TipoUsuario;

public abstract class Usuario {

    private String id;
    private String nombre;
    private String identificacion;
    private String telefono;
    private String correo;
    private String contrasena;           // ← nuevo atributo
    private TipoUsuario tipoUsuario;
    private int puntosReputacion;
    private String rango;
    private List<EventoReputacion> historialReputacion;

    // Constructor actualizado — recibe contrasena
    public Usuario(String id, String nombre, String identificacion,
                   String telefono, String correo,
                   String contrasena,              // ← nuevo parámetro
                   TipoUsuario tipoUsuario) {

        if (contrasena == null || contrasena.length() < 6)
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 6 caracteres");

        this.id                  = id;
        this.nombre              = nombre;
        this.identificacion      = identificacion;
        this.telefono            = telefono;
        this.correo              = correo;
        this.contrasena          = contrasena;
        this.tipoUsuario         = tipoUsuario;
        this.puntosReputacion    = 0;
        this.rango               = "Principiante";
        this.historialReputacion = new ArrayList<>();
    }

    // ── Verificación — sin getter público de la contraseña ───────────────────
    /**
     * Verifica si el intento coincide con la contraseña registrada.
     * La contraseña nunca se expone fuera de Usuario.
     */
    public boolean verificarContrasena(String intento) {
        if (intento == null) return false;
        return this.contrasena.equals(intento);
    }

    // ── Getters ──────────────────────────────────────────────────
    public String getId()                  { return id; }
    public String getNombre()              { return nombre; }
    public String getIdentificacion()      { return identificacion; }
    public String getTelefono()            { return telefono; }
    public String getCorreo()              { return correo; }
    public TipoUsuario getTipoUsuario()    { return tipoUsuario; }
    public int getPuntosReputacion()       { return puntosReputacion; }
    public String getRango()               { return rango; }
    public List<EventoReputacion> getHistorialReputacion() { return historialReputacion; }

    // ── Setters controlados ──────────────────────────────────────
    public void setTelefono(String telefono) {
        if (telefono != null && !telefono.isBlank())
            this.telefono = telefono;
    }
    public void setCorreo(String correo) {
        if (correo != null && correo.contains("@"))
            this.correo = correo;
    }

    // ── Lógica de reputación ─────────────────────────────────────
    public void sumarPuntos(int puntos, String descripcion) {
        if (puntos > 0) {
            this.puntosReputacion += puntos;
            this.historialReputacion.add(new EventoReputacion(descripcion, puntos));
            actualizarRango();
        }
    }

    public void actualizarRango() {
        if      (puntosReputacion <= 100)  rango = "Principiante";
        else if (puntosReputacion <= 500)  rango = "Inversionista";
        else if (puntosReputacion <= 2000) rango = "Experto Inmobiliario";
        else                               rango = "Magnate Inmobiliario";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | ID: %s | Rango: %s | Puntos: %d",
                tipoUsuario, nombre, identificacion, rango, puntosReputacion);
    }
}