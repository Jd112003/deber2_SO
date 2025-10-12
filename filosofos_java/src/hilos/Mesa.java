package hilos;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Clase que representa la mesa donde los filósofos comen.
 * Coordina el acceso a los tenedores compartidos y evita deadlocks.
 */
public class Mesa {
    private final int numFilosofos;
    private final Tenedor[] tenedores;
    private final ReentrantLock mutexMesa;
    private final Condition monitor;
    private final Estado[] estados;
    private final Filosofo[] filosofos;
    
    /**
     * Inicializa la mesa con el número especificado de filósofos.
     * 
     * @param numFilosofos Número de filósofos (y tenedores) en la mesa
     */
    public Mesa(int numFilosofos) {
        this.numFilosofos = numFilosofos;
        
        // Crear los tenedores (recursos compartidos)
        this.tenedores = new Tenedor[numFilosofos];
        for (int i = 0; i < numFilosofos; i++) {
            tenedores[i] = new Tenedor(i);
        }
        
        // Mutex para proteger la modificación del estado global
        this.mutexMesa = new ReentrantLock();
        
        // Monitor: una condición compartida que usa el mutex de la mesa
        this.monitor = mutexMesa.newCondition();
        
        // Estados de los filósofos (array auxiliar para sincronización)
        this.estados = new Estado[numFilosofos];
        for (int i = 0; i < numFilosofos; i++) {
            estados[i] = Estado.PENSANDO;
        }
        
        // Crear los filósofos (actores concurrentes)
        this.filosofos = new Filosofo[numFilosofos];
        for (int i = 0; i < numFilosofos; i++) {
            filosofos[i] = new Filosofo(i, this);
        }
    }
    
    /**
     * Calcula el índice del tenedor izquierdo del filósofo i.
     * 
     * @param i Índice del filósofo
     * @return Índice del tenedor izquierdo
     */
    private int izq(int i) {
        return i;
    }
    
    /**
     * Calcula el índice del tenedor derecho del filósofo i.
     * 
     * @param i Índice del filósofo
     * @return Índice del tenedor derecho
     */
    private int der(int i) {
        return (i + 1) % numFilosofos;
    }
    
    /**
     * Verifica si el filósofo i puede tomar ambos tenedores.
     * Solo puede comer si está HAMBRIENTO y sus vecinos NO están COMIENDO.
     * 
     * @param i Índice del filósofo
     * @return true si puede comer, false en caso contrario
     */
    private boolean permitirComer(int i) {
        int vecinoIzq = (i - 1 + numFilosofos) % numFilosofos;
        int vecinoDer = (i + 1) % numFilosofos;
        
        return (estados[i] == Estado.HAMBRIENTO &&
                estados[vecinoIzq] != Estado.COMIENDO &&
                estados[vecinoDer] != Estado.COMIENDO);
    }
    
    /**
     * Verifica y bloquea los tenedores para el filósofo i.
     * Si no puede obtener ambos tenedores, espera hasta que estén disponibles.
     * 
     * @param i Índice del filósofo
     */
    public void tomarTenedores(int i) {
        mutexMesa.lock();
        try {
            // Cambiar estado a HAMBRIENTO
            estados[i] = Estado.HAMBRIENTO;
            System.out.printf("Filósofo %d intenta tomar tenedores %d y %d%n", 
                            i, izq(i), der(i));
            
            // Intentar obtener permiso para comer
            while (!permitirComer(i)) {
                // Si no puede comer, espera en la condición
                monitor.await();
            }
            
            // Puede comer: cambiar estado y tomar tenedores
            estados[i] = Estado.COMIENDO;
            tenedores[izq(i)].tomar();
            tenedores[der(i)].tomar();
            System.out.println("Filósofo " + i + " tomó los tenedores y está COMIENDO");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutexMesa.unlock();
        }
    }
    
    /**
     * Libera los tenedores del filósofo i y notifica a sus vecinos
     * que pueden intentar comer.
     * 
     * @param i Índice del filósofo
     */
    public void soltarTenedores(int i) {
        mutexMesa.lock();
        try {
            // Cambiar estado a PENSANDO
            estados[i] = Estado.PENSANDO;
            
            // Soltar los tenedores
            tenedores[izq(i)].soltar();
            tenedores[der(i)].soltar();
            System.out.println("Filósofo " + i + " soltó los tenedores");
            
            // Notificar a TODOS los filósofos que pueden intentar comer
            // Usamos signalAll() para despertar a todos los que esperan
            monitor.signalAll();
            
        } finally {
            mutexMesa.unlock();
        }
    }
    
    /**
     * Inicia la ejecución de todos los filósofos.
     */
    public void iniciarCena() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Iniciando cena con " + numFilosofos + " filósofos");
        System.out.println("=".repeat(60) + "\n");
        
        for (Filosofo filosofo : filosofos) {
            filosofo.iniciar();
        }
    }
}
