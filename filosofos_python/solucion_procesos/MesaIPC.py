import multiprocessing
from multiprocessing import Semaphore, Queue, Array, Lock
from enum import IntEnum
import time


class EstadoFilosofo(IntEnum):
    """Enum para los estados del filósofo en memoria compartida"""
    PENSANDO = 0
    HAMBRIENTO = 1
    COMIENDO = 2


class MesaIPC:
    """
    Clase que representa la mesa usando mecanismos IPC (Inter-Process Communication).
    Coordina el acceso a los tenedores compartidos entre múltiples procesos.
    """
    
    def __init__(self, num_filosofos: int):
        """
        Inicializa la mesa IPC con mecanismos de sincronización entre procesos.
        
        Args:
            num_filosofos: Número de filósofos (y tenedores) en la mesa
        """
        self.num_filosofos = num_filosofos
        
        # Array de semáforos - uno por cada tenedor (inicializados en 1)
        self.tenedores_sem = [Semaphore(1) for _ in range(num_filosofos)]
        
        # Canal de comunicación - cola de mensajes para recibir solicitudes
        self.cola_mensajes = Queue()
        
        # Memoria compartida - tabla de estados de los filósofos
        # Usamos Array para compartir entre procesos
        self.tabla_estados = Array('i', [EstadoFilosofo.PENSANDO] * num_filosofos)
        
        # Lock global para proteger operaciones críticas
        self.mutex_global = Lock()
        
        # Semáforos de espera - uno por filósofo para bloquear hasta que pueda comer
        self.sem_espera = [Semaphore(0) for _ in range(num_filosofos)]
        
        # Contador de solicitudes atendidas (para estadísticas)
        self.solicitudes_atendidas = multiprocessing.Value('i', 0)
        
        # Array de contadores de veces que comió cada filósofo
        self.veces_comido = Array('i', [0] * num_filosofos)
    
    def izq(self, i: int) -> int:
        """
        Calcula el índice del tenedor izquierdo del filósofo i.
        
        Args:
            i: Índice del filósofo
            
        Returns:
            Índice del tenedor izquierdo
        """
        return i
    
    def der(self, i: int) -> int:
        """
        Calcula el índice del tenedor derecho del filósofo i.
        
        Args:
            i: Índice del filósofo
            
        Returns:
            Índice del tenedor derecho
        """
        return (i + 1) % self.num_filosofos
    
    def vecino_izq(self, i: int) -> int:
        """
        Calcula el índice del vecino izquierdo del filósofo i.
        
        Args:
            i: Índice del filósofo
            
        Returns:
            Índice del vecino izquierdo
        """
        return (i - 1 + self.num_filosofos) % self.num_filosofos
    
    def vecino_der(self, i: int) -> int:
        """
        Calcula el índice del vecino derecho del filósofo i.
        
        Args:
            i: Índice del filósofo
            
        Returns:
            Índice del vecino derecho
        """
        return (i + 1) % self.num_filosofos
    
    def autorizar(self, id: int) -> bool:
        """
        Comprueba si los tenedores están disponibles para el filósofo.
        Solo puede comer si está HAMBRIENTO y sus vecinos NO están COMIENDO.
        
        Args:
            id: Identificador del filósofo
            
        Returns:
            True si puede obtener los tenedores, False en caso contrario
        """
        # Verificar que el filósofo esté hambriento
        if self.tabla_estados[id] != EstadoFilosofo.HAMBRIENTO:
            return False
        
        # Verificar que los vecinos no estén comiendo
        vec_izq = self.vecino_izq(id)
        vec_der = self.vecino_der(id)
        
        puede_comer = (
            self.tabla_estados[vec_izq] != EstadoFilosofo.COMIENDO and
            self.tabla_estados[vec_der] != EstadoFilosofo.COMIENDO
        )
        
        if puede_comer:
            # Cambiar estado a COMIENDO
            self.tabla_estados[id] = EstadoFilosofo.COMIENDO
            print(f"  [MESA] Filósofo {id} autorizado para COMER")
            return True
        else:
            print(f"  [MESA] Filósofo {id} debe esperar (vecinos comiendo)")
            return False
    
    def tomar_tenedores(self, id: int):
        """
        Procesa la solicitud de un filósofo para tomar los tenedores.
        Bloquea al proceso hasta que ambos tenedores estén disponibles.
        
        Args:
            id: Identificador del filósofo
        """
        with self.mutex_global:
            # Cambiar estado a HAMBRIENTO
            self.tabla_estados[id] = EstadoFilosofo.HAMBRIENTO
            print(f"  [MESA] Filósofo {id} solicita tenedores {self.izq(id)} y {self.der(id)}")
            
            # Intentar autorizar inmediatamente
            if self.autorizar(id):
                # ✅ FIX: Notificar inmediatamente si fue autorizado
                self.sem_espera[id].release()
            else:
                # No puede comer ahora, se bloqueará
                print(f"  [MESA] Filósofo {id} bloqueado esperando recursos")
        
        # Esperar hasta que sea autorizado (fuera del lock)
        self.sem_espera[id].acquire()
        
        # Tomar los semáforos de los tenedores
        self.tenedores_sem[self.izq(id)].acquire()
        self.tenedores_sem[self.der(id)].acquire()
        
        print(f"  [MESA] Filósofo {id} tomó tenedores {self.izq(id)} y {self.der(id)}")
        
        with self.solicitudes_atendidas.get_lock():
            self.solicitudes_atendidas.value += 1
    
    def soltar_tenedores(self, id: int):
        """
        Libera los tenedores del filósofo y actualiza el estado.
        
        Args:
            id: Identificador del filósofo
        """
        # Liberar los semáforos de los tenedores
        self.tenedores_sem[self.der(id)].release()
        self.tenedores_sem[self.izq(id)].release()
        
        print(f"  [MESA] Filósofo {id} liberó tenedores {self.izq(id)} y {self.der(id)}")
        
        self.liberar(id)
    
    def liberar(self, id: int):
        """
        Actualiza el estado del filósofo y libera tenedores.
        Notifica a los vecinos que pueden intentar comer.
        
        Args:
            id: Identificador del filósofo
        """
        with self.mutex_global:
            # Cambiar estado a PENSANDO
            self.tabla_estados[id] = EstadoFilosofo.PENSANDO
            
            # Intentar despertar a los vecinos si pueden comer
            vec_izq = self.vecino_izq(id)
            vec_der = self.vecino_der(id)
            
            # Verificar vecino izquierdo
            if self.autorizar(vec_izq):
                self.notificar(vec_izq)
            
            # Verificar vecino derecho
            if self.autorizar(vec_der):
                self.notificar(vec_der)
    
    def notificar(self, id: int):
        """
        Despierta un proceso en espera señalando que puede continuar.
        
        Args:
            id: Identificador del filósofo a despertar
        """
        print(f"  [MESA] Notificando a Filósofo {id}")
        self.sem_espera[id].release()
    
    def atender_solicitud(self):
        """
        Recibe y atiende peticiones de filósofos desde la cola de mensajes.
        Este método puede ejecutarse en un proceso servidor dedicado.
        
        Returns:
            El mensaje recibido o None si la cola está vacía
        """
        try:
            if not self.cola_mensajes.empty():
                mensaje = self.cola_mensajes.get(timeout=1)
                print(f"  [MESA] Atendiendo solicitud: {mensaje}")
                
                # Procesar según el tipo de mensaje
                tipo = mensaje.get('tipo')
                id_filosofo = mensaje.get('id')
                
                if tipo == 'TOMAR':
                    self.tomar_tenedores(id_filosofo)
                elif tipo == 'SOLTAR':
                    self.soltar_tenedores(id_filosofo)
                
                return mensaje
            return None
        except Exception as e:
            print(f"  [MESA] Error atendiendo solicitud: {e}")
            return None
    
    def enviar_solicitud(self, tipo: str, id_filosofo: int):
        """
        Permite a los filósofos enviar solicitudes a la mesa.
        
        Args:
            tipo: Tipo de solicitud ('TOMAR' o 'SOLTAR')
            id_filosofo: Identificador del filósofo que hace la solicitud
        """
        mensaje = {'tipo': tipo, 'id': id_filosofo}
        self.cola_mensajes.put(mensaje)
    
    def obtener_estadisticas(self):
        """
        Obtiene estadísticas de uso de la mesa.
        
        Returns:
            Diccionario con estadísticas
        """
        estados_actuales = [EstadoFilosofo(self.tabla_estados[i]).name 
                           for i in range(self.num_filosofos)]
        
        return {
            'solicitudes_atendidas': self.solicitudes_atendidas.value,
            'estados': estados_actuales,
            'mensajes_pendientes': self.cola_mensajes.qsize()
        }
    
    def mostrar_estado(self):
        """
        Muestra el estado actual de todos los filósofos.
        """
        print("\n" + "="*60)
        print("ESTADO ACTUAL DE LA MESA")
        print("="*60)
        for i in range(self.num_filosofos):
            estado = EstadoFilosofo(self.tabla_estados[i]).name
            print(f"Filósofo {i}: {estado}")
        print("="*60 + "\n")
    
    def limpiar(self):
        """
        Limpia los recursos IPC utilizados.
        """
        # Cerrar la cola de mensajes
        self.cola_mensajes.close()
        self.cola_mensajes.join_thread()
        
        print("Recursos IPC liberados")
    
    def imprimir_estadisticas(self):
        """
        Imprime las estadísticas finales de la simulación.
        """
        print(f"\n{'='*70}")
        print("ESTADÍSTICAS FINALES")
        print(f"{'='*70}")
        
        total = 0
        for i in range(self.num_filosofos):
            print(f"Filósofo {i} comió {self.veces_comido[i]} veces")
            total += self.veces_comido[i]
        
        print(f"\nTotal de veces que se comió: {total}")
        print(f"Promedio por filósofo: {total / self.num_filosofos:.2f}")
        print(f"{'='*70}")
