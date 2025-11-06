import threading
from .tenedor import Tenedor
from .filosofo import Filosofo, Estado


class Mesa:
    """
    Clase que representa la mesa donde los filósofos comen.
    Coordina el acceso a los tenedores compartidos y evita deadlocks.
    """
    
    def __init__(self, num_filosofos: int):
        """
        Inicializa la mesa con el número especificado de filósofos.
        
        Args:
            num_filosofos: Número de filósofos (y tenedores) en la mesa
        """
        self.num_filosofos = num_filosofos
        
        # Crear los tenedores (recursos compartidos)
        self.tenedores = [Tenedor(i) for i in range(num_filosofos)]
        
        # Mutex para proteger la modificación del estado global
        self.mutex_mesa = threading.Lock()
        
        # Monitor: una condición compartida que usa el mutex de la mesa
        self.monitor = threading.Condition(self.mutex_mesa)
        
        # Estados de los filósofos (array auxiliar para sincronización)
        self.estados = [Estado.PENSANDO for _ in range(num_filosofos)]
        
        # Crear los filósofos (actores concurrentes) - debe ser después del mutex
        self.filosofos = [Filosofo(i, self) for i in range(num_filosofos)]
    
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
    
    def permitir_comer(self, i: int) -> bool:
        """
        Verifica si el filósofo i puede tomar ambos tenedores.
        Solo puede comer si está HAMBRIENTO y sus vecinos NO están COMIENDO.
        
        Args:
            i: Índice del filósofo
            
        Returns:
            True si puede comer, False en caso contrario
        """
        vecino_izq = (i - 1 + self.num_filosofos) % self.num_filosofos
        vecino_der = (i + 1) % self.num_filosofos
        
        puede_comer = (
            self.estados[i] == Estado.HAMBRIENTO and
            self.estados[vecino_izq] != Estado.COMIENDO and
            self.estados[vecino_der] != Estado.COMIENDO
        )
        
        return puede_comer
    
    def tomar_tenedores(self, i: int):
        """
        Verifica y bloquea los tenedores para el filósofo i.
        Si no puede obtener ambos tenedores, espera hasta que estén disponibles.
        
        Args:
            i: Índice del filósofo
        """
        with self.monitor:
            # Cambiar estado a HAMBRIENTO
            self.estados[i] = Estado.HAMBRIENTO
            print(f"Filósofo {i} intenta tomar tenedores {self.izq(i)} y {self.der(i)}")
            
            # Intentar obtener permiso para comer
            while not self.permitir_comer(i):
                # Si no puede comer, espera en la condición
                self.monitor.wait()
            
            # Puede comer: cambiar estado y tomar tenedores
            self.estados[i] = Estado.COMIENDO
            self.tenedores[self.izq(i)].tomar()
            self.tenedores[self.der(i)].tomar()
            print(f"Filósofo {i} tomó los tenedores y está COMIENDO")
    
    def soltar_tenedores(self, i: int):
        """
        Libera los tenedores del filósofo i y notifica a sus vecinos
        que pueden intentar comer.
        
        Args:
            i: Índice del filósofo
        """
        with self.monitor:
            # Cambiar estado a PENSANDO
            self.estados[i] = Estado.PENSANDO
            
            # Soltar los tenedores
            self.tenedores[self.izq(i)].soltar()
            self.tenedores[self.der(i)].soltar()
            print(f"Filósofo {i} soltó los tenedores")
            
            # Notificar a TODOS los filósofos que pueden intentar comer
            # Usamos notify_all() para despertar a todos los que esperan
            self.monitor.notify_all()
    
    def iniciar_cena(self):
        """
        Inicia la ejecución de todos los filósofos.
        """
        print(f"\n{'='*60}")
        print(f"Iniciando cena con {self.num_filosofos} filósofos")
        print(f"{'='*60}\n")
        
        for filosofo in self.filosofos:
            filosofo.iniciar()
    
    def esperar(self):
        """
        Espera a que todos los hilos de los filósofos terminen.
        """
        for filosofo in self.filosofos:
            filosofo.hilo.join()
    
    def detener(self):
        """
        Detiene todos los filósofos y espera a que terminen.
        """
        print("\nDeteniendo filósofos...")
        for filosofo in self.filosofos:
            filosofo.detener()
        
        # Despertar a todos los que puedan estar esperando
        with self.monitor:
            self.monitor.notify_all()
        
        # Esperar a que terminen (timeout por si quedan bloqueados)
        for filosofo in self.filosofos:
            filosofo.hilo.join(timeout=2.0)
    
    def imprimir_estadisticas(self):
        """
        Imprime las estadísticas finales de la simulación.
        """
        print(f"\n{'='*70}")
        print("ESTADÍSTICAS FINALES")
        print(f"{'='*70}")
        
        total = 0
        for filosofo in self.filosofos:
            print(f"Filósofo {filosofo.id} comió {filosofo.veces_comido} veces")
            total += filosofo.veces_comido
        
        print(f"\nTotal de veces que se comió: {total}")
        print(f"Promedio por filósofo: {total / self.num_filosofos:.2f}")
        print(f"{'='*70}")
