package modelo.expensas;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Los datos originales del pago son inmutables: no hay setters ni forma de borrarlo (RN02.8).
// La única forma de corregirlo es agregar un AjustePago, que queda registrado aparte.
public final class Pago implements Serializable {
    private final LocalDate fecha;
    private final double importe;
    private final MedioPago medio;
    private final double imputadoAIntereses;
    private final double imputadoACapital;
    private final List<AjustePago> ajustes = new ArrayList<>();

    // Solo Expensa crea sus pagos (Creador), por eso el constructor no es público.
    Pago(LocalDate fecha, double importe, MedioPago medio, double imputadoAIntereses, double imputadoACapital) {
        if (fecha == null || medio == null) throw new IllegalArgumentException("Fecha y medio de pago son obligatorios.");
        if (importe <= 0) throw new IllegalArgumentException("El importe del pago debe ser mayor que cero.");
        this.fecha = fecha;
        this.importe = importe;
        this.medio = medio;
        this.imputadoAIntereses = imputadoAIntereses;
        this.imputadoACapital = imputadoACapital;
    }
    public LocalDate getFecha() { return fecha; }
    public double getImporte() { return importe; }
    public MedioPago getMedio() { return medio; }
    public double getImputadoAIntereses() { return imputadoAIntereses; }
    public double getImputadoACapital() { return imputadoACapital; }
    public List<AjustePago> getAjustes() { return new ArrayList<>(ajustes); }

    // Importe del pago teniendo en cuenta las correcciones registradas.
    public double getImporteEfectivo() {
        double total = importe;
        for (AjustePago ajuste : ajustes) total += ajuste.getImporte();
        return Expensa.redondear(total);
    }

    // Lo llama solo Expensa, que es la que actualiza la deuda junto con el ajuste.
    AjustePago registrarAjuste(LocalDate fecha, String motivo, double importe) {
        AjustePago ajuste = new AjustePago(fecha, motivo, importe);
        ajustes.add(ajuste);
        return ajuste;
    }
}
