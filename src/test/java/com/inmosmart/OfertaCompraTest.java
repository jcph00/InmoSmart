package com.inmosmart;

import com.inmosmart.model.Oferta;
import com.inmosmart.model.enums.EstadoInmueble;
import com.inmosmart.model.enums.EstadoOferta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del requerimiento 3: Realización de ofertas.
 * Verifica construcción, recepción, validaciones
 * y comportamiento de estados de oferta.
 */
@DisplayName("R3 — Realización de ofertas")
class OfertaCompraTest extends BaseTest {

    // ── Construcción y registro ───────────────────────────────────────────────

    @Test
    @DisplayName("Oferta recibida queda en la lista del sistema")
    void recibirOferta_debeQuedarEnSistema() {
        Oferta oferta = compradorBase.construirOferta(inmuebleBase, 230_000_000.0);
        inmobiliaria.recibirOferta(oferta);

        assertTrue(
                inmobiliaria.getListaOfertas().contains(oferta),
                "La oferta debe estar en la lista del sistema"
        );
    }

    @Test
    @DisplayName("Oferta recibida queda en la lista del comprador")
    void recibirOferta_debeQuedarEnComprador() {
        Oferta oferta = compradorBase.construirOferta(inmuebleBase, 230_000_000.0);
        inmobiliaria.recibirOferta(oferta);

        assertTrue(
                compradorBase.getOfertasRealizadas().contains(oferta),
                "La oferta debe estar en la lista del comprador"
        );
    }

    @Test
    @DisplayName("Oferta nueva inicia en estado PENDIENTE")
    void ofertaNueva_debeIniciarPendiente() {
        Oferta oferta = compradorBase.construirOferta(inmuebleBase, 230_000_000.0);
        assertEquals(EstadoOferta.PENDIENTE, oferta.getEstado());
    }

    @Test
    @DisplayName("Recibir oferta — inmueble pasa a RESERVADO")
    void recibirOferta_debeReservarInmueble() {
        Oferta oferta = compradorBase.construirOferta(inmuebleBase, 230_000_000.0);
        inmobiliaria.recibirOferta(oferta);

        assertEquals(
                EstadoInmueble.RESERVADO, inmuebleBase.getEstado(),
                "El inmueble debe pasar a RESERVADO al recibir una oferta"
        );
    }

    @Test
    @DisplayName("Recibir oferta — comprador obtiene +5 puntos")
    void recibirOferta_debeAsignarPuntosAlComprador() {
        int puntosAntes = compradorBase.getPuntosReputacion();
        Oferta oferta   = compradorBase.construirOferta(inmuebleBase, 230_000_000.0);
        inmobiliaria.recibirOferta(oferta);

        assertEquals(
                puntosAntes + 5,
                compradorBase.getPuntosReputacion(),
                "Realizar una oferta debe otorgar 5 puntos al comprador"
        );
    }

    // ── Validaciones ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Oferta con valor cero — lanza excepción")
    void oferta_conValorCero_debeLanzarExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> compradorBase.construirOferta(inmuebleBase, 0.0)
        );
    }

    @Test
    @DisplayName("Oferta sobre inmueble no disponible — lanza excepción")
    void oferta_sobreInmuebleNoDisponible_debeLanzarExcepcion() {
        // Primera oferta reserva el inmueble
        Oferta primera = compradorBase.construirOferta(inmuebleBase, 230_000_000.0);
        inmobiliaria.recibirOferta(primera);

        // Segunda oferta sobre el mismo inmueble ya reservado
        Oferta segunda = compradorBase.construirOferta(inmuebleBase, 240_000_000.0);

        assertThrows(
                IllegalStateException.class,
                () -> inmobiliaria.recibirOferta(segunda),
                "No se puede ofertar sobre un inmueble reservado"
        );
    }

    @Test
    @DisplayName("Oferta de comprador no registrado — lanza excepción")
    void oferta_compradorNoRegistrado_debeLanzarExcepcion() {
        com.inmosmart.model.Comprador externo = new com.inmosmart.model.Comprador(
                "C-EXT", "Externo", "9999999998",
                "3000000001", "ext2@test.com", "test123"
        );
        Oferta oferta = externo.construirOferta(inmuebleBase, 200_000_000.0);

        assertThrows(
                IllegalStateException.class,
                () -> inmobiliaria.recibirOferta(oferta)
        );
    }

    // ── Cálculo de ahorro ─────────────────────────────────────────────────────

    @Test
    @DisplayName("calcularAhorro — devuelve diferencia correcta")
    void calcularAhorro_debeRetornarDiferenciaCorrecta() {
        Oferta oferta = compradorBase.construirOferta(inmuebleBase, 230_000_000.0);
        double ahorro = compradorBase.calcularAhorro(oferta);

        assertEquals(
                20_000_000.0, ahorro, 0.01,
                "El ahorro debe ser precio base - valor ofertado"
        );
    }
}
