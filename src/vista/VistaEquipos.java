package vista;

import controlador.ControladorEquipos;
import modelo.excepciones.ReglaNegocioException;
import modelo.personas.Administrador;
import modelo.personas.EmpleadoMantenimiento;
import modelo.personas.Guardia;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class VistaEquipos {
    private final Scanner scanner;
    private final ControladorEquipos controlador;
    private final Administrador administrador = new Administrador();

    public VistaEquipos(Scanner scanner, ControladorEquipos controlador) {
        this.scanner = scanner; this.controlador = controlador;
    }
    public void iniciar() {
        boolean seguir = true;
        while (seguir) {
            System.out.println("\n--- EQUIPOS Y MANTENIMIENTO ---");
            System.out.println("1. Alta de cámara");
            System.out.println("2. Alta de luminaria");
            System.out.println("3. Registrar falla (guardia)");
            System.out.println("4. Asignar orden a un empleado (administrador)");
            System.out.println("5. Cerrar orden con la reparación (empleado)");
            System.out.println("6. Estado de los equipos");
            System.out.println("7. Historial de mantenimiento de un equipo");
            System.out.println("8. Alertas al administrador (cámaras sin funcionar más de 48 h)");
            System.out.println("9. Reporte de equipos fuera de servicio");
            System.out.println("0. Volver");
            System.out.print("Opción: ");
            String opcion = scanner.nextLine();
            try {
                switch (opcion) {
                    case "1": altaCamara(); break;
                    case "2": altaLuminaria(); break;
                    case "3": registrarFalla(); break;
                    case "4": asignarOrden(); break;
                    case "5": cerrarOrden(); break;
                    case "6": mostrar(controlador.estadoDeEquipos(), "No hay equipos registrados."); break;
                    case "7": historial(); break;
                    case "8": mostrar(controlador.alertasAlAdministrador(pedirFechaHora()), "No hay cámaras para informar."); break;
                    case "9": mostrar(controlador.reporteFueraDeServicio(), "Todos los equipos están operativos."); break;
                    case "0": seguir = false; break;
                    default: System.out.println("Opción inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Tenés que ingresar un número válido. Volvé a intentarlo.");
            } catch (DateTimeParseException e) {
                System.out.println("Fecha con formato inválido. Volvé a intentarlo.");
            } catch (ReglaNegocioException | IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println("Error al guardar los datos: " + e.getMessage());
            }
        }
    }
    private void altaCamara() {
        System.out.print("Código: "); String codigo = scanner.nextLine();
        System.out.print("Sector: "); String sector = scanner.nextLine();
        System.out.print("Resolución (ej. 1080p): "); String resolucion = scanner.nextLine();
        System.out.print("Días de grabación: "); int dias = Integer.parseInt(scanner.nextLine());
        System.out.print("Fecha de instalación (AAAA-MM-DD, Enter = hoy): "); LocalDate fecha = pedirFecha();
        controlador.altaCamara(codigo, sector, fecha, resolucion, dias);
        System.out.println("Cámara registrada.");
    }
    private void altaLuminaria() {
        System.out.print("Código: "); String codigo = scanner.nextLine();
        System.out.print("Sector: "); String sector = scanner.nextLine();
        System.out.print("Tipo de lámpara (ej. LED): "); String tipo = scanner.nextLine();
        System.out.print("Potencia (W): "); double potencia = Double.parseDouble(scanner.nextLine());
        System.out.print("Fecha de instalación (AAAA-MM-DD, Enter = hoy): "); LocalDate fecha = pedirFecha();
        controlador.altaLuminaria(codigo, sector, fecha, tipo, potencia);
        System.out.println("Luminaria registrada.");
    }
    private void registrarFalla() throws ReglaNegocioException {
        System.out.print("Código del equipo: "); String codigo = scanner.nextLine();
        System.out.print("Descripción de la falla: "); String descripcion = scanner.nextLine();
        Guardia guardia = pedirGuardia();
        System.out.println(controlador.registrarFalla(codigo, guardia, descripcion, pedirFechaHora()));
    }
    private void asignarOrden() {
        System.out.print("Código del equipo: "); String codigo = scanner.nextLine();
        System.out.println(controlador.asignarOrden(codigo, pedirEmpleado(), administrador));
    }
    private void cerrarOrden() throws ReglaNegocioException {
        System.out.print("Código del equipo: "); String codigo = scanner.nextLine();
        EmpleadoMantenimiento empleado = pedirEmpleado();
        System.out.print("Descripción de la reparación: "); String descripcion = scanner.nextLine();
        System.out.print("Fecha de cierre (AAAA-MM-DD, Enter = hoy): "); LocalDate fecha = pedirFecha();
        System.out.println(controlador.cerrarOrden(codigo, empleado, descripcion, fecha));
    }
    private void historial() {
        System.out.print("Código del equipo: "); String codigo = scanner.nextLine();
        mostrar(controlador.historial(codigo), "");
    }

    private Guardia pedirGuardia() {
        System.out.print("Legajo del guardia: "); String legajo = scanner.nextLine();
        System.out.print("Nombre del guardia: "); String nombre = scanner.nextLine();
        return new Guardia(legajo, nombre);
    }
    private EmpleadoMantenimiento pedirEmpleado() {
        System.out.print("Legajo del empleado: "); String legajo = scanner.nextLine();
        System.out.print("Nombre del empleado: "); String nombre = scanner.nextLine();
        return new EmpleadoMantenimiento(legajo, nombre);
    }
    // La fecha la pide la vista: el modelo nunca consulta la fecha actual.
    private LocalDate pedirFecha() {
        String texto = scanner.nextLine().trim();
        return texto.isEmpty() ? LocalDate.now() : LocalDate.parse(texto);
    }
    private LocalDateTime pedirFechaHora() {
        System.out.print("Fecha y hora (AAAA-MM-DDTHH:MM, Enter = ahora): ");
        String texto = scanner.nextLine().trim();
        return texto.isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(texto);
    }
    private void mostrar(List<String> lineas, String siVacio) {
        if (lineas.isEmpty() && !siVacio.isEmpty()) System.out.println(siVacio);
        for (String linea : lineas) System.out.println(linea);
    }
}
