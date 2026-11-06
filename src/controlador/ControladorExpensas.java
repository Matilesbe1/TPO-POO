package controlador;

import modelo.excepciones.ExpensaDuplicadaException;
import modelo.excepciones.ReglaNegocioException;
import modelo.expensas.AjustePago;
import modelo.expensas.Expensa;
import modelo.expensas.MedioPago;
import modelo.expensas.Notificacion;
import modelo.expensas.Pago;
import modelo.lotes.Lote;
import modelo.reportes.ReporteMorosidad;
import persistencia.Repositorio;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Controlador: recibe los pedidos de la vista, busca los lotes y le delega las reglas al modelo.
public class ControladorExpensas {
    private final Repositorio<Lote> lotes;

    public ControladorExpensas(Repositorio<Lote> lotes) {
        this.lotes = lotes;
    }

    // RF03: emite la expensa del período para cada lote registrado.
    public List<String> emitirExpensas(YearMonth periodo, double presupuesto) {
        List<Lote> todos = lotes.listar();
        if (todos.isEmpty()) throw new IllegalArgumentException("No hay lotes registrados.");
        List<String> resultado = new ArrayList<>();
        for (Lote lote : todos) {
            try {
                Expensa expensa = lote.emitirExpensa(periodo, presupuesto);
                lotes.actualizar(lote);
                resultado.add("Lote " + lote.getNumero() + ": $ " + expensa.getImporte()
                        + " (vence el " + expensa.getFechaVencimiento() + ")");
            } catch (ExpensaDuplicadaException e) {
                resultado.add(e.getMessage());
            }
        }
        return resultado;
    }

    public List<String> registrarPago(String numeroLote, LocalDate fecha, double importe, MedioPago medio)
            throws ReglaNegocioException {
        Lote lote = obtenerLote(numeroLote);
        List<Pago> pagos = lote.imputarPago(fecha, importe, medio);
        lotes.actualizar(lote);
        List<String> resultado = new ArrayList<>();
        for (Expensa expensa : lote.getExpensas())
            for (Pago pago : pagos)
                if (expensa.getPagos().contains(pago))
                    resultado.add("Período " + expensa.getPeriodo() + ": $ " + pago.getImporte()
                            + " (intereses $ " + pago.getImputadoAIntereses()
                            + ", capital $ " + pago.getImputadoACapital() + ")");
        return resultado;
    }

    public String registrarAjuste(String numeroLote, YearMonth periodo, int numeroPago, LocalDate fecha,
                                  String motivo, double importe) throws ReglaNegocioException {
        Lote lote = obtenerLote(numeroLote);
        AjustePago ajuste = lote.registrarAjuste(periodo, numeroPago, fecha, motivo, importe);
        lotes.actualizar(lote);
        return "Ajuste de $ " + ajuste.getImporte() + " registrado. Nueva deuda del período: $ "
                + lote.buscarExpensa(periodo).getDeuda(fecha);
    }

    public List<String> estadoDeCuenta(String numeroLote, LocalDate fecha) {
        Lote lote = obtenerLote(numeroLote);
        lote.actualizarMorosidad(fecha);
        lotes.actualizar(lote);
        List<Expensa> expensas = lote.getExpensas();
        Collections.sort(expensas);
        List<String> resultado = new ArrayList<>();
        resultado.add("Lote " + lote.getNumero() + " - Titular: " + lote.getTitular().getNombre());
        for (Expensa expensa : expensas) {
            resultado.add("  " + expensa.getPeriodo() + " | Importe $ " + expensa.getImporte()
                    + " | Vence " + expensa.getFechaVencimiento() + " | " + expensa.getEstado()
                    + " | Capital $ " + expensa.getCapitalPendiente()
                    + " | Interés $ " + expensa.calcularInteres(fecha)
                    + " | Deuda $ " + expensa.getDeuda(fecha));
            int numero = 1;
            for (Pago pago : expensa.getPagos()) {
                resultado.add("      Pago " + numero + ": " + pago.getFecha() + " $ " + pago.getImporte()
                        + " " + pago.getMedio() + " (efectivo $ " + pago.getImporteEfectivo() + ")");
                for (AjustePago ajuste : pago.getAjustes())
                    resultado.add("          Ajuste " + ajuste.getFecha() + " $ " + ajuste.getImporte()
                            + ": " + ajuste.getMotivo());
                numero++;
            }
        }
        resultado.add("Deuda total: $ " + lote.getDeudaTotal(fecha)
                + " | Períodos impagos: " + lote.contarPeriodosImpagos(fecha)
                + (lote.esMoroso(fecha) ? " | MOROSO" : ""));
        return resultado;
    }

    public List<String> reporteMorosidad(LocalDate fecha) {
        List<Lote> todos = lotes.listar();
        for (Lote lote : todos) {
            lote.actualizarMorosidad(fecha);
            lotes.actualizar(lote);
        }
        return new ReporteMorosidad(todos, fecha).generar();
    }

    public List<String> notificaciones(String numeroLote) {
        List<String> resultado = new ArrayList<>();
        for (Notificacion notificacion : obtenerLote(numeroLote).getNotificaciones())
            resultado.add(notificacion.getFecha() + " | Para: " + notificacion.getDestinatario().getNombre()
                    + " | " + notificacion.getMensaje());
        return resultado;
    }

    private Lote obtenerLote(String numero) {
        return lotes.buscarPorId(numero).orElseThrow(() -> new IllegalArgumentException("No existe el lote " + numero + "."));
    }
}
