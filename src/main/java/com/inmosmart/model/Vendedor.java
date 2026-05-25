package com.inmosmart.model;

import com.inmosmart.interfaces.IPublicador;
import com.inmosmart.model.enums.EstadoOferta;
import com.inmosmart.model.enums.TipoUsuario;

import java.util.ArrayList;
import java.util.List;

public class Vendedor extends Usuario implements IPublicador {

    private List<Inmueble> inmueblesPropios;
    private double gananciasAcumuladas;

    public Vendedor(String id, String nombre, String identificacion,
                    String telefono, String correo,
                    String contrasena) {           // ← nuevo parámetro
        super(id, nombre, identificacion, telefono, correo,
                contrasena,                          // ← pasa a super
                TipoUsuario.VENDEDOR);
        this.inmueblesPropios    = new ArrayList<>();
        this.gananciasAcumuladas = 0.0;
    }

    // ── IPublicador ──────────────────────────────────────────────────────────
    // Solo gestiona el estado interno del vendedor.
    // Inmobiliaria es quien valida, registra y asigna puntos.

    @Override
    public void publicarInmueble(Inmueble inmueble) {
        if (inmueble == null)
            throw new IllegalArgumentException("El inmueble no puede ser nulo");
        if (!inmueblesPropios.contains(inmueble))
            inmueblesPropios.add(inmueble);
        // Sin sumarPuntos() — eso lo hace Inmobiliaria.registrarInmueble()
    }

    @Override
    public void gestionarOferta(Oferta oferta, boolean aceptar) {
        if (oferta == null) return;
        oferta.setEstado(aceptar ? EstadoOferta.ACEPTADA : EstadoOferta.RECHAZADA);
        // Sin sumarPuntos() — eso lo hace Inmobiliaria.procesarOferta()
    }

    @Override
    public double calcularGanancias() {
        return gananciasAcumuladas;
    }

    // ── Estado financiero — responsabilidad propia del vendedor ──────────────
    // Solo acumula el valor. Inmobiliaria decide cuándo llamar este método.

    public void registrarGanancia(double valor) {
        if (valor > 0) gananciasAcumuladas += valor;
        // Sin sumarPuntos() — los puntos se asignan desde Inmobiliaria
        // antes o después de llamar este método, pero nunca dentro de él
    }

    // ── Utilidades ───────────────────────────────────────────────────────────

    public void agregarInmueble(Inmueble inmueble) {
        if (inmueble != null && !inmueblesPropios.contains(inmueble))
            inmueblesPropios.add(inmueble);
    }

    public List<Inmueble> getInmueblesPropios()  { return inmueblesPropios; }
    public double getGananciasAcumuladas()        { return gananciasAcumuladas; }

    @Override
    public String toString() {
        return "Vendedor | " + super.toString() +
                String.format(" | Ganancias: $%.0f", gananciasAcumuladas);
    }
}