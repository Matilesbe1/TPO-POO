package modelo.accesos;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import modelo.personas.Vecino;
import modelo.lotes.Lote;
import modelo.excepciones.AutorizacionInvalidaException;

public class Autorizacion implements Serializable {
    private final int numero;
    private final PersonaExterna persona;
    private final Vecino autorizante;
    private final Lote lote;
    private final LocalDateTime vigenciaDesde;
    private final LocalDateTime vigenciaHasta;
    private final boolean recurrente;

    public Autorizacion(PersonaExterna persona, Vecino autorizante, Lote lote,
                        LocalDateTime desde, LocalDateTime hasta, boolean recurrente)
            throws AutorizacionInvalidaException {
        if (persona == null || autorizante == null || lote == null || desde == null || hasta == null
                || hasta.isBefore(desde))
            throw new AutorizacionInvalidaException("Datos o vigencia de autorización inválidos.");
        if (recurrente && Duration.between(desde, hasta).toDays() > 30)
            throw new AutorizacionInvalidaException("Una autorización recurrente no puede superar 30 días.");
        this.numero = Math.abs((persona.getDocumento() + desde.toString()).hashCode());
        this.persona = persona;
        this.autorizante = autorizante;
        this.lote = lote;
        this.vigenciaDesde = desde;
        this.vigenciaHasta = hasta;
        this.recurrente = recurrente;
    }

    public boolean validarVigencia(LocalDateTime fechaHora) {
        return fechaHora != null && !fechaHora.isBefore(vigenciaDesde)
                && !fechaHora.isAfter(vigenciaHasta);
    }
    public int getNumero() { return numero; }
    public PersonaExterna getPersona() { return persona; }
    public String getDocumentoVisitante() { return persona.getDocumento(); }
    public Vecino getAutorizante() { return autorizante; }
    public Lote getLote() { return lote; }
    public LocalDateTime getVigenciaDesde() { return vigenciaDesde; }
    public LocalDateTime getVigenciaHasta() { return vigenciaHasta; }
    public boolean isRecurrente() { return recurrente; }
}
