import procesos.MesaIPC;
import procesos.ProcesoFilosofo;
import java.util.ArrayList;
import java.util.List;

/**
 * Script para ejecutar la solución con PROCESOS (simulados con threads IPC).
 * 
 * Nota: Java no soporta fork() verdadero como Unix/Linux. Esta implementación
 * simula procesos usando threads con comunicación mediante semáforos y memoria atómica.
 * 
 * Uso:
 *     java EjecutarProcesos [num_filosofos] [duracion_segundos]
 * 
 * Ejemplo:
 *     java EjecutarProcesos 5 30
 */
public class EjecutarProcesos {
    
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
        System.out.println("PROBLEMA DE LOS FILÓSOFOS COMENSALES - SOLUCIÓN CON PROCESOS");
        System.out.println("=".repeat(70));
        System.out.println("Configuración:");
        System.out.println("  - Número de filósofos: " + numFilosofos);
        System.out.println("  - Duración: " + duracion + " segundos");
        System.out.println("=".repeat(70) + "\n");
        
        MesaIPC mesaIPC = null;
        List<ProcesoFilosofo> filosofos = new ArrayList<>();
        
        try {
            // Crear la mesa IPC
            mesaIPC = new MesaIPC(numFilosofos);
            
            // Crear los filósofos
            for (int i = 0; i < numFilosofos; i++) {
                filosofos.add(new ProcesoFilosofo(i, mesaIPC));
            }
            
            System.out.println("Iniciando procesos...\n");
            
            // Iniciar todos los procesos
            for (ProcesoFilosofo filosofo : filosofos) {
                filosofo.iniciar();
            }
            
            System.out.println("\nSimulación corriendo por " + duracion + " segundos...");
            System.out.println("Presiona Ctrl+C para detener antes.\n");
            
            // Ejecutar por el tiempo especificado
            Thread.sleep(duracion * 1000L);
            
        } catch (InterruptedException e) {
            System.out.println("\n\nSimulación interrumpida por el usuario.\n");
        } finally {
            // Terminar todos los procesos
            System.out.println("\nTerminando procesos...");
            for (ProcesoFilosofo filosofo : filosofos) {
                filosofo.terminar();
            }
            
            // Dar tiempo para que terminen
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("\n" + "=".repeat(70));
            System.out.println("Fin de la simulación");
            System.out.println("=".repeat(70) + "\n");
            
            // Imprimir estadísticas finales (después de que todos terminaron)
            if (mesaIPC != null) {
                mesaIPC.imprimirEstadisticas();
            }
        }
        
        // Dar tiempo para que los threads terminen
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.exit(0);
    }
}
