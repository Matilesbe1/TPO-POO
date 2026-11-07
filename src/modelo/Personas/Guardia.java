package modelo.personas;

import modelo.accesos.Autorizacion;
import modelo.accesos.RegistroAcceso;
import modelo.equipos.Equipo;
import modelo.equipos.OrdenMantenimiento;
import modelo.excepciones.AutorizacionInvalidaException;
import modelo.excepciones.IngresoDuplicadoException;
import modelo.excepciones.IngresoNoEncontradoException;
import modelo.excepciones.ReglaNegocioException;
import modelo.lotes.Lote;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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

    // RF04: el guardia informa la falla; el equipo genera la orden con su propia prioridad.
    public OrdenMantenimiento registrarFalla(Equipo equipo, List<Equipo> equiposDelBarrio, String descripcion,
                                             LocalDateTime fechaHora) throws ReglaNegocioException {
        if (equipo == null || equiposDelBarrio == null) throw new IllegalArgumentException("El equipo es obligatorio.");
        int fallasEnSector = equipo.contarFallasEnSector(equiposDelBarrio);
        return equipo.registrarFalla(fechaHora, fallasEnSector, descripcion + " (informó " + nombre + ")");
    }

    // Sigue el diagrama de secuencia "Registrar ingreso":
    // recorre las autorizaciones del lote, valida la vigencia y controla que no haya un ingreso sin egreso.
    public RegistroAcceso registrarIngreso(String documento, Lote lote, List<Lote> lotesDelBarrio,
                                           LocalDateTime fechaHora) throws ReglaNegocioException {
        if (documento == null || lote == null || lotesDelBarrio == null || fechaHora == null)
            throw new IllegalArgumentException("Documento, lote y fecha son obligatorios.");
        Autorizacion vigente = null;
        for (Autorizacion aut : lote.getAutorizaciones())
            if (aut.getDocumentoVisitante().equals(documento) && aut.validarVigencia(fechaHora)) vigente = aut;
        // RN04.1 a RN04.3: sin una autorización válida no se habilita el ingreso.
        if (vigente == null)
            throw new AutorizacionInvalidaException("Ingreso denegado: " + documento
                    + " no tiene una autorización vigente para el lote " + lote.getNumero() + ".");
        // RN04.6: la misma persona no puede volver a ingresar sin haber registrado su egreso.
        if (buscarIngresoAbierto(documento, lotesDelBarrio) != null)
            throw new IngresoDuplicadoException("Ingreso denegado: " + documento + " tiene un ingreso sin egreso registrado.");
        RegistroAcceso registro = new RegistroAcceso(vigente, fechaHora, this);
        vigente.agregarRegistro(registro);
        return registro;
    }

    // RN04.5: cada ingreso debe tener su egreso.
    public RegistroAcceso registrarEgreso(String documento, List<Lote> lotesDelBarrio, LocalDateTime fechaHora)
            throws IngresoNoEncontradoException {
        if (documento == null || lotesDelBarrio == null) throw new IllegalArgumentException("El documento es obligatorio.");
        RegistroAcceso abierto = buscarIngresoAbierto(documento, lotesDelBarrio);
        if (abierto == null)
            throw new IngresoNoEncontradoException("No hay un ingreso sin egreso para " + documento + ".");
        abierto.registrarEgreso(fechaHora, this);
        return abierto;
    }

    private RegistroAcceso buscarIngresoAbierto(String documento, List<Lote> lotesDelBarrio) {
        for (Lote lote : lotesDelBarrio)
            for (Autorizacion aut : lote.getAutorizaciones())
                if (aut.getDocumentoVisitante().equals(documento) && aut.getIngresoAbierto() != null)
                    return aut.getIngresoAbierto();
        return null;
    }
}
