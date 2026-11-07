package modelo.accesos;

import java.io.Serializable;

public abstract class PersonaExterna implements Serializable {
    private final String nombre;
    private final String documento;

    protected PersonaExterna(String nombre, String documento) {
        if (nombre == null || nombre.isBlank() || documento == null || documento.isBlank())
            throw new IllegalArgumentException("Nombre y documento son obligatorios.");
        this.nombre = nombre;
        this.documento = documento;
    }
    public String getNombre() { return nombre; }
    public String getDocumento() { return documento; }
}
