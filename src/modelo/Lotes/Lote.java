package modelo.lotes;

import modelo.personas.Vecino;
import modelo.reclamos.Reclamo;
import persistencia.Identificable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lote implements Identificable, Serializable {
    private final String numero;
    private final double superficie;
    private final double coeficiente;
    private Vecino titular;
    private final List<Vecino> residentes = new ArrayList<>();
    // Object permite integrar las clases Expensa y Autorizacion de los otros módulos sin acoplarlos todavía.
    private final List<Object> expensas = new ArrayList<>();
    private final List<Object> autorizaciones = new ArrayList<>();
    private final List<Reclamo> reclamos = new ArrayList<>();

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
    public List<Object> getExpensas() { return new ArrayList<>(expensas); }
    public List<Object> getAutorizaciones() { return new ArrayList<>(autorizaciones); }
    public List<Reclamo> getReclamos() { return new ArrayList<>(reclamos); }
    public void agregarExpensa(Object expensa) { if (expensa == null) throw new IllegalArgumentException(); expensas.add(expensa); }
    public void agregarAutorizacion(Object autorizacion) { if (autorizacion == null) throw new IllegalArgumentException(); autorizaciones.add(autorizacion); }

    @Override public boolean equals(Object otro) {
        if (this == otro) return true;
        if (!(otro instanceof Lote)) return false;
        return numero.equals(((Lote) otro).numero);
    }
    @Override public int hashCode() { return Objects.hash(numero); }
}
