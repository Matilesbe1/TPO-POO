package vista;

import java.util.Scanner;

public class VistaPrincipal {
    private final Scanner scanner;
    private final VistaReclamos vistaReclamos;
    public VistaPrincipal(Scanner scanner, VistaReclamos vistaReclamos) {
        this.scanner = scanner; this.vistaReclamos = vistaReclamos;
    }
    public void iniciar() {
        boolean seguir = true;
        while (seguir) {
            System.out.println("\n=== BARRIO LOS AROMOS ===");
            System.out.println("1. Reclamos");
            System.out.println("2. Expensas (pendiente de integración)");
            System.out.println("3. Equipos (pendiente de integración)");
            System.out.println("4. Accesos (pendiente de integración)");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            String opcion = scanner.nextLine();
            switch (opcion) {
                case "1": vistaReclamos.iniciar(); break;
                case "0": seguir = false; break;
                case "2": case "3": case "4":
                    System.out.println("Este módulo lo desarrolla otro integrante.");
                    break;
                default: System.out.println("Opción inválida.");
            }
        }
    }
}
