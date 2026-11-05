package modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lote {
    private final int numero;
    private final double superficie;
    private Vecino titular;
    private final List<Vecino> residentes;

    public Lote(int numero, double superficie, Vecino titular) {
        this.numero = numero;
        this.superficie = superficie;
        this.titular = titular;
        this.residentes = new ArrayList<>(); // Usamos una lista simple en memoria
    }

    public int getNumero() { return numero; }
    public double getSuperficie() { return superficie; }
    public Vecino getTitular() { return titular; }
    
    public void setTitular(Vecino titular) { 
        this.titular = titular; 
    }

    public List<Vecino> getResidentes() {
        return residentes;
    }

    public void agregarResidente(Vecino residente) {
        this.residentes.add(residente);
    }

    // equals y hashCode basados en el número de lote (identificador único)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lote)) return false;
        Lote lote = (Lote) o;
        return numero == lote.numero;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }
}