package modelo.expensas;

import java.io.Serializable;
import java.time.LocalDate;

// Clase inmutable: un ajuste corrige un pago sin modificarlo (RN02.8).
public final class AjustePago implements Serializable {
    private final LocalDate fecha;
    private final String motivo;
    private final double importe;

    // Solo Pago crea sus ajustes (Creador), por eso el constructor no es público.
    AjustePago(LocalDate fecha, String motivo, double importe) {
        if (fecha == null || motivo == null || motivo.isBlank())
            throw new IllegalArgumentException("Fecha y motivo del ajuste son obligatorios.");
        if (importe == 0) throw new IllegalArgumentException("El importe del ajuste no puede ser cero.");
        this.fecha = fecha;
        this.motivo = motivo;
        this.importe = importe;
    }
    public LocalDate getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public double getImporte() { return importe; }
}
