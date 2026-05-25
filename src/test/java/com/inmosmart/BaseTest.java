package com.inmosmart;

import com.inmosmart.model.Comprador;
import com.inmosmart.model.Inmueble;
import com.inmosmart.model.Vendedor;
import com.inmosmart.model.enums.TipoInmueble;
import com.inmosmart.service.Inmobiliaria;
import org.junit.jupiter.api.BeforeEach;

/**
 * Clase base con setup compartido para todas las pruebas.
 * Cada test hereda un sistema limpio con datos base listos.
 */
public abstract class BaseTest {

    protected Inmobiliaria inmobiliaria;
    protected Comprador    compradorBase;
    protected Vendedor     vendedorBase;
    protected Inmueble     inmuebleBase;

    @BeforeEach
    void configurarSistema() {
        // Sistema limpio en cada prueba
        inmobiliaria = new Inmobiliaria("InmoSmart Test");

        // Usuarios base
        compradorBase = new Comprador(
                "C-001", "Ana Gómez", "1094000001",
                "3001111111", "ana@test.com",
                "test123"    // ← contraseña para pruebas
        );
        vendedorBase = new Vendedor(
                "V-001", "Luis Torres", "1094000002",
                "3002222222", "luis@test.com",
                "test123"    // ← contraseña para pruebas
        );

        // Inmueble base — disponible por defecto
        inmuebleBase = new Inmueble(
                "APT-001", TipoInmueble.APARTAMENTO,
                "Calle 10 #5-20", "Armenia",
                80.0, 250_000_000.0, vendedorBase
        );

        // Registrar en el sistema
        inmobiliaria.registrarComprador(compradorBase);
        inmobiliaria.registrarVendedor(vendedorBase);
        inmobiliaria.registrarInmueble(inmuebleBase, vendedorBase);
    }
}
