package com.inmosmart.service;

import com.inmosmart.model.Alerta;
import com.inmosmart.model.enums.TipoAlerta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestiona el ciclo de vida de las alertas en memoria.
 * Crea, almacena y entrega alertas por destinatario.
 *
 * No decide cuándo generar alertas — eso lo hace Inmobiliaria.
 * No envía notificaciones externas — solo administra la bandeja interna.
 */
public class AlertaService {

    private final List<Alerta> alertas;

    public AlertaService() {
        this.alertas = new ArrayList<>();
    }

    // ════════════════════════════════════════════════════════════════════════
    // API PÚBLICA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Crea y almacena una nueva alerta para un destinatario.
     */
    public void generarAlerta(String mensaje, TipoAlerta tipo,
                              String destinatarioId) {
        if (mensaje == null || mensaje.isBlank())
            throw new IllegalArgumentException("El mensaje no puede estar vacío");
        if (destinatarioId == null || destinatarioId.isBlank())
            throw new IllegalArgumentException("El destinatario no puede estar vacío");

        alertas.add(new Alerta(mensaje, tipo, destinatarioId));
    }

    /**
     * Devuelve todas las alertas (leídas y no leídas) de un usuario.
     */
    public List<Alerta> getAlertasPorUsuario(String destinatarioId) {
        if (destinatarioId == null) return Collections.emptyList();

        return alertas.stream()
                .filter(a -> a.getDestinatarioId().equals(destinatarioId))
                .toList();
    }

    /**
     * Devuelve solo las alertas no leídas de un usuario.
     */
    public List<Alerta> getAlertasNoLeidas(String destinatarioId) {
        return getAlertasPorUsuario(destinatarioId).stream()
                .filter(a -> !a.isLeida())
                .toList();
    }

    /**
     * Marca todas las alertas de un usuario como leídas.
     */
    public void marcarTodasLeidas(String destinatarioId) {
        getAlertasPorUsuario(destinatarioId)
                .forEach(Alerta::marcarLeida);
    }

    /**
     * Elimina todas las alertas leídas de un usuario.
     * Útil para limpiar la bandeja sin perder las pendientes.
     */
    public void limpiarAlertasLeidas(String destinatarioId) {
        alertas.removeIf(a ->
                a.getDestinatarioId().equals(destinatarioId) && a.isLeida());
    }

    /**
     * Cuenta alertas no leídas de un usuario.
     * Útil para mostrar un badge de notificaciones en la UI.
     */
    public int contarNoLeidas(String destinatarioId) {
        return (int) alertas.stream()
                .filter(a -> a.getDestinatarioId().equals(destinatarioId)
                        && !a.isLeida())
                .count();
    }
}
