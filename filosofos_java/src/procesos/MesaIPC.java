package procesos;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase que representa la mesa usando mecanismos IPC (Inter-Process Communication).
 * En Java, simulamos IPC usando semáforos y estructuras atómicas compartidas.
 * 
 * Nota: Java no tiene IPC verdadero como fork() de Unix. Esta implementación
 * simula procesos usando threads con comunicación mediante estructuras compartidas.
 */
public class MesaIPC {
    private final int numFilosofos;
    private final Semaphore[] tenedoresSem;
    private final Semaphore mutexGlobal;
    private final AtomicIntegerArray tablaEstados;
    private final Semaphore[] semEspera;
    private final AtomicInteger solicitudesAtendidas;
    private final AtomicIntegerArray vecesComido;
    
    /**
     * Inicializa la mesa IPC con mecanismos de sincronización entre procesos.
     * 
     * @param numFilosofos Número de filósofos (y tenedores) en la mesa
     */
    public MesaIPC(int numFilosofos) {
        this.numFilosofos = numFilosofos;
        
        // Array de semáforos - uno por cada tenedor (inicializados en 1)
        this.tenedoresSem = new Semaphore[numFilosofos];
        for (int i = 0; i < numFilosofos; i++) {
            tenedoresSem[i] = new Semaphore(1);
        }
        
        // Lock global para proteger operaciones críticas
        this.mutexGlobal = new Semaphore(1);
        
        // Memoria compartida - tabla de estados de los filósofos
        this.tablaEstados = new AtomicIntegerArray(numFilosofos);
        for (int i = 0; i < numFilosofos; i++) {
            tablaEstados.set(i, EstadoFilosofo.PENSANDO.getValor());
        }
        
        // Semáforos de espera - uno por filósofo para bloquear hasta que pueda comer
        this.semEspera = new Semaphore[numFilosofos];
        for (int i = 0; i < numFilosofos; i++) {
            semEspera[i] = new Semaphore(0);
        }
        
        // Contador de solicitudes atendidas (para estadísticas)
        this.solicitudesAtendidas = new AtomicInteger(0);
        
        // Array de contadores de veces que comió cada filósofo
        this.vecesComido = new AtomicIntegerArray(numFilosofos);
        for (int i = 0; i < numFilosofos; i++) {
            vecesComido.set(i, 0);
        }
    }
    
    /**
     * Calcula el índice del tenedor izquierdo del filósofo i.
     */
    private int izq(int i) {
        return i;
    }
    
    /**
     * Calcula el índice del tenedor derecho del filósofo i.
     */
    private int der(int i) {
        return (i + 1) % numFilosofos;
    }
    
    /**
     * Calcula el índice del vecino izquierdo del filósofo i.
     */
    private int vecinoIzq(int i) {
        return (i - 1 + numFilosofos) % numFilosofos;
    }
    
    /**
     * Calcula el índice del vecino derecho del filósofo i.
     */
    private int vecinoDer(int i) {
        return (i + 1) % numFilosofos;
    }
    
    /**
     * Comprueba si los tenedores están disponibles para el filósofo.
     */
    private boolean autorizar(int id) {
        // Verificar que el filósofo esté hambriento
        if (tablaEstados.get(id) != EstadoFilosofo.HAMBRIENTO.getValor()) {
            return false;
        }
        
        // Verificar que los vecinos no estén comiendo
        int vecIzq = vecinoIzq(id);
        int vecDer = vecinoDer(id);
        
        boolean puedeComer = (
            tablaEstados.get(vecIzq) != EstadoFilosofo.COMIENDO.getValor() &&
            tablaEstados.get(vecDer) != EstadoFilosofo.COMIENDO.getValor()
        );
        
        if (puedeComer) {
            // Cambiar estado a COMIENDO
            tablaEstados.set(id, EstadoFilosofo.COMIENDO.getValor());
            System.out.println("  [MESA] Filósofo " + id + " autorizado para COMER");
            return true;
        } else {
            System.out.println("  [MESA] Filósofo " + id + " debe esperar (vecinos comiendo)");
            return false;
        }
    }
    
    /**
     * Procesa la solicitud de un filósofo para tomar los tenedores.
     */
    public void tomarTenedores(int id) {
        try {
            mutexGlobal.acquire();
            
            // Cambiar estado a HAMBRIENTO
            tablaEstados.set(id, EstadoFilosofo.HAMBRIENTO.getValor());
            System.out.printf("  [MESA] Filósofo %d solicita tenedores %d y %d%n", 
                            id, izq(id), der(id));
            
            // Intentar autorizar inmediatamente
            if (autorizar(id)) {
                // Notificar inmediatamente si fue autorizado
                semEspera[id].release();
            } else {
                // No puede comer ahora, se bloqueará
                System.out.println("  [MESA] Filósofo " + id + " bloqueado esperando recursos");
            }
            
            mutexGlobal.release();
            
            // Esperar hasta que sea autorizado (fuera del lock)
            semEspera[id].acquire();
            
            // Tomar los semáforos de los tenedores
            tenedoresSem[izq(id)].acquire();
            tenedoresSem[der(id)].acquire();
            
            System.out.printf("  [MESA] Filósofo %d tomó tenedores %d y %d%n", 
                            id, izq(id), der(id));
            
            solicitudesAtendidas.incrementAndGet();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Libera los tenedores del filósofo y actualiza el estado.
     */
    public void soltarTenedores(int id) {
        // Liberar los semáforos de los tenedores
        tenedoresSem[der(id)].release();
        tenedoresSem[izq(id)].release();
        
        System.out.printf("  [MESA] Filósofo %d liberó tenedores %d y %d%n", 
                        id, izq(id), der(id));
        
        liberar(id);
    }
    
    /**
     * Actualiza el estado del filósofo y libera tenedores.
     * Notifica a los vecinos que pueden intentar comer.
     */
    private void liberar(int id) {
        try {
            mutexGlobal.acquire();
            
            // Cambiar estado a PENSANDO
            tablaEstados.set(id, EstadoFilosofo.PENSANDO.getValor());
            
            // Intentar despertar a los vecinos si pueden comer
            int vecIzq = vecinoIzq(id);
            int vecDer = vecinoDer(id);
            
            // Verificar vecino izquierdo
            if (autorizar(vecIzq)) {
                notificar(vecIzq);
            }
            
            // Verificar vecino derecho
            if (autorizar(vecDer)) {
                notificar(vecDer);
            }
            
            mutexGlobal.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Despierta un proceso en espera señalando que puede continuar.
     */
    private void notificar(int id) {
        System.out.println("  [MESA] Notificando a Filósofo " + id);
        semEspera[id].release();
    }
    
    /**
     * Obtiene estadísticas de uso de la mesa.
     */
    public String obtenerEstadisticas() {
        StringBuilder sb = new StringBuilder();
        sb.append("Solicitudes atendidas: ").append(solicitudesAtendidas.get()).append("\n");
        for (int i = 0; i < numFilosofos; i++) {
            EstadoFilosofo estado = EstadoFilosofo.fromValor(tablaEstados.get(i));
            sb.append("Filósofo ").append(i).append(": ").append(estado).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Muestra el estado actual de todos los filósofos.
     */
    public void mostrarEstado() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ESTADO ACTUAL DE LA MESA");
        System.out.println("=".repeat(60));
        for (int i = 0; i < numFilosofos; i++) {
            EstadoFilosofo estado = EstadoFilosofo.fromValor(tablaEstados.get(i));
            System.out.println("Filósofo " + i + ": " + estado);
        }
        System.out.println("=".repeat(60) + "\n");
    }
    
    /**
     * Incrementa el contador de veces que comió un filósofo.
     */
    public void incrementarVecesComido(int id) {
        vecesComido.incrementAndGet(id);
    }
    
    /**
     * Imprime las estadísticas finales de la simulación.
     */
    public void imprimirEstadisticas() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ESTADÍSTICAS FINALES");
        System.out.println("=".repeat(70));
        
        int total = 0;
        for (int i = 0; i < numFilosofos; i++) {
            System.out.printf("Filósofo %d comió %d veces%n", i, vecesComido.get(i));
            total += vecesComido.get(i);
        }
        
        System.out.printf("%nTotal de veces que se comió: %d%n", total);
        System.out.printf("Promedio por filósofo: %.2f%n", (double)total / numFilosofos);
        System.out.println("=".repeat(70));
    }
}
