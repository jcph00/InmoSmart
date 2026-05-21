package com.inmosmart.model;

import com.inmosmart.model.enums.TipoOperacion;

import java.time.LocalDate;

public record Transaccion(
        String codigoTransaccion,
        Comprador comprador,
        Vendedor vendedor,
        Inmueble inmueble,
        double valorFinal,
        TipoOperacion tipoOperacion,
        LocalDate fecha
) {

    // Validación en el constructor compacto
    public Transaccion {
        if (valorFinal <= 0)
            throw new IllegalArgumentException("El valor final debe ser mayor a 0");
        if (codigoTransaccion == null || codigoTransaccion.isBlank())
            throw new IllegalArgumentException("El código de transacción no puede estar vacío");
    }

    @Override
    public String toString() {
        return String.format("Transacción [%s] | %s | Comprador: %s | Vendedor: %s | $%.0f | %s",
                codigoTransaccion, tipoOperacion,
                comprador.getNombre(), vendedor.getNombre(),
                valorFinal, fecha);
    }
}

