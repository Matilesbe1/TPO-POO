package modelo.accesos;

public class Proveedor extends PersonaExterna {
    private final String servicio;
    public Proveedor(String nombre, String documento, String servicio) {
        super(nombre, documento);
        if (servicio == null || servicio.isBlank()) throw new IllegalArgumentException("Servicio obligatorio.");
        this.servicio = servicio;
    }
    public String getServicio() { return servicio; }
    @Override public boolean puedeTenerAutorizacionRecurrente() { return true; }
    @Override public String getTipo() { return "Proveedor (" + servicio + ")"; }
}
