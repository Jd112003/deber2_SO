package hilos;

import java.util.Random;

/**
 * Clase que representa un filósofo en el problema de los filósofos comensales.
 */
public class Filosofo implements Runnable {
    private final int id;
    private final Mesa mesa;
    private Estado estado;
    private final Random random;
    private final Thread hilo;
    
    /**
     * Inicializa un filósofo.
     * 
     * @param id Identificador único del filósofo
     * @param mesa Referencia a la Mesa para acceder a los tenedores compartidos
     */
    public Filosofo(int id, Mesa mesa) {
        this.id = id;
        this.mesa = mesa;
        this.estado = Estado.PENSANDO;
        this.random = new Random();
        this.hilo = new Thread(this, "Filosofo-" + id);
        this.hilo.setDaemon(true);
    }
    
    /**
     * Simula el tiempo de pensamiento del filósofo.
     * El tiempo es aleatorio entre 1 y 3 segundos.
     */
    private void pensar() {
        estado = Estado.PENSANDO;
        double tiempo = 1.0 + random.nextDouble() * 2.0;
        System.out.printf("Filósofo %d está PENSANDO por %.2f segundos%n", id, tiempo);
        try {
            Thread.sleep((long)(tiempo * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Simula el tiempo de comida del filósofo.
     * El tiempo es aleatorio entre 1 y 3 segundos.
     */
    private void comer() {
        estado = Estado.COMIENDO;
        double tiempo = 1.0 + random.nextDouble() * 2.0;
        System.out.printf("Filósofo %d está COMIENDO por %.2f segundos%n", id, tiempo);
        try {
            Thread.sleep((long)(tiempo * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Solicita a la mesa que le asigne los dos tenedores.
     * Cambia el estado a HAMBRIENTO y espera hasta obtener ambos tenedores.
     */
    private void tomarTenedores() {
        estado = Estado.HAMBRIENTO;
        System.out.println("Filósofo " + id + " está HAMBRIENTO y quiere comer");
        mesa.tomarTenedores(id);
    }
    
    /**
     * Notifica a la mesa que terminó de comer y libera los tenedores.
     */
    private void soltarTenedores() {
        System.out.println("Filósofo " + id + " soltó los tenedores");
        mesa.soltarTenedores(id);
    }
    
    /**
     * Bucle infinito que representa el comportamiento del filósofo.
     * Ciclo: pensar → pedir tenedores → comer → liberar tenedores
     */
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                pensar();
                tomarTenedores();
                comer();
                soltarTenedores();
            }
        } catch (Exception e) {
            System.out.println("Filósofo " + id + " interrumpido");
        }
    }
    
    /**
     * Inicia el hilo de ejecución del filósofo.
     */
    public void iniciar() {
        hilo.start();
    }
    
    /**
     * Obtiene el estado actual del filósofo.
     * 
     * @return Estado actual
     */
    public Estado getEstado() {
        return estado;
    }
    
    /**
     * Obtiene el identificador del filósofo.
     * 
     * @return ID del filósofo
     */
    public int getId() {
        return id;
    }
}
