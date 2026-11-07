package vista;

import controlador.ControladorReclamos;
import modelo.personas.*;
import modelo.reclamos.*;
import modelo.excepciones.ReglaNegocioException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class VistaReclamos {
    private final Scanner scanner;
    private final ControladorReclamos controlador;
    private final Administrador administrador = new Administrador();

    public VistaReclamos(Scanner scanner, ControladorReclamos controlador) {
        this.scanner = scanner; this.controlador = controlador;
    }
    public void iniciar() {
        boolean seguir = true;
        while (seguir) {
            System.out.println("\n--- RECLAMOS ---");
            System.out.println("1. Alta vecino");
            System.out.println("2. Alta lote");
            System.out.println("3. Presentar reclamo");
            System.out.println("4. Asignar reclamo");
            System.out.println("5. Avanzar estado");
            System.out.println("6. Reabrir reclamo");
            System.out.println("7. Reporte de pendientes");
            System.out.println("8. Agregar residente a un lote");
            System.out.println("9. Comentar un reclamo");
            System.out.println("10. Revisar plazos (escalar y cierre automático)");
            System.out.println("0. Volver");
            System.out.print("Opción: ");
            String opcion = scanner.nextLine();
            try {
                switch (opcion) {
                    case "1": altaVecino(); break;
                    case "2": altaLote(); break;
                    case "3": presentar(); break;
                    case "4": asignar(); break;
                    case "5": avanzar(); break;
                    case "6": reabrir(); break;
                    case "7": controlador.reportePendientes().forEach(System.out::println); break;
                    case "8": agregarResidente(); break;
                    case "9": comentar(); break;
                    case "10": revisarPlazos(); break;
                    case "0": seguir = false; break;
                    default: System.out.println("Opción inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Tenés que ingresar un número válido. Volvé a intentarlo.");
            } catch (DateTimeParseException e) {
                System.out.println("Fecha con formato inválido. Volvé a intentarlo.");
            } catch (ReglaNegocioException | IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (InputMismatchException e) {
                System.out.println("Ingresaste un dato numérico inválido. Volvé a intentarlo.");
                scanner.nextLine();
            }
        }
    }
    private void altaVecino() {
        System.out.print("Documento: "); String doc = scanner.nextLine();
        System.out.print("Nombre: "); String nombre = scanner.nextLine();
        controlador.altaVecino(doc, nombre);
        System.out.println("Vecino registrado.");
    }
    private void altaLote() {
        System.out.print("Número de lote: "); String numero = scanner.nextLine();
        System.out.print("Superficie: "); double superficie = Double.parseDouble(scanner.nextLine());
        System.out.print("Coeficiente: "); double coef = Double.parseDouble(scanner.nextLine());
        System.out.print("Documento del titular: "); String doc = scanner.nextLine();
        controlador.altaLote(numero, superficie, coef, doc);
        System.out.println("Lote registrado.");
    }
    private void presentar() throws ReglaNegocioException {
        System.out.print("Documento del vecino: "); String doc = scanner.nextLine();
        System.out.print("Número de lote: "); String lote = scanner.nextLine();
        System.out.println("Tipo: 1 SEGURIDAD, 2 MANTENIMIENTO, 3 CONVIVENCIA, 4 ADMINISTRATIVO");
        TipoReclamo tipo = TipoReclamo.values()[elegirOpcion(TipoReclamo.values().length) - 1];
        System.out.print("Descripción: "); String descripcion = scanner.nextLine();
        String id = controlador.presentarReclamo(doc, lote, tipo, descripcion, pedirFechaHora());
        System.out.println("Reclamo creado. ID: " + id);
    }
    private void asignar() throws ReglaNegocioException {
        System.out.print("ID del reclamo: "); String id = scanner.nextLine();
        System.out.print("Legajo del empleado: "); String legajo = scanner.nextLine();
        System.out.print("Nombre del empleado: "); String nombre = scanner.nextLine();
        controlador.asignar(id, new EmpleadoMantenimiento(legajo, nombre), pedirFechaHora(), administrador);
        System.out.println("Reclamo asignado.");
    }
    private void avanzar() throws ReglaNegocioException {
        EstadoReclamo[] opciones = { EstadoReclamo.EN_CURSO, EstadoReclamo.RESUELTO, EstadoReclamo.CERRADO };
        System.out.print("ID del reclamo: "); String id = scanner.nextLine();
        System.out.println("Nuevo estado: 1 EN_CURSO, 2 RESUELTO, 3 CERRADO");
        EstadoReclamo estado = opciones[elegirOpcion(opciones.length) - 1];
        System.out.print("Observación: "); String obs = scanner.nextLine();
        controlador.avanzar(id, estado, obs, pedirFechaHora());
        System.out.println("Estado actualizado.");
    }
    private void reabrir() throws ReglaNegocioException {
        System.out.print("ID del reclamo: "); String id = scanner.nextLine();
        controlador.reabrir(id, pedirFechaHora());
        System.out.println("Reclamo reabierto.");
    }
    private void agregarResidente() {
        System.out.print("Número de lote: "); String lote = scanner.nextLine();
        System.out.print("Documento del residente (ya dado de alta como vecino): "); String doc = scanner.nextLine();
        controlador.agregarResidente(lote, doc);
        System.out.println("Residente agregado.");
    }
    private void comentar() {
        System.out.print("ID del reclamo: "); String id = scanner.nextLine();
        System.out.print("Autor: "); String autor = scanner.nextLine();
        System.out.print("Comentario: "); String texto = scanner.nextLine();
        controlador.comentar(id, autor, texto, pedirFechaHora());
        System.out.println("Comentario agregado.");
    }
    private void revisarPlazos() {
        List<String> resultado = controlador.revisarPlazos(pedirFechaHora());
        if (resultado.isEmpty()) System.out.println("No hay reclamos para escalar ni cerrar.");
        for (String linea : resultado) System.out.println(linea);
    }

    // La fecha la pide la vista: el modelo nunca consulta la fecha actual.
    private LocalDateTime pedirFechaHora() {
        System.out.print("Fecha y hora (AAAA-MM-DDTHH:MM, Enter = ahora): ");
        String texto = scanner.nextLine().trim();
        return texto.isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(texto);
    }
    private int elegirOpcion(int cantidad) {
        int opcion = Integer.parseInt(scanner.nextLine());
        if (opcion < 1 || opcion > cantidad) throw new IllegalArgumentException("Opción inválida.");
        return opcion;
    }
}
