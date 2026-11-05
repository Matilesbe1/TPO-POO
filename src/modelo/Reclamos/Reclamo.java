package ar.aromos.modelo.reclamos;

import modelo.lotes.Lote;
import modelo.personas.Vecino;
import modelo.personas.EmpleadoMantenimiento;
import modelo.excepciones.*;
import persistencia.Identificable;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Reclamo implements Identificable, Serializable {
    private final String id;
    private final Lote lote;
    private final Vecino solicitante;
    private final TipoReclamo tipo;
    private final Prioridad prioridad;
    private final String descripcion;
    private EstadoReclamo estado;
    private final LocalDateTime fechaCreacion;
    private EmpleadoMantenimiento responsable;
    private final List<HistorialEstado> historial = new ArrayList<>();
    private final List<Comentario> comentarios = new ArrayList<>();

    public Reclamo(Lote lote, Vecino solicitante, TipoReclamo tipo, String descripcion, LocalDateTime fecha) {
        if (lote == null || solicitante == null || tipo == null || descripcion == null || descripcion.isBlank() || fecha == null)
            throw new IllegalArgumentException("Los datos del reclamo son obligatorios.");
        this.id = lote.getNumero() + "-" + fecha.toString();
        this.lote = lote; this.solicitante = solicitante; this.tipo = tipo;
        this.prioridad = tipo.getPrioridad(); this.descripcion = descripcion;
        this.fechaCreacion = fecha; this.estado = EstadoReclamo.ABIERTO;
        historial.add(new HistorialEstado(estado, "Reclamo presentado", fecha));
    }
    @Override public String getId() { return id; }
    public Lote getLote() { return lote; }
    public Vecino getSolicitante() { return solicitante; }
    public TipoReclamo getTipo() { return tipo; }
    public Prioridad getPrioridad() { return prioridad; }
    public String getDescripcion() { return descripcion; }
    public EstadoReclamo getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public EmpleadoMantenimiento getResponsable() { return responsable; }
    public List<HistorialEstado> getHistorial() { return new ArrayList<>(historial); }
    public List<Comentario> getComentarios() { return new ArrayList<>(comentarios); }

    public void cambiarEstado(EstadoReclamo nuevo, String observacion, LocalDateTime fecha)
            throws TransicionInvalidaException {
        if (nuevo == null || fecha == null) throw new IllegalArgumentException("Estado y fecha son obligatorios.");
        if (nuevo != estado.siguiente() || estado == EstadoReclamo.CERRADO)
            throw new TransicionInvalidaException();
        estado = nuevo;
        historial.add(new HistorialEstado(nuevo, observacion, fecha));
    }
    public void agregarComentario(String autor, String texto, LocalDateTime fecha) {
        comentarios.add(new Comentario(autor, texto, fecha));
    }
    public void asignarResponsable(EmpleadoMantenimiento empleado, LocalDateTime fecha)
            throws TransicionInvalidaException {
        if (responsable != null && !responsable.getLegajo().equals(empleado.getLegajo()))
            throw new TransicionInvalidaException("El reclamo ya tiene un responsable asignado.");
        responsable = empleado;
        if (estado == EstadoReclamo.ABIERTO)
            cambiarEstado(EstadoReclamo.EN_CURSO, "Reclamo asignado", fecha);
    }
    public void reabrir(LocalDateTime fecha) throws ReglaNegocioException {
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria.");
        if (estado != EstadoReclamo.RESUELTO) throw new ReclamoNoReabribleException();
        HistorialEstado ultimo = historial.get(historial.size() - 1);
        long dias = Duration.between(ultimo.getFecha(), fecha).toDays();
        if (dias > 5) {
            estado = EstadoReclamo.CERRADO;
            historial.add(new HistorialEstado(estado, "Cierre por plazo vencido", fecha));
            throw new PlazoVencidoException();
        }
        estado = EstadoReclamo.ABIERTO;
        historial.add(new HistorialEstado(estado, "Reabierto por el vecino", fecha));
    }
    public void cerrarSiVencido(LocalDateTime fecha) {
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria.");
        // El documento no especifica el plazo de cierre automático: queda definido en 5 días desde RESUELTO.
        if (estado == EstadoReclamo.RESUELTO) {
            HistorialEstado ultimo = historial.get(historial.size() - 1);
            if (Duration.between(ultimo.getFecha(), fecha).toDays() > 5) {
                estado = EstadoReclamo.CERRADO;
                historial.add(new HistorialEstado(estado, "Cierre automático por vencimiento", fecha));
            }
        }
    }
    public void escalar() {
        if (estado == EstadoReclamo.ABIERTO && prioridad == Prioridad.ALTA) {
            // El escalamiento queda reflejado como observación sin alterar el estado.
            historial.add(new HistorialEstado(estado, "Reclamo escalado", fechaCreacion.plusHours(25)));
        }
    }
    public boolean debeEscalarse(LocalDateTime fechaHora) {
        return prioridad == Prioridad.ALTA && estado == EstadoReclamo.ABIERTO
                && Duration.between(fechaCreacion, fechaHora).toHours() >= 25;
    }
    public static Comparator<Reclamo> comparadorPorPrioridadYFecha() {
        return Comparator.comparing(Reclamo::getPrioridad).reversed()
                .thenComparing(Reclamo::getFechaCreacion);
    }
}
