package controlador;

import modelo.equipos.Camara;
import modelo.equipos.Equipo;
import modelo.equipos.Luminaria;
import modelo.equipos.OrdenMantenimiento;
import modelo.excepciones.ReglaNegocioException;
import modelo.personas.Administrador;
import modelo.personas.EmpleadoMantenimiento;
import modelo.personas.Guardia;
import modelo.reportes.ReporteEquiposFueraServicio;
import persistencia.Repositorio;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Controlador: recibe los pedidos de la vista, busca los equipos y le delega las reglas al modelo.
public class ControladorEquipos {
    private final Repositorio<Equipo> equipos;

    public ControladorEquipos(Repositorio<Equipo> equipos) {
        this.equipos = equipos;
    }

    public void altaCamara(String codigo, String sector, LocalDate instalacion, String resolucion, int diasGrabacion) {
        validarCodigoLibre(codigo);
        equipos.guardar(new Camara(codigo, sector, instalacion, resolucion, diasGrabacion));
    }
    public void altaLuminaria(String codigo, String sector, LocalDate instalacion, String tipoLampara, double potencia) {
        validarCodigoLibre(codigo);
        equipos.guardar(new Luminaria(codigo, sector, instalacion, tipoLampara, potencia));
    }

    public String registrarFalla(String codigo, Guardia guardia, String descripcion, LocalDateTime fechaHora)
            throws ReglaNegocioException {
        Equipo equipo = obtenerEquipo(codigo);
        OrdenMantenimiento orden = guardia.registrarFalla(equipo, equipos.listar(), descripcion, fechaHora);
        equipos.actualizar(equipo);
        return "Orden " + orden.getNumero() + " generada con prioridad " + orden.getPrioridad() + ".";
    }

    public String asignarOrden(String codigo, EmpleadoMantenimiento empleado, Administrador administrador) {
        Equipo equipo = obtenerEquipo(codigo);
        OrdenMantenimiento orden = equipo.getOrdenAbierta();
        administrador.asignarOrden(orden, empleado);
        equipos.actualizar(equipo);
        return "Orden " + orden.getNumero() + " asignada a " + empleado.getNombre() + ".";
    }

    public String cerrarOrden(String codigo, EmpleadoMantenimiento empleado, String descripcion, LocalDate fecha)
            throws ReglaNegocioException {
        Equipo equipo = obtenerEquipo(codigo);
        OrdenMantenimiento orden = equipo.getOrdenAbierta();
        empleado.registrarReparacion(orden, fecha, descripcion);
        equipos.actualizar(equipo);
        return "Orden " + orden.getNumero() + " cerrada. El equipo vuelve a estar " + equipo.getEstado() + ".";
    }

    public List<String> estadoDeEquipos() {
        List<String> resultado = new ArrayList<>();
        for (Equipo equipo : equipos.listar())
            resultado.add(equipo.getTipo() + " " + equipo.getCodigo() + " | Sector " + equipo.getSector()
                    + " | Instalado " + equipo.getFechaInstalacion() + " | " + equipo.getEstado());
        return resultado;
    }

    // RF04: historial de mantenimiento del equipo.
    public List<String> historial(String codigo) {
        Equipo equipo = obtenerEquipo(codigo);
        List<String> resultado = new ArrayList<>();
        resultado.add(equipo.getTipo() + " " + equipo.getCodigo() + " - Estado actual: " + equipo.getEstado());
        for (OrdenMantenimiento orden : equipo.getHistorial())
            resultado.add("  Orden " + orden.getNumero() + " | " + orden.getPrioridad()
                    + " | Apertura " + orden.getFechaApertura() + " | " + orden.getDescripcionTrabajo()
                    + (orden.getTecnico() != null ? " | Técnico: " + orden.getTecnico().getNombre() : "")
                    + (orden.isCerrada() ? " | Cierre " + orden.getFechaCierre() + ": " + orden.getDescripcionReparacion()
                                         : " | ABIERTA"));
        return resultado;
    }

    // RN03.5: cámaras que llevan más de 48 horas sin funcionar.
    public List<String> alertasAlAdministrador(LocalDateTime fechaHora) {
        List<String> resultado = new ArrayList<>();
        for (Equipo equipo : equipos.listar())
            if (equipo.requiereAvisoAdministrador(fechaHora))
                resultado.add("AVISO: " + equipo.getTipo() + " " + equipo.getCodigo() + " (sector " + equipo.getSector()
                        + ") sin funcionar desde " + equipo.getFechaHoraFalla());
        return resultado;
    }

    public List<String> reporteFueraDeServicio() {
        return new ReporteEquiposFueraServicio(equipos.listar()).generar();
    }

    private void validarCodigoLibre(String codigo) {
        if (equipos.buscarPorId(codigo).isPresent())
            throw new IllegalArgumentException("Ya existe un equipo con el código " + codigo + ".");
    }
    private Equipo obtenerEquipo(String codigo) {
        return equipos.buscarPorId(codigo).orElseThrow(() -> new IllegalArgumentException("No existe el equipo " + codigo + "."));
    }
}
