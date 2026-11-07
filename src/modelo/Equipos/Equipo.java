package modelo.equipos;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import modelo.excepciones.EquipoConFallaException;
import modelo.reclamos.Prioridad;
import persistencia.Identificable;

/** Equipo común del barrio. La prioridad se resuelve polimórficamente en las subclases. */
public abstract class Equipo implements Identificable, Serializable {
    private final String codigo;
    private final String sector;
    private final LocalDate fechaInstalacion;
    private EstadoEquipo estado;
    private LocalDateTime fechaHoraFalla;
    private final List<OrdenMantenimiento> historial = new ArrayList<>();

    protected Equipo(String codigo, String sector, LocalDate fechaInstalacion) {
        if (codigo == null || codigo.isBlank() || sector == null || sector.isBlank()
                || fechaInstalacion == null) {
            throw new IllegalArgumentException("Código, sector y fecha de instalación son obligatorios.");
        }
        this.codigo = codigo;
        this.sector = sector;
        this.fechaInstalacion = fechaInstalacion;
        this.estado = EstadoEquipo.OPERATIVO;
    }

    // Creador: el equipo contiene su historial de mantenimiento, entonces crea sus órdenes.
    // Polimorfismo: la prioridad la calcula cada subclase (RN03.2 a RN03.4).
    public OrdenMantenimiento registrarFalla(LocalDateTime fechaHora, int fallasEnSector, String descripcion)
            throws EquipoConFallaException {
        if (fechaHora == null) throw new IllegalArgumentException("La fecha de falla es obligatoria.");
        if (estado != EstadoEquipo.OPERATIVO)
            throw new EquipoConFallaException("El equipo " + codigo + " ya tiene una falla registrada.");
        OrdenMantenimiento orden = new OrdenMantenimiento(this, historial.size() + 1,
                calcularPrioridadFalla(fallasEnSector), descripcion, fechaHora.toLocalDate());
        historial.add(orden);
        estado = EstadoEquipo.CON_FALLA;
        fechaHoraFalla = fechaHora;
        return orden;
    }

    // Cantidad de equipos del mismo tipo con falla en el sector, contando esta nueva falla (RN03.4).
    public int contarFallasEnSector(List<Equipo> equipos) {
        int fallas = 1;
        for (Equipo otro : equipos)
            if (!otro.equals(this) && otro.getSector().equals(sector) && otro.getTipo().equals(getTipo())
                    && otro.getEstado() != EstadoEquipo.OPERATIVO)
                fallas++;
        return fallas;
    }

    public OrdenMantenimiento getOrdenAbierta() {
        for (OrdenMantenimiento orden : historial)
            if (!orden.isCerrada()) return orden;
        throw new IllegalArgumentException("El equipo " + codigo + " no tiene una orden abierta.");
    }

    // Por defecto un equipo no genera avisos; Camara lo redefine (RN03.5).
    public boolean requiereAvisoAdministrador(LocalDateTime fechaHora) {
        return false;
    }

    public void marcarEnReparacion() { estado = EstadoEquipo.EN_REPARACION; }
    public void marcarFueraDeServicio() { estado = EstadoEquipo.FUERA_DE_SERVICIO; }
    public void marcarOperativo() { estado = EstadoEquipo.OPERATIVO; fechaHoraFalla = null; }

    public abstract Prioridad calcularPrioridadFalla(int fallasEnSector);
    public abstract String getTipo();

    @Override public String getId() { return codigo; }
    public String getCodigo() { return codigo; }
    public String getSector() { return sector; }
    public LocalDate getFechaInstalacion() { return fechaInstalacion; }
    public EstadoEquipo getEstado() { return estado; }
    public LocalDateTime getFechaHoraFalla() { return fechaHoraFalla; }
    public List<OrdenMantenimiento> getHistorial() { return new ArrayList<>(historial); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equipo)) return false;
        Equipo equipo = (Equipo) o;
        return codigo.equals(equipo.codigo);
    }
    @Override public int hashCode() { return Objects.hash(codigo); }
}
