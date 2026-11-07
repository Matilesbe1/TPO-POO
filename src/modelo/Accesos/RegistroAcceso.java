package modelo.accesos;

import java.io.Serializable;
import java.time.LocalDateTime;
import modelo.personas.Guardia;
import modelo.excepciones.IngresoNoEncontradoException;

public class RegistroAcceso implements Serializable {
    private final String numero;
    private final Autorizacion autorizacion;
    private final LocalDateTime horaIngreso;
    private final Guardia guardiaIngreso;
    private LocalDateTime horaEgreso;
    private Guardia guardiaEgreso;

    public RegistroAcceso(Autorizacion autorizacion, LocalDateTime ingreso, Guardia guardia) {
        if (autorizacion == null || ingreso == null || guardia == null)
            throw new IllegalArgumentException("Autorización, ingreso y guardia son obligatorios.");
        this.numero = autorizacion.getDocumentoVisitante() + "-" + ingreso;
        this.autorizacion = autorizacion;
        this.horaIngreso = ingreso;
        this.guardiaIngreso = guardia;
    }
    public void registrarEgreso(LocalDateTime egreso, Guardia guardia)
            throws IngresoNoEncontradoException {
        if (horaEgreso != null) throw new IngresoNoEncontradoException("El ingreso ya tiene egreso.");
        if (egreso == null || guardia == null || egreso.isBefore(horaIngreso))
            throw new IngresoNoEncontradoException("Egreso inválido.");
        horaEgreso = egreso;
        guardiaEgreso = guardia;
    }
    public String getNumero() { return numero; }
    public Autorizacion getAutorizacion() { return autorizacion; }
    public String getDocumentoPersona() { return autorizacion.getDocumentoVisitante(); }
    public LocalDateTime getHoraIngreso() { return horaIngreso; }
    public LocalDateTime getHoraEgreso() { return horaEgreso; }
    public Guardia getGuardiaIngreso() { return guardiaIngreso; }
    public Guardia getGuardiaEgreso() { return guardiaEgreso; }
}
