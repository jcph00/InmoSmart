package com.inmosmart;

import com.inmosmart.model.Inmueble;
import com.inmosmart.model.Publicacion;
import com.inmosmart.model.Vendedor;
import com.inmosmart.model.enums.EstadoInmueble;
import com.inmosmart.model.enums.TipoInmueble;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del requerimiento 2: Publicación de inmuebles.
 * Verifica registro, publicación, estado inicial
 * y validaciones de datos del inmueble.
 */
@DisplayName("R2 — Publicación de inmuebles")
class PublicacionInmuebleTest extends BaseTest {

    // ── Registro de inmueble ──────────────────────────────────────────────────

    @Test
    @DisplayName("Inmueble registrado queda en la lista del sistema")
    void registrarInmueble_debeQuedarEnSistema() {
        assertTrue(
                inmobiliaria.getListaInmuebles().contains(inmuebleBase),
                "El inmueble debe estar en la lista del sistema"
        );
    }

    @Test
    @DisplayName("Inmueble recién registrado inicia DISPONIBLE")
    void inmuebleNuevo_debeIniciarDisponible() {
        assertEquals(EstadoInmueble.DISPONIBLE, inmuebleBase.getEstado());
    }

    @Test
    @DisplayName("Inmueble registrado asigna +10 puntos al vendedor")
    void registrarInmueble_debeAsignarPuntosAlVendedor() {
        // vendedorBase ya registró inmuebleBase en el setup
        assertEquals(10, vendedorBase.getPuntosReputacion(),
                "Publicar un inmueble debe otorgar 10 puntos al vendedor");
    }

    @Test
    @DisplayName("Inmueble aparece en la lista propia del vendedor")
    void registrarInmueble_debeAgregarseAlVendedor() {
        assertTrue(
                vendedorBase.getInmueblesPropios().contains(inmuebleBase),
                "El inmueble debe estar en la lista del vendedor"
        );
    }

    // ── Publicación ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Publicar inmueble — crea publicación en el sistema")
    void publicarInmueble_debeCrearPublicacion() {
        Publicacion pub = inmobiliaria.publicarInmueble(
                inmuebleBase, "Hermoso apartamento en Armenia"
        );

        assertNotNull(pub);
        assertTrue(inmobiliaria.getListaPublicaciones().contains(pub));
        assertEquals(inmuebleBase, pub.getInmueble());
    }

    @Test
    @DisplayName("Publicar inmueble no disponible — lanza excepción")
    void publicarInmuebleNoDisponible_debeLanzarExcepcion() {
        inmuebleBase.setEstado(EstadoInmueble.VENDIDO);

        assertThrows(
                IllegalStateException.class,
                () -> inmobiliaria.publicarInmueble(inmuebleBase, "Desc")
        );
    }

    // ── Validaciones de datos ─────────────────────────────────────────────────

    @Test
    @DisplayName("Crear inmueble con precio cero — lanza excepción")
    void crearInmueble_conPrecioCero_debeLanzarExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Inmueble("ERR-001", TipoInmueble.CASA,
                        "Calle falsa", "Armenia",
                        60.0, 0.0, vendedorBase)
        );
    }

    @Test
    @DisplayName("Crear inmueble con área cero — lanza excepción")
    void crearInmueble_conAreaCero_debeLanzarExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Inmueble("ERR-002", TipoInmueble.CASA,
                        "Calle falsa", "Armenia",
                        0.0, 200_000_000.0, vendedorBase)
        );
    }

    @Test
    @DisplayName("Registrar inmueble con vendedor no registrado — lanza excepción")
    void registrarInmueble_vendedorNoRegistrado_debeLanzarExcepcion() {
        Vendedor vendedorExterno = new Vendedor(
                "V-EXT", "Externo", "9999999999",
                "3000000000", "ext@test.com", "extpass"
        );
        Inmueble inmuebleExterno = new Inmueble(
                "EXT-001", TipoInmueble.TERRENO,
                "Vereda", "Calarcá", 200.0,
                50_000_000.0, vendedorExterno
        );

        assertThrows(
                IllegalStateException.class,
                () -> inmobiliaria.registrarInmueble(inmuebleExterno, vendedorExterno)
        );
    }

    // ── Tipos de inmueble ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Sistema soporta todos los tipos de inmueble")
    void sistema_debeSoportarTodosLosTipos() {
        for (TipoInmueble tipo : TipoInmueble.values()) {
            Inmueble i = new Inmueble(
                    "TEST-" + tipo.name(), tipo,
                    "Dirección test", "Armenia",
                    50.0, 100_000_000.0, vendedorBase
            );
            assertDoesNotThrow(
                    () -> inmobiliaria.registrarInmueble(i, vendedorBase),
                    "Debe poder registrarse un inmueble de tipo " + tipo
            );
        }
    }
}
