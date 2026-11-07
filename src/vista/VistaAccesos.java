package vista;

import controlador.ControladorAccesos;
import modelo.excepciones.ReglaNegocioException;
import modelo.personas.Guardia;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class VistaAccesos {
    private final Scanner scanner;
    private final ControladorAccesos controlador;

    public VistaAccesos(Scanner scanner, ControladorAccesos controlador) {
        this.scanner = scanner; this.controlador = controlador;
    }
    public void iniciar() {
        boolean seguir = true;
        while (seguir) {
            System.out.println("\n--- VISITAS Y PROVEEDORES ---");
            System.out.println("1. Autorizar visita o proveedor (vecino)");
            System.out.println("2. Registrar ingreso (guardia)");
            System.out.println("3. Registrar egreso (guardia)");
            System.out.println("4. Autorizaciones de un lote");
            System.out.println("5. Personas dentro del barrio");
            System.out.println("0. Volver");
            System.out.print("Opción: ");
            String opcion = scanner.nextLine();
            try {
                switch (opcion) {
                    case "1": autorizar(); break;
                    case "2": ingreso(); break;
                    case "3": egreso(); break;
                    case "4": autorizaciones(); break;
                    case "5": mostrar(controlador.personasDentroDelBarrio(), "No hay personas dentro del barrio."); break;
                    case "0": seguir = false; break;
                    default: System.out.println("Opción inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Tenés que ingresar un número válido. Volvé a intentarlo.");
            } catch (DateTimeParseException e) {
                System.out.println("Fecha u hora con formato inválido. Volvé a intentarlo.");
            } catch (ReglaNegocioException | IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println("Error al guardar los datos: " + e.getMessage());
            }
        }
    }
    private void autorizar() throws ReglaNegocioException {
        System.out.print("Documento del vecino que autoriza: "); String vecino = scanner.nextLine();
        System.out.print("Número de lote de destino: "); String lote = scanner.nextLine();
        System.out.println("Tipo: 1 VISITANTE, 2 PROVEEDOR");
        String tipo = scanner.nextLine().trim();
        if (!tipo.equals("1") && !tipo.equals("2")) throw new IllegalArgumentException("Tipo inválido.");
        boolean esProveedor = tipo.equals("2");
        System.out.print("Nombre: "); String nombre = scanner.nextLine();
        System.out.print("Documento: "); String documento = scanner.nextLine();
        String servicio = "";
        boolean recurrente = false;
        if (esProveedor) {
            System.out.print("Servicio (ej. jardinería): "); servicio = scanner.nextLine();
            System.out.print("¿Autorización recurrente? (S/N): "); recurrente = scanner.nextLine().trim().equalsIgnoreCase("S");
        }
        System.out.print("Fecha desde (AAAA-MM-DD): "); LocalDate desde = LocalDate.parse(scanner.nextLine().trim());
        LocalDate hasta = desde;
        if (recurrente) { System.out.print("Fecha hasta (AAAA-MM-DD, máximo 30 días): "); hasta = LocalDate.parse(scanner.nextLine().trim()); }
        System.out.print("Hora desde (HH:MM): "); LocalTime horaDesde = LocalTime.parse(scanner.nextLine().trim());
        System.out.print("Hora hasta (HH:MM): "); LocalTime horaHasta = LocalTime.parse(scanner.nextLine().trim());
        System.out.println(controlador.autorizarVisita(vecino, lote, esProveedor, nombre, documento, servicio,
                desde, hasta, horaDesde, horaHasta, recurrente));
    }
    private void ingreso() throws ReglaNegocioException {
        System.out.print("Documento de la persona: "); String documento = scanner.nextLine();
        System.out.print("Número de lote de destino: "); String lote = scanner.nextLine();
        Guardia guardia = pedirGuardia();
        System.out.println(controlador.registrarIngreso(documento, lote, guardia, pedirFechaHora()));
    }
    private void egreso() throws ReglaNegocioException {
        System.out.print("Documento de la persona: "); String documento = scanner.nextLine();
        Guardia guardia = pedirGuardia();
        System.out.println(controlador.registrarEgreso(documento, guardia, pedirFechaHora()));
    }
    private void autorizaciones() {
        System.out.print("Número de lote: "); String lote = scanner.nextLine();
        mostrar(controlador.autorizacionesDelLote(lote), "El lote no tiene autorizaciones.");
    }

    private Guardia pedirGuardia() {
        System.out.print("Legajo del guardia: "); String legajo = scanner.nextLine();
        System.out.print("Nombre del guardia: "); String nombre = scanner.nextLine();
        return new Guardia(legajo, nombre);
    }
    // La fecha la pide la vista: el modelo nunca consulta la fecha actual.
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
