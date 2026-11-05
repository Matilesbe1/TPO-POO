package vista;

import controlador.ControladorReclamos;
import modelo.personas.*;
import modelo.reclamos.*;
import modelo.excepciones.ReglaNegocioException;
import java.time.LocalDateTime;
import java.util.InputMismatchException;
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
                    case "0": seguir = false; break;
                    default: System.out.println("Opción inválida.");
                }
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
        TipoReclamo tipo = TipoReclamo.values()[Integer.parseInt(scanner.nextLine()) - 1];
        System.out.print("Descripción: "); String descripcion = scanner.nextLine();
        String id = controlador.presentarReclamo(doc, lote, tipo, descripcion, LocalDateTime.now());
        System.out.println("Reclamo creado. ID: " + id);
    }
    private void asignar() throws ReglaNegocioException {
        System.out.print("ID del reclamo: "); String id = scanner.nextLine();
        System.out.print("Legajo del empleado: "); String legajo = scanner.nextLine();
        System.out.print("Nombre del empleado: "); String nombre = scanner.nextLine();
        controlador.asignar(id, new EmpleadoMantenimiento(legajo, nombre), LocalDateTime.now(), administrador);
        System.out.println("Reclamo asignado.");
    }
    private void avanzar() throws ReglaNegocioException {
        System.out.print("ID del reclamo: "); String id = scanner.nextLine();
        System.out.println("Nuevo estado: 1 EN_CURSO, 2 RESUELTO, 3 CERRADO");
        EstadoReclamo estado = EstadoReclamo.values()[Integer.parseInt(scanner.nextLine())];
        System.out.print("Observación: "); String obs = scanner.nextLine();
        controlador.avanzar(id, estado, obs, LocalDateTime.now());
        System.out.println("Estado actualizado.");
    }
    private void reabrir() throws ReglaNegocioException {
        System.out.print("ID del reclamo: "); String id = scanner.nextLine();
        controlador.reabrir(id, LocalDateTime.now());
        System.out.println("Reclamo reabierto.");
    }
}
