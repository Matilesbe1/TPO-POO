package modelo.reclamos;

public enum TipoReclamo {
    SEGURIDAD(Prioridad.ALTA),
    MANTENIMIENTO(Prioridad.MEDIA),
    CONVIVENCIA(Prioridad.BAJA),
    ADMINISTRATIVO(Prioridad.BAJA);

    private final Prioridad prioridad;

    TipoReclamo(Prioridad prioridad) { this.prioridad = prioridad; }

    public Prioridad getPrioridad() { return prioridad; }
}
