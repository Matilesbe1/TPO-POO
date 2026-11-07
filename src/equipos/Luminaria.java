package ar.aromos.modelo.equipos;

import java.time.LocalDate;
import ar.aromos.modelo.reclamos.Prioridad;

public class Luminaria extends Equipo {
    private final String tipoLampara;
    private final double potencia;

    public Luminaria(String codigo, String sector, LocalDate instalacion,
                     String tipoLampara, double potencia) {
        super(codigo, sector, instalacion);
        if (tipoLampara == null || tipoLampara.isBlank() || potencia <= 0)
            throw new IllegalArgumentException("Tipo de lámpara y potencia deben ser válidos.");
        this.tipoLampara = tipoLampara;
        this.potencia = potencia;
    }

    @Override public Prioridad calcularPrioridadFalla(int fallasEnSector) {
        if (fallasEnSector < 0) throw new IllegalArgumentException("Cantidad de fallas inválida.");
        return fallasEnSector > 3 ? Prioridad.ALTA : Prioridad.NORMAL;
    }
    public String getTipoLampara() { return tipoLampara; }
    public double getPotencia() { return potencia; }
}
