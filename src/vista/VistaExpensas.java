package vista;

import controlador.ControladorExpensas;
import modelo.excepciones.ReglaNegocioException;
import modelo.expensas.MedioPago;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class VistaExpensas {
    private final Scanner scanner;
    private final ControladorExpensas controlador;

    public VistaExpensas(Scanner scanner, ControladorExpensas controlador) {
        this.scanner = scanner; this.controlador = controlador;
    }
    public void iniciar() {
        boolean seguir = true;
        while (seguir) {
            System.out.println("\n--- EXPENSAS ---");
            System.out.println("1. Emitir expensas del mes");
            System.out.println("2. Registrar pago");
            System.out.println("3. Registrar ajuste de un pago");
            System.out.println("4. Estado de cuenta de un lote");
            System.out.println("5. Reporte de morosidad");
            System.out.println("6. Notificaciones de un lote");
            System.out.println("0. Volver");
            System.out.print("Opción: ");
            String opcion = scanner.nextLine();
            try {
                switch (opcion) {
                    case "1": emitir(); break;
                    case "2": pagar(); break;
                    case "3": ajustar(); break;
                    case "4": estadoDeCuenta(); break;
                    case "5": mostrar(controlador.reporteMorosidad(pedirFecha()), "No hay lotes morosos."); break;
                    case "6": notificaciones(); break;
                    case "0": seguir = false; break;
                    default: System.out.println("Opción inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Tenés que ingresar un número válido. Volvé a intentarlo.");
            } catch (ReglaNegocioException | IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (DateTimeParseException e) {
                System.out.println("Fecha o período con formato inválido. Volvé a intentarlo.");
            } catch (IllegalStateException e) {
                System.out.println("Error al guardar los datos: " + e.getMessage());
            }
        }
    }
    private void emitir() {
        YearMonth periodo = pedirPeriodo();
        System.out.print("Presupuesto del mes: "); double presupuesto = Double.parseDouble(scanner.nextLine());
        mostrar(controlador.emitirExpensas(periodo, presupuesto), "No se emitió ninguna expensa.");
    }
    private void pagar() throws ReglaNegocioException {
        System.out.print("Número de lote: "); String lote = scanner.nextLine();
        System.out.print("Importe: "); double importe = Double.parseDouble(scanner.nextLine());
        MedioPago medio = pedirMedioPago();
        LocalDate fecha = pedirFecha();
        List<String> imputacion = controlador.registrarPago(lote, fecha, importe, medio);
        System.out.println("Pago registrado. Imputación:");
        mostrar(imputacion, "");
    }
    private void ajustar() throws ReglaNegocioException {
        System.out.print("Número de lote: "); String lote = scanner.nextLine();
        YearMonth periodo = pedirPeriodo();
        System.out.print("Número de pago (ver estado de cuenta): "); int numeroPago = Integer.parseInt(scanner.nextLine());
        System.out.print("Importe del ajuste (positivo suma al pago, negativo lo descuenta): ");
        double importe = Double.parseDouble(scanner.nextLine());
        System.out.print("Motivo: "); String motivo = scanner.nextLine();
        LocalDate fecha = pedirFecha();
        System.out.println(controlador.registrarAjuste(lote, periodo, numeroPago, fecha, motivo, importe));
    }
    private void estadoDeCuenta() {
        System.out.print("Número de lote: "); String lote = scanner.nextLine();
        mostrar(controlador.estadoDeCuenta(lote, pedirFecha()), "");
    }
    private void notificaciones() {
        System.out.print("Número de lote: "); String lote = scanner.nextLine();
        mostrar(controlador.notificaciones(lote), "El lote no tiene notificaciones.");
    }

    private YearMonth pedirPeriodo() {
        System.out.print("Período (AAAA-MM): ");
        return YearMonth.parse(scanner.nextLine().trim());
    }
    // La fecha la pide la vista: el modelo nunca consulta la fecha actual.
    private LocalDate pedirFecha() {
        System.out.print("Fecha (AAAA-MM-DD, Enter = hoy): ");
        String texto = scanner.nextLine().trim();
        return texto.isEmpty() ? LocalDate.now() : LocalDate.parse(texto);
    }
    private MedioPago pedirMedioPago() {
        System.out.println("Medio de pago: 1 EFECTIVO, 2 TRANSFERENCIA, 3 TARJETA_CREDITO");
        int opcion = Integer.parseInt(scanner.nextLine());
        if (opcion < 1 || opcion > MedioPago.values().length) throw new IllegalArgumentException("Medio de pago inválido.");
        return MedioPago.values()[opcion - 1];
    }
    private void mostrar(List<String> lineas, String siVacio) {
        if (lineas.isEmpty() && !siVacio.isEmpty()) System.out.println(siVacio);
        for (String linea : lineas) System.out.println(linea);
    }
}
