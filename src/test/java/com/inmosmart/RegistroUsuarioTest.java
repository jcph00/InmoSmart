package com.inmosmart;

import com.inmosmart.model.Comprador;
import com.inmosmart.model.Vendedor;
import com.inmosmart.model.enums.TipoUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del requerimiento 1: Registro de usuarios.
 * Verifica registro de compradores, vendedores,
 * validaciones de duplicados y consistencia de datos.
 */
@DisplayName("R1 — Registro de usuarios")
class RegistroUsuarioTest extends BaseTest {

    // ── Registro exitoso ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Registrar comprador — queda en la lista del sistema")
    void registrarComprador_debeQuedarEnSistema() {
        Comprador nuevo = new Comprador(
                "C-002", "María López", "1094000010",
                "3003333333", "maria@test.com"
        );
        inmobiliaria.registrarComprador(nuevo);

        assertTrue(
                inmobiliaria.getListaUsuarios().contains(nuevo),
                "El comprador debe estar en la lista de usuarios"
        );
    }

    @Test
    @DisplayName("Registrar vendedor — queda en la lista del sistema")
    void registrarVendedor_debeQuedarEnSistema() {
        Vendedor nuevo = new Vendedor(
                "V-002", "Pedro Ríos", "1094000011",
                "3004444444", "pedro@test.com"
        );
        inmobiliaria.registrarVendedor(nuevo);

        assertTrue(
                inmobiliaria.getListaUsuarios().contains(nuevo),
                "El vendedor debe estar en la lista de usuarios"
        );
    }

    @Test
    @DisplayName("Comprador registrado tiene tipo COMPRADOR")
    void comprador_debeTenerTipoCorrecto() {
        assertEquals(TipoUsuario.COMPRADOR, compradorBase.getTipoUsuario());
    }

    @Test
    @DisplayName("Vendedor registrado tiene tipo VENDEDOR")
    void vendedor_debeTenerTipoCorrecto() {
        assertEquals(TipoUsuario.VENDEDOR, vendedorBase.getTipoUsuario());
    }

    @Test
    @DisplayName("Usuario nuevo inicia con 0 puntos de reputación")
    void usuarioNuevo_debeIniciarConCeroPuntos() {
        // Usuarios creados aquí — sin pasar por ninguna acción del sistema
        Comprador compradorFresco = new Comprador(
                "C-FRESH", "Test Comprador", "9000000001",
                "3000000001", "fresh@test.com"
        );
        Vendedor vendedorFresco = new Vendedor(
                "V-FRESH", "Test Vendedor", "9000000002",
                "3000000002", "fresh2@test.com"
        );

        assertEquals(0, compradorFresco.getPuntosReputacion(),
                "Comprador recién creado debe tener 0 puntos");
        assertEquals(0, vendedorFresco.getPuntosReputacion(),
                "Vendedor recién creado debe tener 0 puntos");
    }

    @Test
    @DisplayName("Usuario nuevo inicia con rango Principiante")
    void usuarioNuevo_debeIniciarComoPrincipiante() {
        assertEquals("Principiante", compradorBase.getRango());
        assertEquals("Principiante", vendedorBase.getRango());
    }

    // ── Validaciones de duplicado ─────────────────────────────────────────────

    @Test
    @DisplayName("Registrar identificación duplicada — lanza excepción")
    void registrarIdentificacionDuplicada_debeLanzarExcepcion() {
        Comprador duplicado = new Comprador(
                "C-DUP", "Otro Nombre", "1094000001", // misma ID que compradorBase
                "3009999999", "otro@test.com"
        );

        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> inmobiliaria.registrarComprador(duplicado)
        );

        assertTrue(
                excepcion.getMessage().contains("1094000001"),
                "El mensaje debe indicar la identificación duplicada"
        );
    }

    @Test
    @DisplayName("Sistema contiene exactamente los usuarios registrados")
    void sistema_debeTenerCantidadCorrectaDeUsuarios() {
        // Setup ya registró compradorBase y vendedorBase
        assertEquals(2, inmobiliaria.getListaUsuarios().size());
    }

    // ── Reputación ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sumarPuntos — actualiza puntaje y registra evento")
    void sumarPuntos_debeActualizarPuntajeYRegistrarEvento() {
        compradorBase.sumarPuntos(50, "Compró un inmueble");

        assertEquals(50, compradorBase.getPuntosReputacion());
        assertEquals(1,  compradorBase.getHistorialReputacion().size());
        assertEquals("Compró un inmueble",
                compradorBase.getHistorialReputacion().get(0).getDescripcion());
    }

    @Test
    @DisplayName("Rango se actualiza correctamente según puntos acumulados")
    void rango_debeActualizarseSegunPuntos() {
        compradorBase.sumarPuntos(101, "Test rango Inversionista");
        assertEquals("Inversionista", compradorBase.getRango());

        compradorBase.sumarPuntos(400, "Test rango Experto");
        assertEquals("Experto Inmobiliario", compradorBase.getRango());

        compradorBase.sumarPuntos(1500, "Test rango Magnate");
        assertEquals("Magnate Inmobiliario", compradorBase.getRango());
    }
}