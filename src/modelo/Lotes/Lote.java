package modelo.lotes;

import modelo.excepciones.AjusteInvalidoException;
import modelo.excepciones.ExpensaDuplicadaException;
import modelo.excepciones.PagoInvalidoException;
import modelo.expensas.AjustePago;
import modelo.expensas.Expensa;
import modelo.expensas.MedioPago;
import modelo.expensas.Notificacion;
import modelo.expensas.Pago;
import modelo.personas.Vecino;
import modelo.reclamos.Reclamo;
import persistencia.Identificable;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Lote implements Identificable, Serializable {
    public static final int MAXIMO_PERIODOS_IMPAGOS = 3;   // RN02.5: con más de 3 es moroso

    private final String numero;
    private final double superficie;
    private final double coeficiente;
    private Vecino titular;
    private final List<Vecino> residentes = new ArrayList<>();
    private final List<Expensa> expensas = new ArrayList<>();
    // Object permite integrar la clase Autorizacion del otro módulo sin acoplarlo todavía.
    private final List<Object> autorizaciones = new ArrayList<>();
    private final List<Reclamo> reclamos = new ArrayList<>();
    private final List<Notificacion> notificaciones = new ArrayList<>();
    private boolean moroso;

    public Lote(String numero, double superficie, double coeficiente, Vecino titular) {
        if (numero == null || numero.isBlank() || superficie <= 0 || coeficiente <= 0 || titular == null)
            throw new IllegalArgumentException("Datos del lote inválidos.");
        this.numero = numero;
        this.superficie = superficie;
        this.coeficiente = coeficiente;
        this.titular = titular;
    }
    @Override public String getId() { return numero; }
    public String getNumero() { return numero; }
    public double getSuperficie() { return superficie; }
    public double getCoeficiente() { return coeficiente; }
    public Vecino getTitular() { return titular; }
    public void agregarResidente(Vecino vecino) {
        if (vecino == null) throw new IllegalArgumentException("El residente no puede ser null.");
        if (!residentes.contains(vecino)) residentes.add(vecino);
    }
    public boolean estaAutorizado(Vecino vecino) {
        return titular.equals(vecino) || residentes.contains(vecino);
    }
    public void agregarReclamo(Reclamo reclamo) {
        if (reclamo == null) throw new IllegalArgumentException("El reclamo no puede ser null.");
        reclamos.add(reclamo);
    }
    public List<Vecino> getResidentes() { return new ArrayList<>(residentes); }
    public List<Expensa> getExpensas() { return new ArrayList<>(expensas); }
    public List<Object> getAutorizaciones() { return new ArrayList<>(autorizaciones); }
    public List<Reclamo> getReclamos() { return new ArrayList<>(reclamos); }
    public List<Notificacion> getNotificaciones() { return new ArrayList<>(notificaciones); }

    // Creador: el lote genera y contiene sus expensas. Una sola expensa por período.
    public Expensa emitirExpensa(YearMonth periodo, double presupuesto) throws ExpensaDuplicadaException {
        if (periodo == null) throw new IllegalArgumentException("El período es obligatorio.");
        for (Expensa expensa : expensas)
            if (expensa.getPeriodo().equals(periodo))
                throw new ExpensaDuplicadaException("El lote " + numero + " ya tiene la expensa de " + periodo + ".");
        Expensa expensa = new Expensa(periodo, presupuesto, coeficiente);
        expensas.add(expensa);
        return expensa;
    }

    public Expensa buscarExpensa(YearMonth periodo) {
        for (Expensa expensa : expensas)
            if (expensa.getPeriodo().equals(periodo)) return expensa;
        throw new IllegalArgumentException("El lote " + numero + " no tiene expensa del período " + periodo + ".");
    }

    public double getDeudaTotal(LocalDate fecha) {
        double total = 0;
        for (Expensa expensa : expensas) total += expensa.getDeuda(fecha);
        return Expensa.redondear(total);
    }

    // RN02.4: un pago se reparte entre las expensas empezando por el período más antiguo.
    // Cada expensa lo imputa primero a intereses y después a capital.
    public List<Pago> imputarPago(LocalDate fecha, double importe, MedioPago medio) throws PagoInvalidoException {
        if (fecha == null || medio == null) throw new IllegalArgumentException("Fecha y medio de pago son obligatorios.");
        if (importe <= 0) throw new IllegalArgumentException("El importe del pago debe ser mayor que cero.");
        double deudaTotal = getDeudaTotal(fecha);
        if (deudaTotal == 0) throw new PagoInvalidoException("El lote " + numero + " no tiene deuda.");
        if (Expensa.redondear(importe) > deudaTotal)
            throw new PagoInvalidoException("El pago supera la deuda total del lote ($ " + deudaTotal + ").");
        List<Expensa> ordenadas = new ArrayList<>(expensas);
        Collections.sort(ordenadas);
        List<Pago> pagosGenerados = new ArrayList<>();
        double restante = Expensa.redondear(importe);
        for (Expensa expensa : ordenadas) {
            if (restante == 0) break;
            if (expensa.estaPagada()) continue;
            double aplicar = Math.min(restante, expensa.getDeuda(fecha));
            pagosGenerados.add(expensa.registrarPago(fecha, aplicar, medio));
            restante = Expensa.redondear(restante - aplicar);
        }
        actualizarMorosidad(fecha);
        return pagosGenerados;
    }

    // RN02.8: los pagos no se borran, se corrigen con un ajuste.
    public AjustePago registrarAjuste(YearMonth periodo, int numeroPago, LocalDate fecha, String motivo, double importe)
            throws AjusteInvalidoException {
        AjustePago ajuste = buscarExpensa(periodo).registrarAjuste(numeroPago, fecha, motivo, importe);
        actualizarMorosidad(fecha);
        return ajuste;
    }

    public int contarPeriodosImpagos(LocalDate fecha) {
        int impagos = 0;
        for (Expensa expensa : expensas)
            if (expensa.estaImpaga(fecha)) impagos++;
        return impagos;
    }

    // Experto: el lote conoce todas sus expensas, entonces sabe si es moroso (RN02.5).
    public boolean esMoroso(LocalDate fecha) {
        return contarPeriodosImpagos(fecha) > MAXIMO_PERIODOS_IMPAGOS;
    }

    // RN02.6 y RN02.7: al pasar a moroso se notifica al titular; al regularizar deja de ser moroso.
    public void actualizarMorosidad(LocalDate fecha) {
        boolean morosoAhora = esMoroso(fecha);
        if (morosoAhora && !moroso)
            notificaciones.add(new Notificacion(titular, fecha,
                    "El lote " + numero + " registra " + contarPeriodosImpagos(fecha)
                            + " períodos impagos y pasa a ser moroso. Deuda: $ " + getDeudaTotal(fecha)));
        moroso = morosoAhora;
    }

    public void agregarAutorizacion(Object autorizacion) { if (autorizacion == null) throw new IllegalArgumentException(); autorizaciones.add(autorizacion); }

    @Override public boolean equals(Object otro) {
        if (this == otro) return true;
        if (!(otro instanceof Lote)) return false;
        return numero.equals(((Lote) otro).numero);
    }
    @Override public int hashCode() { return Objects.hash(numero); }
}
