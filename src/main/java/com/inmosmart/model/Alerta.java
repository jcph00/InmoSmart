package com.inmosmart.model;

import com.inmosmart.model.enums.TipoAlerta;
import java.time.LocalDateTime;

/**
 * Representa una notificación generada por el sistema.
 * Las alertas son inmutables una vez creadas — solo se leen, no se modifican.
 */
public class Alerta {

    private final String mensaje;
    private final TipoAlerta tipo;
    private final LocalDateTime fechaHora;
    private final String destinatarioId;
    private boolean leida;

    public Alerta(String mensaje, TipoAlerta tipo, String destinatarioId) {
        this.mensaje        = mensaje;
        this.tipo           = tipo;
        this.destinatarioId = destinatarioId;
        this.fechaHora      = LocalDateTime.now();
        this.leida          = false;
    }

    public String getMensaje()          { return mensaje; }
    public TipoAlerta getTipo()         { return tipo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getDestinatarioId()   { return destinatarioId; }
    public boolean isLeida()            { return leida; }
    public void marcarLeida()           { this.leida = true; }

    @Override
    public String toString() {
        return String.format("[%s] %s — %s%s",
                tipo, mensaje, fechaHora.toLocalDate(),
                leida ? " ✓" : "");
    }
}
