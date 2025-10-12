package procesosreales;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lanzador que crea múltiples procesos Java (JVMs separadas) para los filósofos.
 * 
 * Este programa usa ProcessBuilder para crear procesos REALES del sistema operativo,
 * no threads dentro de una sola JVM. Cada filósofo correrá en su propia JVM.
 * 
 * Arquitectura:
 * - 1 proceso servidor (MesaServer) que coordina el acceso a recursos
 * - N procesos cliente (FilosofoClient), uno por cada filósofo
 * - Comunicación mediante sockets TCP (IPC)
 * 
 * Uso:
 *   java procesosreales.Launcher [num_filosofos] [duracion_segundos]
 * 
 * Ejemplo:
 *   java procesosreales.Launcher 5 30
 */
public class Launcher {
    private static final String HOST = "localhost";
    private static final int PUERTO = 9999;
    
    public static void main(String[] args) {
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
        System.out.println("FILOSOFOS COMENSALES - PROCESOS REALES CON MULTIPLES JVMs");
        System.out.println("=".repeat(70));
        System.out.println("Configuracion:");
        System.out.println("  - Numero de filosofos: " + numFilosofos);
        System.out.println("  - Duracion: " + duracion + " segundos");
        System.out.println("  - Host del servidor: " + HOST);
        System.out.println("  - Puerto: " + PUERTO);
        System.out.println("=".repeat(70) + "\n");
        
        Process procesoServidor = null;
        List<Process> procesosFilosofos = new ArrayList<>();
        
        try {
            // 1. Obtener el classpath actual
            String classpath = System.getProperty("java.class.path");
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            
            // 2. Iniciar el servidor en un proceso separado
            System.out.println("Iniciando servidor en proceso separado...");
            ProcessBuilder pbServidor = new ProcessBuilder(
                javaBin,
                "-cp", classpath,
                "procesosreales.MesaServer",
                String.valueOf(numFilosofos),
                String.valueOf(PUERTO)
            );
            pbServidor.inheritIO(); // Heredar stdin/stdout/stderr
            procesoServidor = pbServidor.start();
            
            // Esperar a que el servidor esté listo
            System.out.println("Esperando a que el servidor esté listo...");
            Thread.sleep(2000);
            
            // 3. Iniciar cada filósofo en su propio proceso
            System.out.println("\nIniciando " + numFilosofos + " procesos de filosofos...\n");
            for (int i = 0; i < numFilosofos; i++) {
                ProcessBuilder pbFilosofo = new ProcessBuilder(
                    javaBin,
                    "-cp", classpath,
                    "procesosreales.FilosofoClient",
                    String.valueOf(i),
                    HOST,
                    String.valueOf(PUERTO)
                );
                pbFilosofo.inheritIO();
                Process proceso = pbFilosofo.start();
                procesosFilosofos.add(proceso);
                
                long pid = proceso.pid();
                System.out.println("Filosofo " + i + " iniciado en proceso PID: " + pid);
                
                // Pequena pausa entre inicios
                Thread.sleep(500);
            }
            
            System.out.println("\n" + "=".repeat(70));
            System.out.println("TODOS LOS PROCESOS INICIADOS");
            System.out.println("=".repeat(70));
            System.out.println("\nSimulacion corriendo por " + duracion + " segundos...");
            System.out.println("Total de procesos Java: " + (1 + numFilosofos) + 
                             " (1 servidor + " + numFilosofos + " filosofos)");
            System.out.println("\nPresiona Ctrl+C para detener antes.\n");
            
            // 4. Ejecutar por el tiempo especificado
            Thread.sleep(duracion * 1000L);
            
            System.out.println("\n" + "=".repeat(70));
            System.out.println("Tiempo de simulacion completado");
            System.out.println("=".repeat(70) + "\n");
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 5. Terminar todos los procesos
            System.out.println("Terminando procesos de filosofos...");
            for (int i = 0; i < procesosFilosofos.size(); i++) {
                Process p = procesosFilosofos.get(i);
                if (p.isAlive()) {
                    System.out.println("  Terminando Filosofo " + i + " (PID " + p.pid() + ")");
                    p.destroy();
                    try {
                        p.waitFor();
                    } catch (InterruptedException e) {
                        p.destroyForcibly();
                    }
                }
            }
            
            System.out.println("\nTerminando servidor...");
            if (procesoServidor != null && procesoServidor.isAlive()) {
                procesoServidor.destroy();
                try {
                    procesoServidor.waitFor();
                } catch (InterruptedException e) {
                    procesoServidor.destroyForcibly();
                }
            }
            
            System.out.println("\n" + "=".repeat(70));
            System.out.println("Fin de la simulacion - Todos los procesos terminados");
            System.out.println("=".repeat(70) + "\n");
        }
    }
}
