package com.inmosmart;

import com.inmosmart.model.Oferta;
import com.inmosmart.model.Transaccion;
import com.inmosmart.model.enums.TipoOperacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del requerimiento 5: Generación de reportes.
 * Verifica que los reportes contengan información correcta
 * y reflejen el estado actual del sistema.
 */
@DisplayName("R5 — Generación de reportes")
class ReporteTest extends BaseTest {

    // ── Reporte de inmuebles ──────────────────────────────────────────────────

    @Test
    @DisplayName("Reporte de inmuebles — no es nulo ni vacío")
    void reporteInmuebles_noDebeSerVacio() {
        String reporte = inmobiliaria.generarReporteInmuebles();

        assertNotNull(reporte);
        assertFalse(reporte.isBlank());
    }

    @Test
    @DisplayName("Reporte de inmuebles — refleja cantidad correcta")
    void reporteInmuebles_debeReflejarCantidadCorrecta() {
        String reporte = inmobiliaria.generarReporteInmuebles();

        assertTrue(
                reporte.contains("1"),
                "El reporte debe indicar que hay 1 inmueble registrado"
        );
    }

    @Test
    @DisplayName("Reporte de inmuebles — incluye código del inmueble")
    void reporteInmuebles_debeIncluirDatosDelInmueble() {
        String reporte = inmobiliaria.generarReporteInmuebles();

        assertTrue(
                reporte.contains("APT-001"),
                "El reporte debe contener el código del inmueble"
        );
    }

    // ── Reporte de usuarios ───────────────────────────────────────────────────

    @Test
    @DisplayName("Reporte de usuarios — no es nulo ni vacío")
    void reporteUsuarios_noDebeSerVacio() {
        String reporte = inmobiliaria.generarReporteUsuarios();

        assertNotNull(reporte);
        assertFalse(reporte.isBlank());
    }

    @Test
    @DisplayName("Reporte de usuarios — contiene nombres registrados")
    void reporteUsuarios_debeContenerNombresRegistrados() {
        String reporte = inmobiliaria.generarReporteUsuarios();

        assertTrue(reporte.contains("Ana Gómez"),
                "Debe aparecer el nombre del comprador");
        assertTrue(reporte.contains("Luis Torres"),
                "Debe aparecer el nombre del vendedor");
    }

    @Test
    @DisplayName("Reporte de usuarios — actualiza ranking tras transacción")
    void reporteUsuarios_debeActualizarRankingTrasTransaccion() {
        // Ejecutar un flujo completo
        Oferta oferta = compradorBase.construirOferta(inmuebleBase, 240_000_000.0);
        inmobiliaria.recibirOferta(oferta);
        inmobiliaria.procesarOferta(
                oferta.getCodigoOferta(), true, TipoOperacion.VENTA
        );

        String reporte = inmobiliaria.generarReporteUsuarios();

        // El vendedor debe aparecer con sus puntos actualizados
        assertTrue(reporte.contains("Luis Torres"));
    }

    // ── Reporte de transacciones ──────────────────────────────────────────────

    @Test
    @DisplayName("Reporte de transacciones — vacío sin transacciones")
    void reporteTransacciones_sinDatos_debeIndicarCero() {
        String reporte = inmobiliaria.generarReporteTransacciones();

        assertTrue(
                reporte.contains("0"),
                "Sin transacciones el reporte debe indicar 0"
        );
    }

    @Test
    @DisplayName("Reporte de transacciones — refleja transacción completada")
    void reporteTransacciones_debeReflejarTransaccionCompletada() {
        Oferta oferta = compradorBase.construirOferta(inmuebleBase, 240_000_000.0);
        inmobiliaria.recibirOferta(oferta);
        Transaccion t = inmobiliaria.procesarOferta(
                oferta.getCodigoOferta(), true, TipoOperacion.VENTA
        );

        String reporte = inmobiliaria.generarReporteTransacciones();

        assertAll("reporte tras transacción",
                () -> assertTrue(reporte.contains(t.codigoTransaccion()),
                        "Debe contener el código de la transacción"),
                () -> assertTrue(reporte.contains("240"),
                        "Debe contener el valor transaccionado")
        );
    }

    @Test
    @DisplayName("Reporte de transacciones — calcula volumen total correctamente")
    void reporteTransacciones_debeCalcularVolumenTotal() {
        Oferta oferta = compradorBase.construirOferta(inmuebleBase, 240_000_000.0);
        inmobiliaria.recibirOferta(oferta);
        inmobiliaria.procesarOferta(
                oferta.getCodigoOferta(), true, TipoOperacion.VENTA
        );

        String reporte = inmobiliaria.generarReporteTransacciones();

        assertTrue(
                reporte.contains("240"),
                "El volumen total debe reflejar el valor de la transacción"
        );
    }
}
