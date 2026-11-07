package modelo.personas;

import java.io.Serializable;

public class Guardia implements Serializable {
    private final String legajo;
    private final String nombre;

    public Guardia(String legajo, String nombre) {
        if (legajo == null || legajo.isBlank() || nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Legajo y nombre son obligatorios.");
        this.legajo = legajo;
        this.nombre = nombre;
    }
    public String getLegajo() { return legajo; }
    public String getNombre() { return nombre; }
}
