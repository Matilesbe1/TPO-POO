package ar.aromos.modelo.equipos;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import ar.aromos.modelo.reclamos.Prioridad;

/** Equipo común del barrio. La prioridad se resuelve polimórficamente en las subclases. */
public abstract class Equipo implements Serializable {
    private final String codigo;
    private final String sector;
    private final LocalDate fechaInstalacion;
    private EstadoEquipo estado;
    private LocalDateTime fechaHoraFalla;

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

    public void registrarFalla(LocalDateTime fechaHora) {
        if (fechaHora == null) throw new IllegalArgumentException("La fecha de falla es obligatoria.");
        estado = EstadoEquipo.CON_FALLA;
        if (fechaHoraFalla == null) fechaHoraFalla = fechaHora;
    }

    public void registrarFalla() {
        registrarFalla(LocalDateTime.of(fechaInstalacion, java.time.LocalTime.MIDNIGHT));
    }

    public void marcarEnReparacion() { estado = EstadoEquipo.EN_REPARACION; }
    public void marcarFueraDeServicio() { estado = EstadoEquipo.FUERA_DE_SERVICIO; }
    public void marcarOperativo() { estado = EstadoEquipo.OPERATIVO; fechaHoraFalla = null; }

    public abstract Prioridad calcularPrioridadFalla(int fallasEnSector);

    public String getCodigo() { return codigo; }
    public String getSector() { return sector; }
    public LocalDate getFechaInstalacion() { return fechaInstalacion; }
    public EstadoEquipo getEstado() { return estado; }
    public LocalDateTime getFechaHoraFalla() { return fechaHoraFalla; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equipo)) return false;
        Equipo equipo = (Equipo) o;
        return codigo.equals(equipo.codigo);
    }
    @Override public int hashCode() { return Objects.hash(codigo); }
}
