package com.inmosmart.service;

import com.inmosmart.interfaces.IReportable;
import com.inmosmart.model.*;
import com.inmosmart.model.enums.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Núcleo coordinador del sistema InmoSmart.
 *
 * Responsabilidades:
 *  - Administrar las listas maestras del sistema.
 *  - Orquestar flujos de negocio multi-entidad.
 *  - Aplicar validaciones transversales.
 *  - Asignar puntos de reputación.
 *  - Generar reportes agregados.
 *
 * No responsable de:
 *  - Lógica interna de entidades.
 *  - Algoritmo de recomendaciones (RecomendacionService).
 *  - Gestión de alertas (AlertaService).
 *  - Estado de sesión (Sesion).
 */
public class Inmobiliaria implements IReportable {

    private final String nombre;

    // ── Listas maestras — solo Inmobiliaria las modifica ────────────────────
    private final List<Usuario>     listaUsuarios;
    private final List<Inmueble>    listaInmuebles;
    private final List<Publicacion> listaPublicaciones;
    private final List<Oferta>      listaOfertas;
    private final List<Transaccion> listaTransacciones;

    // ── Servicios auxiliares ─────────────────────────────────────────────────
    private final AlertaService alertaService;

    public Inmobiliaria(String nombre) {
        this.nombre             = nombre;
        this.listaUsuarios      = new ArrayList<>();
        this.listaInmuebles     = new ArrayList<>();
        this.listaPublicaciones = new ArrayList<>();
        this.listaOfertas       = new ArrayList<>();
        this.listaTransacciones = new ArrayList<>();
        this.alertaService      = new AlertaService();
    }

    // ════════════════════════════════════════════════════════════════════════
    // REGISTRO DE USUARIOS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Registra un comprador en el sistema.
     * Valida que no exista otro usuario con la misma identificación.
     */
    public void registrarComprador(Comprador comprador) {
        validarUsuarioNuevo(comprador);
        listaUsuarios.add(comprador);
    }

    /**
     * Registra un vendedor en el sistema.
     * Valida que no exista otro usuario con la misma identificación.
     */
    public void registrarVendedor(Vendedor vendedor) {
        validarUsuarioNuevo(vendedor);
        listaUsuarios.add(vendedor);
    }

    // ════════════════════════════════════════════════════════════════════════
    // GESTIÓN DE INMUEBLES
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Registra un inmueble en el sistema y lo asigna al vendedor.
     * Asigna +10 puntos al vendedor por publicar.
     */
    public void registrarInmueble(Inmueble inmueble, Vendedor vendedor) {
        if (inmueble == null)
            throw new IllegalArgumentException("El inmueble no puede ser nulo");
        if (!listaUsuarios.contains(vendedor))
            throw new IllegalStateException("El vendedor no está registrado en el sistema");

        listaInmuebles.add(inmueble);
        vendedor.publicarInmueble(inmueble);
        vendedor.sumarPuntos(10, "Publicó el inmueble: " + inmueble.getCodigo());
    }

    /**
     * Crea y registra una publicación para un inmueble ya registrado.
     * La publicación hace visible el inmueble en la plataforma.
     */
    public Publicacion publicarInmueble(Inmueble inmueble, String descripcion) {
        if (!listaInmuebles.contains(inmueble))
            throw new IllegalStateException("El inmueble no está registrado en el sistema");
        if (!inmueble.estaDisponible())
            throw new IllegalStateException("Solo se pueden publicar inmuebles disponibles");

        Publicacion publicacion = new Publicacion(
                generarCodigo("PUB"), descripcion, inmueble
        );
        listaPublicaciones.add(publicacion);
        return publicacion;
    }

    /**
     * Actualiza el precio de un inmueble y genera una alerta automática.
     */
    public void actualizarPrecioInmueble(Inmueble inmueble, double nuevoPrecio) {
        if (!listaInmuebles.contains(inmueble))
            throw new IllegalStateException("El inmueble no está registrado");

        inmueble.setPrecio(nuevoPrecio);

        alertaService.generarAlerta(
                "El inmueble " + inmueble.getCodigo() + " cambió su precio a $"
                        + String.format("%.0f", nuevoPrecio),
                TipoAlerta.PRECIO_MODIFICADO,
                inmueble.getVendedor().getId()
        );
    }

    // ════════════════════════════════════════════════════════════════════════
    // GESTIÓN DE OFERTAS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Registra una oferta sobre un inmueble.
     * Valida: comprador registrado, inmueble disponible, valor > 0.
     * Asigna +5 puntos al comprador por realizar la oferta.
     */
    public void recibirOferta(Oferta oferta) {
        validarOferta(oferta);

        listaOfertas.add(oferta);
        oferta.getComprador().construirOferta(
                oferta.getInmueble(), oferta.getValorOferta());
        oferta.getComprador().sumarPuntos(5, "Realizó oferta sobre: "
                + oferta.getInmueble().getCodigo());

        // Reserva el inmueble mientras la oferta está pendiente
        oferta.getInmueble().setEstado(EstadoInmueble.RESERVADO);
    }

    /**
     * Procesa la decisión del vendedor sobre una oferta.
     *
     * Si acepta:
     *   - Cambia estado de la oferta a ACEPTADA.
     *   - Cambia estado del inmueble a VENDIDO o ARRENDADO.
     *   - Registra la ganancia del vendedor.
     *   - Asigna puntos a comprador (+50) y vendedor (+100).
     *   - Crea la Transaccion inmutable.
     *   - Genera alerta al comprador.
     *
     * Si rechaza:
     *   - Cambia estado de la oferta a RECHAZADA.
     *   - Devuelve el inmueble a DISPONIBLE.
     *   - Genera alerta al comprador.
     *
     * @return Transaccion creada si se aceptó, null si se rechazó.
     */
    public Transaccion procesarOferta(String codigoOferta,
                                      boolean aceptar,
                                      TipoOperacion tipoOperacion) {
        Oferta oferta = buscarOfertaPorCodigo(codigoOferta)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe oferta con código: " + codigoOferta));

        if (oferta.getEstado() != EstadoOferta.PENDIENTE)
            throw new IllegalStateException("La oferta ya fue procesada anteriormente");

        Vendedor vendedor = oferta.getInmueble().getVendedor();
        Comprador comprador = oferta.getComprador();

        if (!aceptar) {
            vendedor.gestionarOferta(oferta, false);
            oferta.getInmueble().setEstado(EstadoInmueble.DISPONIBLE);

            alertaService.generarAlerta(
                    "Tu oferta sobre " + oferta.getInmueble().getCodigo() + " fue rechazada",
                    TipoAlerta.OFERTA_RECHAZADA,
                    comprador.getId()
            );
            return null;
        }

        // ── Flujo de aceptación ──────────────────────────────────────────
        vendedor.gestionarOferta(oferta, true);

        EstadoInmueble nuevoEstado = (tipoOperacion == TipoOperacion.VENTA)
                ? EstadoInmueble.VENDIDO
                : EstadoInmueble.ARRENDADO;
        oferta.getInmueble().setEstado(nuevoEstado);

        vendedor.registrarGanancia(oferta.getValorOferta());

        comprador.sumarPuntos(50, "Compró el inmueble: "
                + oferta.getInmueble().getCodigo());
        vendedor.sumarPuntos(100, "Completó transacción sobre: "
                + oferta.getInmueble().getCodigo());

        Transaccion transaccion = new Transaccion(
                generarCodigo("TRX"),
                comprador,
                vendedor,
                oferta.getInmueble(),
                oferta.getValorOferta(),
                tipoOperacion,
                LocalDate.now()
        );
        listaTransacciones.add(transaccion);

        alertaService.generarAlerta(
                "¡Tu oferta sobre " + oferta.getInmueble().getCodigo()
                        + " fue aceptada! Valor: $"
                        + String.format("%.0f", oferta.getValorOferta()),
                TipoAlerta.OFERTA_ACEPTADA,
                comprador.getId()
        );

        return transaccion;
    }

    // ════════════════════════════════════════════════════════════════════════
    // BÚSQUEDA DE INMUEBLES
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Filtra inmuebles disponibles según los parámetros de búsqueda.
     * Registra la búsqueda en el historial del comprador para recomendaciones.
     */
    public List<Inmueble> buscarInmuebles(Comprador comprador,
                                          ParametrosBusqueda params) {
        if (params == null)
            throw new IllegalArgumentException("Los parámetros de búsqueda no pueden ser nulos");

        comprador.registrarBusqueda(params);

        return listaInmuebles.stream()
                .filter(Inmueble::estaDisponible)
                .filter(i -> params.getCiudad() == null
                        || i.getCiudad().equalsIgnoreCase(params.getCiudad()))
                .filter(i -> params.getTipo() == null
                        || i.getTipo() == params.getTipo())
                .filter(i -> i.getPrecio() >= params.getPrecioMin())
                .filter(i -> params.getPrecioMax() <= 0
                        || i.getPrecio() <= params.getPrecioMax())
                .filter(i -> i.getArea() >= params.getAreaMin())
                .toList();
    }

    // ════════════════════════════════════════════════════════════════════════
    // REPORTES — IReportable
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Reporte de inmuebles: disponibles, vendidos y arrendados con detalles.
     */
    @Override
    public String generarReporteInmuebles() {
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════\n");
        sb.append("       REPORTE DE INMUEBLES\n");
        sb.append("══════════════════════════════════════\n");
        sb.append(String.format("Total registrados : %d%n", listaInmuebles.size()));
        sb.append(String.format("Disponibles       : %d%n", contarPorEstado(EstadoInmueble.DISPONIBLE)));
        sb.append(String.format("Vendidos          : %d%n", contarPorEstado(EstadoInmueble.VENDIDO)));
        sb.append(String.format("Arrendados        : %d%n", contarPorEstado(EstadoInmueble.ARRENDADO)));
        sb.append(String.format("Reservados        : %d%n", contarPorEstado(EstadoInmueble.RESERVADO)));
        sb.append("──────────────────────────────────────\n");

        sb.append("Ciudad con mayor demanda: ")
                .append(ciudadConMayorDemanda()).append("\n");

        sb.append("──────────────────────────────────────\n");
        sb.append("Detalle de inmuebles:\n");
        listaInmuebles.forEach(i -> sb.append("  • ").append(i).append("\n"));

        return sb.toString();
    }

    /**
     * Reporte de usuarios: compradores, vendedores, ranking por reputación.
     */
    @Override
    public String generarReporteUsuarios() {
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════\n");
        sb.append("        REPORTE DE USUARIOS\n");
        sb.append("══════════════════════════════════════\n");
        sb.append(String.format("Total usuarios    : %d%n", listaUsuarios.size()));
        sb.append(String.format("Compradores       : %d%n",
                listaUsuarios.stream().filter(u -> u instanceof Comprador).count()));
        sb.append(String.format("Vendedores        : %d%n",
                listaUsuarios.stream().filter(u -> u instanceof Vendedor).count()));
        sb.append("──────────────────────────────────────\n");
        sb.append("Ranking por reputación:\n");

        listaUsuarios.stream()
                .sorted(Comparator.comparingInt(Usuario::getPuntosReputacion).reversed())
                .forEach(u -> sb.append(String.format("  • %-20s %4d pts — %s%n",
                        u.getNombre(), u.getPuntosReputacion(), u.getRango())));

        return sb.toString();
    }

    /**
     * Reporte de transacciones: historial completo con valor total transaccionado.
     */
    @Override
    public String generarReporteTransacciones() {
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════\n");
        sb.append("     REPORTE DE TRANSACCIONES\n");
        sb.append("══════════════════════════════════════\n");
        sb.append(String.format("Total transacciones: %d%n", listaTransacciones.size()));

        double totalTransaccionado = listaTransacciones.stream()
                .mapToDouble(Transaccion::valorFinal)
                .sum();
        sb.append(String.format("Valor total        : $%.0f%n", totalTransaccionado));
        sb.append("──────────────────────────────────────\n");

        listaTransacciones.forEach(t -> sb.append("  • ").append(t).append("\n"));

        return sb.toString();
    }

    // ════════════════════════════════════════════════════════════════════════
    // ACCESO A DATOS — getters de solo lectura
    // ════════════════════════════════════════════════════════════════════════

    public String getNombre()                          { return nombre; }
    public List<Usuario> getListaUsuarios()            { return List.copyOf(listaUsuarios); }
    public List<Inmueble> getListaInmuebles()          { return List.copyOf(listaInmuebles); }
    public List<Publicacion> getListaPublicaciones()   { return List.copyOf(listaPublicaciones); }
    public List<Oferta> getListaOfertas()              { return List.copyOf(listaOfertas); }
    public List<Transaccion> getListaTransacciones()   { return List.copyOf(listaTransacciones); }
    public AlertaService getAlertaService()            { return alertaService; }

    // ════════════════════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS — lógica interna de soporte
    // ════════════════════════════════════════════════════════════════════════

    private void validarUsuarioNuevo(Usuario usuario) {
        if (usuario == null)
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        boolean yaExiste = listaUsuarios.stream()
                .anyMatch(u -> u.getIdentificacion()
                        .equals(usuario.getIdentificacion()));
        if (yaExiste)
            throw new IllegalStateException(
                    "Ya existe un usuario con identificación: "
                            + usuario.getIdentificacion());
    }

    private void validarOferta(Oferta oferta) {
        if (oferta == null)
            throw new IllegalArgumentException("La oferta no puede ser nula");
        if (!listaUsuarios.contains(oferta.getComprador()))
            throw new IllegalStateException("El comprador no está registrado en el sistema");
        if (!listaInmuebles.contains(oferta.getInmueble()))
            throw new IllegalStateException("El inmueble no está registrado en el sistema");
        if (!oferta.getInmueble().estaDisponible())
            throw new IllegalStateException("El inmueble no está disponible");
        if (oferta.getValorOferta() <= 0)
            throw new IllegalArgumentException("El valor de la oferta debe ser mayor a 0");
    }

    private Optional<Oferta> buscarOfertaPorCodigo(String codigo) {
        return listaOfertas.stream()
                .filter(o -> o.getCodigoOferta().equals(codigo))
                .findFirst();
    }

    private long contarPorEstado(EstadoInmueble estado) {
        return listaInmuebles.stream()
                .filter(i -> i.getEstado() == estado)
                .count();
    }

    private String ciudadConMayorDemanda() {
        return listaOfertas.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        o -> o.getInmueble().getCiudad(),
                        java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("Sin datos");
    }

    /**
     * Busca un usuario por identificación y tipo de rol.
     * Usado por LoginController para autenticar sin contraseña.
     */
    public Optional<Usuario> buscarUsuarioPorIdentificacion(String identificacion,
                                                            TipoUsuario tipo) {
        return listaUsuarios.stream()
                .filter(u -> u.getIdentificacion().equals(identificacion)
                        && u.getTipoUsuario() == tipo)
                .findFirst();
    }

    private String generarCodigo(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
