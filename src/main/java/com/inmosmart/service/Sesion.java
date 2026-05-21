package com.inmosmart.service;

import com.inmosmart.model.Comprador;
import com.inmosmart.model.Usuario;
import com.inmosmart.model.Vendedor;

/**
 * Mantiene el estado del usuario autenticado durante la ejecución.
 *
 * Concepto de presentación — Inmobiliaria no la conoce.
 * Los controladores la consultan para saber quién está activo.
 *
 * Patrón: estado global de aplicación (singleton de sesión).
 * No persiste entre ejecuciones.
 */
public class Sesion {

    private static Usuario usuarioActual = null;

    // Constructor privado — no se instancia, solo se usan métodos estáticos
    private Sesion() {}

    // ════════════════════════════════════════════════════════════════════════
    // API PÚBLICA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Inicia sesión con el usuario autenticado.
     */
    public static void iniciar(Usuario usuario) {
        if (usuario == null)
            throw new IllegalArgumentException("No se puede iniciar sesión con usuario nulo");
        usuarioActual = usuario;
    }

    /**
     * Cierra la sesión activa.
     */
    public static void cerrar() {
        usuarioActual = null;
    }

    /**
     * Devuelve el usuario actualmente autenticado.
     * Lanza excepción si no hay sesión activa — obliga a verificar antes de usar.
     */
    public static Usuario getUsuarioActual() {
        if (usuarioActual == null)
            throw new IllegalStateException("No hay ninguna sesión activa");
        return usuarioActual;
    }

    /**
     * Indica si hay una sesión activa.
     */
    public static boolean hayUsuarioActivo() {
        return usuarioActual != null;
    }

    /**
     * Devuelve el usuario actual como Comprador si corresponde.
     * Devuelve null si el usuario activo es un Vendedor.
     */
    public static Comprador getCompradorActual() {
        if (usuarioActual instanceof Comprador c) return c;
        return null;
    }

    /**
     * Devuelve el usuario actual como Vendedor si corresponde.
     * Devuelve null si el usuario activo es un Comprador.
     */
    public static Vendedor getVendedorActual() {
        if (usuarioActual instanceof Vendedor v) return v;
        return null;
    }

    /**
     * Indica si el usuario activo es Comprador.
     */
    public static boolean esComprador() {
        return usuarioActual instanceof Comprador;
    }

    /**
     * Indica si el usuario activo es Vendedor.
     */
    public static boolean esVendedor() {
        return usuarioActual instanceof Vendedor;
    }
}
