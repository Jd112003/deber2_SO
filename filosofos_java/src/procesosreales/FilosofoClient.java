package procesosreales;

import java.io.*;
import java.net.*;
import java.util.Random;

/**
 * Cliente filósofo que se ejecuta en un proceso Java SEPARADO (JVM independiente).
 * Se comunica con el servidor MesaServer mediante sockets TCP.
 * 
 * Cada instancia de esta clase corre en su PROPIA JVM, creando procesos
 * reales del sistema operativo (visibles en el Task Manager).
 */
public class FilosofoClient {
    private final int id;
    private final String servidorHost;
    private final int servidorPuerto;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final Random random;
    private volatile boolean activo = true;
    
    public FilosofoClient(int id, String servidorHost, int servidorPuerto) {
        this.id = id;
        this.servidorHost = servidorHost;
        this.servidorPuerto = servidorPuerto;
        this.random = new Random();
    }
    
    /**
     * Conecta al servidor de la mesa.
     */
    public void conectar() throws IOException {
        socket = new Socket(servidorHost, servidorPuerto);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        // Registrarse en el servidor
        out.println("REGISTER:" + id);
        String respuesta = in.readLine();
        
        if ("OK".equals(respuesta)) {
            long pid = ProcessHandle.current().pid();
            System.out.println("[PID " + pid + "] Filosofo " + id + " conectado al servidor");
        } else {
            throw new IOException("Error al registrarse: " + respuesta);
        }
    }
    
    /**
     * Desconecta del servidor.
     */
    public void desconectar() {
        activo = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error al desconectar: " + e.getMessage());
        }
    }
    
    /**
     * Simula el tiempo de pensamiento.
     */
    private void pensar() {
        double tiempo = 1.0 + random.nextDouble() * 2.0;
        long pid = ProcessHandle.current().pid();
        System.out.printf("[PID %d] Filosofo %d esta PENSANDO por %.2f segundos%n", 
                         pid, id, tiempo);
        try {
            Thread.sleep((long)(tiempo * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Simula el tiempo de comida.
     */
    private void comer() {
        double tiempo = 1.0 + random.nextDouble() * 2.0;
        long pid = ProcessHandle.current().pid();
        System.out.printf("[PID %d] Filosofo %d esta COMIENDO por %.2f segundos%n", 
                         pid, id, tiempo);
        try {
            Thread.sleep((long)(tiempo * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Solicita tenedores al servidor.
     */
    private void tomarTenedores() throws IOException {
        long pid = ProcessHandle.current().pid();
        System.out.printf("[PID %d] Filosofo %d esta HAMBRIENTO y solicita recursos%n", 
                         pid, id);
        
        out.println("TOMAR:" + id);
        String respuesta = in.readLine();
        
        if ("OK".equals(respuesta)) {
            System.out.printf("[PID %d] Filosofo %d obtuvo los recursos%n", pid, id);
        } else {
            throw new IOException("Error al tomar tenedores: " + respuesta);
        }
    }
    
    /**
     * Libera tenedores en el servidor.
     */
    private void soltarTenedores() throws IOException {
        long pid = ProcessHandle.current().pid();
        System.out.printf("[PID %d] Filosofo %d libera recursos%n", pid, id);
        
        out.println("SOLTAR:" + id);
        String respuesta = in.readLine();
        
        if (!"OK".equals(respuesta)) {
            throw new IOException("Error al soltar tenedores: " + respuesta);
        }
    }
    
    /**
     * Ciclo principal del filosofo: pensar -> pedir -> comer -> liberar.
     */
    public void ejecutar() {
        long pid = ProcessHandle.current().pid();
        System.out.println("[PID " + pid + "] Filosofo " + id + " iniciando ciclo principal");
        
        try {
            while (activo) {
                pensar();
                tomarTenedores();
                comer();
                soltarTenedores();
            }
        } catch (IOException e) {
            if (activo) {
                System.err.println("[PID " + pid + "] Error: " + e.getMessage());
            }
        } finally {
            System.out.println("[PID " + pid + "] Filosofo " + id + " finalizando");
            desconectar();
        }
    }
    
    /**
     * Punto de entrada de cada proceso filósofo.
     * 
     * Uso:
     *   java procesosreales.FilosofoClient <id> <host> <puerto>
     * 
     * Ejemplo:
     *   java procesosreales.FilosofoClient 0 localhost 9999
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Uso: java procesosreales.FilosofoClient <id> <host> <puerto>");
            System.exit(1);
        }
        
        int id = Integer.parseInt(args[0]);
        String host = args[1];
        int puerto = Integer.parseInt(args[2]);
        
        FilosofoClient filosofo = new FilosofoClient(id, host, puerto);
        
        // Manejar Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nFilosofo " + id + " recibio senal de terminacion");
            filosofo.desconectar();
        }));
        
        try {
            filosofo.conectar();
            filosofo.ejecutar();
        } catch (IOException e) {
            System.err.println("Error en Filosofo " + id + ": " + e.getMessage());
            System.exit(1);
        }
    }
}
