package controlador;

import modelo.accesos.Autorizacion;
import modelo.accesos.PersonaExterna;
import modelo.accesos.Proveedor;
import modelo.accesos.RegistroAcceso;
import modelo.accesos.Visitante;
import modelo.excepciones.ReglaNegocioException;
import modelo.lotes.Lote;
import modelo.personas.Guardia;
import modelo.personas.Vecino;
import persistencia.Repositorio;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Controlador: recibe los pedidos de la vista, busca vecinos y lotes y le delega las reglas al modelo.
public class ControladorAccesos {
    private final Repositorio<Vecino> vecinos;
    private final Repositorio<Lote> lotes;

    public ControladorAccesos(Repositorio<Vecino> vecinos, Repositorio<Lote> lotes) {
        this.vecinos = vecinos;
        this.lotes = lotes;
    }

    public String autorizarVisita(String documentoVecino, String numeroLote, boolean esProveedor, String nombre,
                                  String documento, String servicio, LocalDate fechaDesde, LocalDate fechaHasta,
                                  LocalTime horaDesde, LocalTime horaHasta, boolean recurrente)
            throws ReglaNegocioException {
        Vecino vecino = vecinos.buscarPorId(documentoVecino)
                .orElseThrow(() -> new IllegalArgumentException("No existe el vecino."));
        Lote lote = obtenerLote(numeroLote);
        PersonaExterna persona = esProveedor ? new Proveedor(nombre, documento, servicio) : new Visitante(nombre, documento);
        Autorizacion autorizacion = vecino.autorizarVisita(persona, lote, fechaDesde, fechaHasta, horaDesde, horaHasta, recurrente);
        lotes.actualizar(lote);
        return "Autorización registrada para " + persona.getNombre() + " (" + persona.getTipo() + "): del "
                + autorizacion.getFechaDesde() + " al " + autorizacion.getFechaHasta()
                + ", de " + autorizacion.getHoraDesde() + " a " + autorizacion.getHoraHasta() + ".";
    }

    public String registrarIngreso(String documento, String numeroLote, Guardia guardia, LocalDateTime fechaHora)
            throws ReglaNegocioException {
        Lote lote = obtenerLote(numeroLote);
        RegistroAcceso registro = guardia.registrarIngreso(documento, lote, lotes.listar(), fechaHora);
        lotes.actualizar(lote);
        return "Ingreso registrado: " + registro.getAutorizacion().getPersona().getNombre()
                + " al lote " + lote.getNumero() + " a las " + registro.getHoraIngreso() + ".";
    }

    public String registrarEgreso(String documento, Guardia guardia, LocalDateTime fechaHora)
            throws ReglaNegocioException {
        RegistroAcceso registro = guardia.registrarEgreso(documento, lotes.listar(), fechaHora);
        lotes.actualizar(registro.getAutorizacion().getLote());
        return "Egreso registrado: " + registro.getAutorizacion().getPersona().getNombre()
                + " a las " + registro.getHoraEgreso() + ".";
    }

    public List<String> autorizacionesDelLote(String numeroLote) {
        List<String> resultado = new ArrayList<>();
        for (Autorizacion aut : obtenerLote(numeroLote).getAutorizaciones())
            resultado.add(aut.getPersona().getNombre() + " (" + aut.getDocumentoVisitante() + ", " + aut.getPersona().getTipo()
                    + ") | " + aut.getFechaDesde() + " a " + aut.getFechaHasta() + " | " + aut.getHoraDesde()
                    + "-" + aut.getHoraHasta() + (aut.isRecurrente() ? " | RECURRENTE" : "")
                    + " | Autorizó: " + aut.getAutorizante().getNombre());
        return resultado;
    }

    // Personas que ingresaron y todavía no registraron su egreso.
    public List<String> personasDentroDelBarrio() {
        List<String> resultado = new ArrayList<>();
        for (Lote lote : lotes.listar())
            for (Autorizacion aut : lote.getAutorizaciones())
                if (aut.getIngresoAbierto() != null)
                    resultado.add(aut.getPersona().getNombre() + " (" + aut.getDocumentoVisitante() + ") | Lote "
                            + lote.getNumero() + " | Ingresó " + aut.getIngresoAbierto().getHoraIngreso());
        return resultado;
    }

    private Lote obtenerLote(String numero) {
        return lotes.buscarPorId(numero).orElseThrow(() -> new IllegalArgumentException("No existe el lote " + numero + "."));
    }
}
