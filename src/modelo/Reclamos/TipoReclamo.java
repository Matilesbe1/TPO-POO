package modelo;

public enum TipoReclamo {
    SEGURIDAD(Prioridad.ALTA),
    MANTENIMIENTO(Prioridad.MEDIA),
    CONVIVENCIA(Prioridad.BAJA),
    ADMINISTRATIVO(Prioridad.BAJA);

    private final Prioridad prioridadPorDefecto;

    TipoReclamo(Prioridad prioridadPorDefecto) {
        this.prioridadPorDefecto = prioridadPorDefecto;
    }

    public Prioridad getPrioridadPorDefecto() {
        return prioridadPorDefecto;
    }
}