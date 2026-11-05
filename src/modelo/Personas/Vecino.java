package modelo;

import java.util.Objects;

public class Vecino {
    private final String documento;
    private String nombre;
    private String telefono;

    public Vecino(String documento, String nombre, String telefono) {
        this.documento = documento;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vecino)) return false;
        Vecino vecino = (Vecino) o;
        return documento.equals(vecino.documento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documento);
    }
}