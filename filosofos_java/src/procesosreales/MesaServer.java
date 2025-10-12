package procesosreales;

import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Servidor central que coordina el acceso a los tenedores.
 * Usa sockets para comunicarse con procesos Java independientes (JVMs separadas).
 * 
 * Este servidor simula la memoria compartida y semáforos IPC mediante:
 * - Un servidor TCP que escucha solicitudes
 * - Estado centralizado de los filósofos
 * - Sincronización mediante semáforos
 */
public class MesaServer {
    private final int numFilosofos;
    private final int puerto;
    private final Semaphore[] tenedoresSem;
    private final Semaphore mutexGlobal;
    private final AtomicIntegerArray tablaEstados;
    private final Semaphore[] semEspera;
    private final AtomicInteger solicitudesAtendidas;
    private ServerSocket serverSocket;
    private volatile boolean activo = true;
    private final List<ClientHandler> clientes;
    
    public MesaServer(int numFilosofos, int puerto) {
        this.numFilosofos = numFilosofos;
        this.puerto = puerto;
        this.clientes = new ArrayList<>();
        
        // Inicializar semáforos de tenedores
        this.tenedoresSem = new Semaphore[numFilosofos];
        for (int i = 0; i < numFilosofos; i++) {
            tenedoresSem[i] = new Semaphore(1);
        }
        
        // Lock global
        this.mutexGlobal = new Semaphore(1);
        
        // Tabla de estados
        this.tablaEstados = new AtomicIntegerArray(numFilosofos);
        for (int i = 0; i < numFilosofos; i++) {
            tablaEstados.set(i, 0); // PENSANDO
        }
        
        // Semáforos de espera
        this.semEspera = new Semaphore[numFilosofos];
        for (int i = 0; i < numFilosofos; i++) {
            semEspera[i] = new Semaphore(0);
        }
        
        this.solicitudesAtendidas = new AtomicInteger(0);
    }
    
    /**
     * Inicia el servidor TCP.
     */
    public void iniciar() throws IOException {
        serverSocket = new ServerSocket(puerto);
        System.out.println("\n" + "=".repeat(70));
        System.out.println("SERVIDOR DE MESA INICIADO - PROCESOS REALES CON JVMs SEPARADAS");
        System.out.println("=".repeat(70));
        System.out.println("Escuchando en puerto: " + puerto);
        System.out.println("Numero de filosofos: " + numFilosofos);
        System.out.println("=".repeat(70) + "\n");
        
        // Hilo para aceptar conexiones
        Thread acceptThread = new Thread(() -> {
            while (activo) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket);
                    clientes.add(handler);
                    new Thread(handler).start();
                } catch (IOException e) {
                    if (activo) {
                        System.err.println("Error aceptando cliente: " + e.getMessage());
                    }
                }
            }
        });
        acceptThread.setDaemon(true);
        acceptThread.start();
    }
    
    /**
     * Detiene el servidor.
     */
    public void detener() {
        activo = false;
        try {
            // Cerrar todas las conexiones de clientes
            for (ClientHandler cliente : clientes) {
                cliente.cerrar();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error al detener servidor: " + e.getMessage());
        }
        
        mostrarEstadisticas();
    }
    
    /**
     * Calcula índices de tenedores y vecinos.
     */
    private int izq(int i) { return i; }
    private int der(int i) { return (i + 1) % numFilosofos; }
    private int vecinoIzq(int i) { return (i - 1 + numFilosofos) % numFilosofos; }
    private int vecinoDer(int i) { return (i + 1) % numFilosofos; }
    
    /**
     * Verifica si el filósofo puede comer.
     */
    private boolean autorizar(int id) {
        if (tablaEstados.get(id) != 1) { // No está HAMBRIENTO
            return false;
        }
        
        int vecIzq = vecinoIzq(id);
        int vecDer = vecinoDer(id);
        
        boolean puedeComer = (
            tablaEstados.get(vecIzq) != 2 && // Vecino izq no esta COMIENDO
            tablaEstados.get(vecDer) != 2     // Vecino der no esta COMIENDO
        );
        
        if (puedeComer) {
            tablaEstados.set(id, 2); // COMIENDO
            System.out.println("  [SERVIDOR] Filosofo " + id + " autorizado para COMER");
            return true;
        } else {
            System.out.println("  [SERVIDOR] Filosofo " + id + " debe esperar (vecinos comiendo)");
            return false;
        }
    }
    
    /**
     * Procesa solicitud para tomar tenedores.
     */
    private void tomarTenedores(int id) throws InterruptedException {
        mutexGlobal.acquire();
        
        tablaEstados.set(id, 1); // HAMBRIENTO
        System.out.printf("  [SERVIDOR] Filosofo %d solicita tenedores %d y %d%n", 
                         id, izq(id), der(id));
        
        if (autorizar(id)) {
            semEspera[id].release();
        } else {
            System.out.println("  [SERVIDOR] Filosofo " + id + " bloqueado esperando recursos");
        }
        
        mutexGlobal.release();
        
        // Esperar autorizacion
        semEspera[id].acquire();
        
        // Tomar semáforos de tenedores
        tenedoresSem[izq(id)].acquire();
        tenedoresSem[der(id)].acquire();
        
        System.out.printf("  [SERVIDOR] Filosofo %d tomo tenedores %d y %d%n", 
                         id, izq(id), der(id));
        
        solicitudesAtendidas.incrementAndGet();
    }
    
    /**
     * Procesa solicitud para soltar tenedores.
     */
    private void soltarTenedores(int id) throws InterruptedException {
        // Liberar semáforos
        tenedoresSem[der(id)].release();
        tenedoresSem[izq(id)].release();
        
        System.out.printf("  [SERVIDOR] Filosofo %d libero tenedores %d y %d%n", 
                         id, izq(id), der(id));
        
        mutexGlobal.acquire();
        
        tablaEstados.set(id, 0); // PENSANDO
        
        // Intentar despertar vecinos
        int vecIzq = vecinoIzq(id);
        int vecDer = vecinoDer(id);
        
        if (autorizar(vecIzq)) {
            System.out.println("  [SERVIDOR] Notificando a Filosofo " + vecIzq);
            semEspera[vecIzq].release();
        }
        
        if (autorizar(vecDer)) {
            System.out.println("  [SERVIDOR] Notificando a Filosofo " + vecDer);
            semEspera[vecDer].release();
        }
        
        mutexGlobal.release();
    }
    
    /**
     * Muestra estadisticas finales.
     */
    private void mostrarEstadisticas() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ESTADISTICAS FINALES DEL SERVIDOR");
        System.out.println("=".repeat(70));
        System.out.println("Solicitudes atendidas: " + solicitudesAtendidas.get());
        
        String[] estados = {"PENSANDO", "HAMBRIENTO", "COMIENDO"};
        for (int i = 0; i < numFilosofos; i++) {
            int estadoVal = tablaEstados.get(i);
            String estadoNombre = (estadoVal >= 0 && estadoVal < 3) ? estados[estadoVal] : "DESCONOCIDO";
            System.out.println("Filosofo " + i + ": " + estadoNombre);
        }
        System.out.println("=".repeat(70) + "\n");
    }
    
    /**
     * Handler para cada cliente (filósofo) conectado.
     */
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private int filosofoId = -1;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    procesarMensaje(mensaje);
                }
            } catch (IOException e) {
                if (activo) {
                    System.err.println("Error en cliente: " + e.getMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                cerrar();
            }
        }
        
        private void procesarMensaje(String mensaje) throws InterruptedException {
            String[] partes = mensaje.split(":");
            String comando = partes[0];
            
            switch (comando) {
                case "REGISTER":
                    filosofoId = Integer.parseInt(partes[1]);
                    System.out.println("  [SERVIDOR] Filosofo " + filosofoId + " conectado desde proceso separado");
                    out.println("OK");
                    break;
                    
                case "TOMAR":
                    int idTomar = Integer.parseInt(partes[1]);
                    tomarTenedores(idTomar);
                    out.println("OK");
                    break;
                    
                case "SOLTAR":
                    int idSoltar = Integer.parseInt(partes[1]);
                    soltarTenedores(idSoltar);
                    out.println("OK");
                    break;
                    
                case "PING":
                    out.println("PONG");
                    break;
                    
                default:
                    out.println("ERROR:Comando desconocido");
            }
        }
        
        public void cerrar() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                // Ignorar
            }
        }
    }
    
    /**
     * Punto de entrada del servidor.
     */
    public static void main(String[] args) {
        int numFilosofos = 5;
        int puerto = 9999;
        
        if (args.length > 0) {
            numFilosofos = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
            puerto = Integer.parseInt(args[1]);
        }
        
        MesaServer servidor = new MesaServer(numFilosofos, puerto);
        
        try {
            servidor.iniciar();
            
            // Mantener el servidor activo
            System.out.println("Servidor activo. Presiona Enter para detener...");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
            
            servidor.detener();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
