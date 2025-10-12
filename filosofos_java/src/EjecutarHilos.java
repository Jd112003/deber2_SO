import hilos.Mesa;

/**
 * Script para ejecutar únicamente la solución con HILOS.
 * 
 * Uso:
 *     java EjecutarHilos [num_filosofos] [duracion_segundos]
 * 
 * Ejemplo:
 *     java EjecutarHilos 5 30
 */
public class EjecutarHilos {
    
    public static void main(String[] args) {
        // Parsear argumentos de línea de comandos
        int numFilosofos = 5;
        int duracion = 30;
        
        if (args.length > 0) {
            try {
                numFilosofos = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Error: El número de filósofos debe ser un entero.");
                System.exit(1);
            }
        }
        
        if (args.length > 1) {
            try {
                duracion = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Error: La duración debe ser un entero.");
                System.exit(1);
            }
        }
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PROBLEMA DE LOS FILÓSOFOS COMENSALES - SOLUCIÓN CON HILOS");
        System.out.println("=".repeat(70));
        System.out.println("Configuración:");
        System.out.println("  - Número de filósofos: " + numFilosofos);
        System.out.println("  - Duración: " + duracion + " segundos");
        System.out.println("=".repeat(70) + "\n");
        
        try {
            // Crear la mesa
            Mesa mesa = new Mesa(numFilosofos);
            
            // Iniciar la simulación
            mesa.iniciarCena();
            
            System.out.println("\nSimulación corriendo por " + duracion + " segundos...");
            System.out.println("Presiona Ctrl+C para detener antes.\n");
            
            // Ejecutar por el tiempo especificado
            Thread.sleep(duracion * 1000L);
            
            System.out.println("\n" + "=".repeat(70));
            System.out.println("Fin de la simulación");
            System.out.println("=".repeat(70) + "\n");
            
        } catch (InterruptedException e) {
            System.out.println("\n\nSimulación interrumpida por el usuario.\n");
        }
        
        // Dar tiempo para que los hilos terminen sus operaciones actuales
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.exit(0);
    }
}
