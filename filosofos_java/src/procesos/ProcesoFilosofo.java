package procesos;

import java.util.Random;

/**
 * Clase que representa un filósofo usando procesos independientes.
 * En Java, simulamos procesos usando threads independientes con comunicación IPC.
 * 
 * Nota: Java no soporta fork() verdadero como Unix/Linux. Esta implementación
 * simula el comportamiento de procesos usando threads con aislamiento de datos.
 */
public class ProcesoFilosofo implements Runnable {
    private final int id;
    private final MesaIPC mesaIPC;
    private final Random random;
    private volatile boolean activo;
    private Thread thread;
    
    /**
     * Inicializa un filósofo basado en procesos.
     */
    public ProcesoFilosofo(int id, MesaIPC mesaIPC) {
        this.id = id;
        this.mesaIPC = mesaIPC;
        this.random = new Random();
        this.activo = true;
    }
    
    /**
     * Simula el tiempo de pensamiento del filósofo.
     */
    private void pensar() {
        double tiempo = 1.0 + random.nextDouble() * 2.0;
        System.out.printf("[Thread-%d] Filósofo %d está PENSANDO por %.2f segundos%n", 
                         Thread.currentThread().getId(), id, tiempo);
        try {
            Thread.sleep((long)(tiempo * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Simula el tiempo de comida del filósofo.
     */
    private void comer() {
        mesaIPC.incrementarVecesComido(id);
        double tiempo = 1.0 + random.nextDouble() * 2.0;
        System.out.printf("[Thread-%d] Filósofo %d está COMIENDO por %.2f segundos%n", 
                         Thread.currentThread().getId(), id, tiempo);
        try {
            Thread.sleep((long)(tiempo * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Envía solicitud a la mesa para obtener los tenedores.
     */
    private void solicitarRecursos() {
        System.out.printf("[Thread-%d] Filósofo %d está HAMBRIENTO y solicita recursos%n", 
                         Thread.currentThread().getId(), id);
        mesaIPC.tomarTenedores(id);
        System.out.printf("[Thread-%d] Filósofo %d obtuvo los recursos%n", 
                         Thread.currentThread().getId(), id);
    }
    
    /**
     * Libera los tenedores a través del mecanismo IPC.
     */
    private void liberarRecursos() {
        System.out.printf("[Thread-%d] Filósofo %d libera recursos%n", 
                         Thread.currentThread().getId(), id);
        mesaIPC.soltarTenedores(id);
    }
    
    /**
     * Ciclo principal del proceso: pensar → solicitar → comer → liberar.
     */
    @Override
    public void run() {
        long threadId = Thread.currentThread().getId();
        System.out.printf("[Thread-%d] Filósofo %d inició su proceso%n", threadId, id);
        
        try {
            while (activo && !Thread.currentThread().isInterrupted()) {
                pensar();
                solicitarRecursos();
                comer();
                liberarRecursos();
            }
        } catch (Exception e) {
            System.out.printf("[Thread-%d] Filósofo %d error: %s%n", threadId, id, e.getMessage());
        } finally {
            System.out.printf("[Thread-%d] Filósofo %d finalizó%n", threadId, id);
        }
    }
    
    /**
     * Inicia el proceso del filósofo.
     */
    public void iniciar() {
        thread = new Thread(this, "Filosofo-" + id);
        thread.start();
        System.out.println("Filósofo " + id + " iniciado con Thread-" + thread.getId());
    }
    
    /**
     * Finaliza el proceso hijo de manera ordenada.
     */
    public void terminar() {
        if (thread != null && thread.isAlive()) {
            System.out.println("Terminando Filósofo " + id + "...");
            activo = false;
            thread.interrupt();
            
            try {
                thread.join(5000); // Esperar hasta 5 segundos
                if (thread.isAlive()) {
                    System.out.println("Forzando terminación de Filósofo " + id + "...");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("Filósofo " + id + " terminado");
        }
    }
    
    /**
     * Verifica si el proceso está activo.
     */
    public boolean estaVivo() {
        return thread != null && thread.isAlive();
    }
}
