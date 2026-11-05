package modelo;

public abstract class Equipo implements Identificable {
    private final String codigo;
    private final String sector;
    private EstadoEquipo estado;

    public Equipo(String codigo, String sector, EstadoEquipo estado) {
        this.codigo = codigo;
        this.sector = sector;
        this.estado = estado;
    }

    @Override
    public String getId() { 
        return codigo; 
    }
    
    public String getCodigo() { return codigo; }
    public String getSector() { return sector; }
    public EstadoEquipo getEstado() { return estado; }
    
    public void setEstado(EstadoEquipo estado) { 
        this.estado = estado; 
    }

    // Método abstracto para que cada subtipo (ej: Cámara, Luminaria) calcule su prioridad
    public abstract Prioridad calcularPrioridadFalla();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equipo)) return false;
        Equipo equipo = (Equipo) o;
        return codigo.equals(equipo.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }
}