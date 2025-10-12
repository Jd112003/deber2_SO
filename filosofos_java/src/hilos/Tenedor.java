package hilos;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Clase que representa un tenedor en el problema de los filósofos comensales.
 * Utiliza un ReentrantLock para controlar el acceso exclusivo al tenedor.
 */
public class Tenedor {
    private final int id;
    private final ReentrantLock mutex;
    
    /**
     * Inicializa un tenedor.
     * 
     * @param id Identificador único del tenedor
     */
    public Tenedor(int id) {
        this.id = id;
        this.mutex = new ReentrantLock();
    }
    
    /**
     * Bloquea el mutex si el tenedor está libre.
     * Si el tenedor está siendo usado, el hilo se bloquea hasta que esté disponible.
     */
    public void tomar() {
        mutex.lock();
        System.out.println("  -> Tenedor " + id + " tomado");
    }
    
    /**
     * Desbloquea el mutex, liberando el tenedor para que otros lo puedan usar.
     */
    public void soltar() {
        mutex.unlock();
        System.out.println("  -> Tenedor " + id + " liberado");
    }
    
    /**
     * Obtiene el identificador del tenedor.
     * 
     * @return ID del tenedor
     */
    public int getId() {
        return id;
    }
}
