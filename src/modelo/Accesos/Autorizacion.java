package modelo.accesos;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import modelo.personas.Vecino;
import modelo.lotes.Lote;
import modelo.excepciones.AutorizacionInvalidaException;

public class Autorizacion implements Serializable {
    public static final int MAXIMO_DIAS_RECURRENTE = 30;   // RN04.4

    private final String numero;
    private final PersonaExterna persona;
    private final Vecino autorizante;
    private final Lote lote;
    private final LocalDate fechaDesde;
    private final LocalDate fechaHasta;
    private final LocalTime horaDesde;
    private final LocalTime horaHasta;
    private final boolean recurrente;
    private final List<RegistroAcceso> registros = new ArrayList<>();

    // RN04.2: la autorización indica las fechas y la franja horaria en la que se permite el ingreso.
    public Autorizacion(PersonaExterna persona, Vecino autorizante, Lote lote, LocalDate fechaDesde,
                        LocalDate fechaHasta, LocalTime horaDesde, LocalTime horaHasta, boolean recurrente)
            throws AutorizacionInvalidaException {
        if (persona == null || autorizante == null || lote == null || fechaDesde == null || fechaHasta == null
                || horaDesde == null || horaHasta == null)
            throw new IllegalArgumentException("Todos los datos de la autorización son obligatorios.");
        if (fechaHasta.isBefore(fechaDesde) || !horaHasta.isAfter(horaDesde))
            throw new AutorizacionInvalidaException("Las fechas o la franja horaria son inválidas.");
        if (!recurrente && !fechaHasta.equals(fechaDesde))
            throw new AutorizacionInvalidaException("Una autorización no recurrente es para un solo día.");
        // RN04.4: solo proveedores, y por un período máximo de 30 días.
        if (recurrente && !persona.puedeTenerAutorizacionRecurrente())
            throw new AutorizacionInvalidaException("Solo los proveedores pueden tener autorizaciones recurrentes.");
        if (recurrente && ChronoUnit.DAYS.between(fechaDesde, fechaHasta) + 1 > MAXIMO_DIAS_RECURRENTE)
            throw new AutorizacionInvalidaException("Una autorización recurrente no puede superar los 30 días.");
        this.numero = persona.getDocumento() + "-" + fechaDesde + "-" + horaDesde;
        this.persona = persona;
        this.autorizante = autorizante;
        this.lote = lote;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.horaDesde = horaDesde;
        this.horaHasta = horaHasta;
        this.recurrente = recurrente;
    }

    // Experto: la autorización conoce sus fechas y su franja horaria, entonces valida su vigencia.
    public boolean validarVigencia(LocalDateTime fechaHora) {
        if (fechaHora == null) return false;
        LocalDate fecha = fechaHora.toLocalDate();
        LocalTime hora = fechaHora.toLocalTime();
        return !fecha.isBefore(fechaDesde) && !fecha.isAfter(fechaHasta)
                && !hora.isBefore(horaDesde) && !hora.isAfter(horaHasta);
    }

    public void agregarRegistro(RegistroAcceso registro) {
        if (registro == null || registro.getAutorizacion() != this)
            throw new IllegalArgumentException("El registro no corresponde a esta autorización.");
        registros.add(registro);
    }

    public RegistroAcceso getIngresoAbierto() {
        for (RegistroAcceso registro : registros)
            if (registro.getHoraEgreso() == null) return registro;
        return null;
    }

    public String getNumero() { return numero; }
    public PersonaExterna getPersona() { return persona; }
    public String getDocumentoVisitante() { return persona.getDocumento(); }
    public Vecino getAutorizante() { return autorizante; }
    public Lote getLote() { return lote; }
    public LocalDate getFechaDesde() { return fechaDesde; }
    public LocalDate getFechaHasta() { return fechaHasta; }
    public LocalTime getHoraDesde() { return horaDesde; }
    public LocalTime getHoraHasta() { return horaHasta; }
    public boolean isRecurrente() { return recurrente; }
    public List<RegistroAcceso> getRegistros() { return new ArrayList<>(registros); }
}
