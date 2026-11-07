package modelo.equipos;

import java.time.LocalDate;
import modelo.reclamos.Prioridad;

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

    // RN03.3 y RN03.4: prioridad normal, salvo que fallen más de tres luminarias del mismo sector.
    @Override public Prioridad calcularPrioridadFalla(int fallasEnSector) {
        if (fallasEnSector < 0) throw new IllegalArgumentException("Cantidad de fallas inválida.");
        return fallasEnSector > 3 ? Prioridad.ALTA : Prioridad.NORMAL;
    }
    @Override public String getTipo() { return "Luminaria"; }
    public String getTipoLampara() { return tipoLampara; }
    public double getPotencia() { return potencia; }
}
