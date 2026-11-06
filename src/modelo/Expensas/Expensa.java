package modelo.expensas;

import modelo.excepciones.AjusteInvalidoException;
import modelo.excepciones.PagoInvalidoException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Expensa implements Comparable<Expensa>, Serializable {
    public static final double TASA_MENSUAL = 0.03;   // RN02.3
    public static final int DIAS_POR_MES = 30;        // Supuesto: prorrateo diario (3 % / 30 por día)
    public static final int DIA_VENCIMIENTO = 10;     // RN02.2

    private final YearMonth periodo;
    private final double importe;
    private final LocalDate fechaVencimiento;
    private double capitalPendiente;
    private double interesPendiente;
    private LocalDate interesCalculadoHasta;
    private final List<Pago> pagos = new ArrayList<>();

    // RN02.1: importe = presupuesto del mes x coeficiente de participación del lote.
    public Expensa(YearMonth periodo, double presupuesto, double coeficiente) {
        if (periodo == null) throw new IllegalArgumentException("El período es obligatorio.");
        if (presupuesto <= 0 || coeficiente <= 0)
            throw new IllegalArgumentException("El presupuesto y el coeficiente deben ser mayores que cero.");
        this.periodo = periodo;
        this.importe = redondear(presupuesto * coeficiente);
        this.fechaVencimiento = periodo.atDay(DIA_VENCIMIENTO);
        this.capitalPendiente = importe;
        this.interesCalculadoHasta = fechaVencimiento;
    }

    public YearMonth getPeriodo() { return periodo; }
    public double getImporte() { return importe; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public double getCapitalPendiente() { return capitalPendiente; }
    public List<Pago> getPagos() { return new ArrayList<>(pagos); }

    public EstadoExpensa getEstado() {
        if (estaPagada()) return EstadoExpensa.PAGADA;
        if (pagos.isEmpty()) return EstadoExpensa.PENDIENTE;
        return EstadoExpensa.PAGADA_PARCIALMENTE;
    }
    public boolean estaPagada() { return capitalPendiente == 0; }
    public boolean estaVencida(LocalDate fecha) { return fecha.isAfter(fechaVencimiento); }
    public boolean estaImpaga(LocalDate fecha) { return estaVencida(fecha) && !estaPagada(); }

    // Experto: la expensa conoce su capital, su vencimiento y sus pagos, entonces calcula su interés.
    // RN02.3: 3 % mensual sobre el saldo impago desde el vencimiento.
    public double calcularInteres(LocalDate fecha) {
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria.");
        return redondear(interesPendiente + interesGenerado(fecha));
    }
    public double getDeuda(LocalDate fecha) {
        return redondear(capitalPendiente + calcularInteres(fecha));
    }

    // Creador: la expensa contiene sus pagos, entonces los crea.
    // RN02.4: el pago cubre primero los intereses y después el capital.
    public Pago registrarPago(LocalDate fecha, double monto, MedioPago medio) throws PagoInvalidoException {
        if (fecha == null || medio == null) throw new IllegalArgumentException("Fecha y medio de pago son obligatorios.");
        if (monto <= 0) throw new IllegalArgumentException("El importe del pago debe ser mayor que cero.");
        if (estaPagada()) throw new PagoInvalidoException("La expensa " + periodo + " ya está pagada.");
        double deuda = getDeuda(fecha);
        if (redondear(monto) > deuda)
            throw new PagoInvalidoException("El pago supera la deuda de la expensa " + periodo + " ($ " + deuda + ").");
        acumularInteres(fecha);
        double aIntereses = Math.min(redondear(monto), interesPendiente);
        double aCapital = redondear(monto - aIntereses);
        interesPendiente = redondear(interesPendiente - aIntereses);
        capitalPendiente = redondear(capitalPendiente - aCapital);
        Pago pago = new Pago(fecha, redondear(monto), medio, aIntereses, aCapital);
        pagos.add(pago);
        return pago;
    }

    // RN02.8: el pago original no se toca. El ajuste queda registrado en el pago y corrige el saldo.
    // Un ajuste positivo reconoce un pago mayor: baja la deuda, primero intereses y después capital (RN02.4).
    // Un ajuste negativo anula parte del pago: vuelve a sumar capital y, si no alcanza, intereses.
    public AjustePago registrarAjuste(int numeroPago, LocalDate fecha, String motivo, double monto)
            throws AjusteInvalidoException {
        if (numeroPago < 1 || numeroPago > pagos.size())
            throw new IllegalArgumentException("La expensa " + periodo + " no tiene un pago número " + numeroPago + ".");
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria.");
        Pago pago = pagos.get(numeroPago - 1);
        if (redondear(pago.getImporteEfectivo() + monto) < 0)
            throw new AjusteInvalidoException("El ajuste deja el pago con un importe negativo.");
        if (monto > 0 && redondear(monto) > getDeuda(fecha))
            throw new AjusteInvalidoException("El ajuste deja la expensa con saldo a favor.");
        acumularInteres(fecha);
        if (monto > 0) {
            double aIntereses = Math.min(redondear(monto), interesPendiente);
            interesPendiente = redondear(interesPendiente - aIntereses);
            capitalPendiente = redondear(capitalPendiente - (monto - aIntereses));
        } else {
            double aCapital = Math.min(redondear(-monto), redondear(importe - capitalPendiente));
            capitalPendiente = redondear(capitalPendiente + aCapital);
            interesPendiente = redondear(interesPendiente + (-monto - aCapital));
        }
        return pago.registrarAjuste(fecha, motivo, redondear(monto));
    }

    // Orden natural por período: permite imputar primero al período más antiguo (RN02.4).
    @Override
    public int compareTo(Expensa otra) {
        return this.periodo.compareTo(otra.periodo);
    }

    // Interés que generó el capital pendiente desde el último cálculo hasta la fecha indicada.
    private double interesGenerado(LocalDate fecha) {
        if (!fecha.isAfter(interesCalculadoHasta)) return 0;
        long dias = ChronoUnit.DAYS.between(interesCalculadoHasta, fecha);
        return capitalPendiente * TASA_MENSUAL / DIAS_POR_MES * dias;
    }
    private void acumularInteres(LocalDate fecha) {
        interesPendiente = redondear(interesPendiente + interesGenerado(fecha));
        if (fecha.isAfter(interesCalculadoHasta)) interesCalculadoHasta = fecha;
    }

    // Redondea a centavos para evitar errores de precisión de double.
    public static double redondear(double valor) {
        return Math.round(valor * 100) / 100.0;
    }
}
