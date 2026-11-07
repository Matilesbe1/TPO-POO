package vista;

import java.util.Scanner;

public class VistaPrincipal {
    private final Scanner scanner;
    private final VistaReclamos vistaReclamos;
    private final VistaExpensas vistaExpensas;
    private final VistaEquipos vistaEquipos;
    private final VistaAccesos vistaAccesos;
    public VistaPrincipal(Scanner scanner, VistaReclamos vistaReclamos, VistaExpensas vistaExpensas,
                          VistaEquipos vistaEquipos, VistaAccesos vistaAccesos) {
        this.scanner = scanner; this.vistaReclamos = vistaReclamos; this.vistaExpensas = vistaExpensas;
        this.vistaEquipos = vistaEquipos; this.vistaAccesos = vistaAccesos;
    }
    public void iniciar() {
        boolean seguir = true;
        while (seguir) {
            System.out.println("\n=== BARRIO LOS AROMOS ===");
            System.out.println("1. Vecinos, lotes y reclamos");
            System.out.println("2. Expensas y pagos");
            System.out.println("3. Equipos y mantenimiento");
            System.out.println("4. Visitas y proveedores");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            String opcion = scanner.nextLine();
            switch (opcion) {
                case "1": vistaReclamos.iniciar(); break;
                case "2": vistaExpensas.iniciar(); break;
                case "3": vistaEquipos.iniciar(); break;
                case "4": vistaAccesos.iniciar(); break;
                case "0": seguir = false; break;
                default: System.out.println("Opción inválida.");
            }
        }
    }
}
