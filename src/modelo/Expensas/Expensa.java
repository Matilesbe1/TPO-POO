package modelo;

public class Expensa implements Comparable<Expensa>, Identificable {
    private final String periodo; // Formato "YYYY-MM" (Ej: "2026-05")
    private final double importeTotal;
    private double saldoImpago;
    private EstadoExpensa estado;

    public Expensa(String periodo, double importeTotal) {
        this.periodo = periodo;
        this.importeTotal = importeTotal;
        this.saldoImpago = importeTotal;
        this.estado = EstadoExpensa.PENDIENTE;
    }

    @Override
    public String getId() { 
        return periodo; 
    }

    public String getPeriodo() { return periodo; }
    public double getImporteTotal() { return importeTotal; }
    public double getSaldoImpago() { return saldoImpago; }
    public EstadoExpensa getEstado() { return estado; }

    public void setSaldoImpago(double saldoImpago) { this.saldoImpago = saldoImpago; }
    public void setEstado(EstadoExpensa estado) { this.estado = estado; }

    @Override
    public int compareTo(Expensa otra) {
        // Orden natural por período. Permite ordenar las expensas de más vieja a más nueva
        // para cumplir con la regla de negocio de pagar primero los períodos más antiguos.
        return this.periodo.compareTo(otra.periodo);
    }
}