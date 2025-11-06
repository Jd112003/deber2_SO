import threading
import time
import random
from enum import Enum


class Estado(Enum):
    """Enum para los estados del filósofo"""
    PENSANDO = "PENSANDO"
    HAMBRIENTO = "HAMBRIENTO"
    COMIENDO = "COMIENDO"


class Filosofo:
    """
    Clase que representa un filósofo en el problema de los filósofos comensales.
    """
    
    def __init__(self, id: int, mesa):
        """
        Inicializa un filósofo.
        
        Args:
            id: Identificador único del filósofo
            mesa: Referencia a la Mesa para acceder a los tenedores compartidos
        """
        self.id = id
        self.mesa = mesa
        self.estado = Estado.PENSANDO
        self.hilo = threading.Thread(target=self.run, name=f"Filosofo-{id}")
        self.hilo.daemon = True
        self.veces_comido = 0
        self._activo = True
    
    def pensar(self):
        """
        Simula el tiempo de pensamiento del filósofo.
        El tiempo es aleatorio entre 1 y 3 segundos.
        """
        self.estado = Estado.PENSANDO
        tiempo = random.uniform(1, 3)
        print(f"Filósofo {self.id} está PENSANDO por {tiempo:.2f} segundos")
        time.sleep(tiempo)
    
    def comer(self):
        """
        Simula el tiempo de comida del filósofo.
        El tiempo es aleatorio entre 1 y 3 segundos.
        """
        self.estado = Estado.COMIENDO
        self.veces_comido += 1
        tiempo = random.uniform(1, 3)
        print(f"Filósofo {self.id} está COMIENDO por {tiempo:.2f} segundos")
        time.sleep(tiempo)
    
    def tomar_tenedores(self):
        """
        Solicita a la mesa que le asigne los dos tenedores.
        Cambia el estado a HAMBRIENTO y espera hasta obtener ambos tenedores.
        """
        self.estado = Estado.HAMBRIENTO
        print(f"Filósofo {self.id} está HAMBRIENTO y quiere comer")
        self.mesa.tomar_tenedores(self.id)
    
    def soltar_tenedores(self):
        """
        Notifica a la mesa que terminó de comer y libera los tenedores.
        """
        print(f"Filósofo {self.id} soltó los tenedores")
        self.mesa.soltar_tenedores(self.id)
    
    def run(self):
        """
        Bucle infinito que representa el comportamiento del filósofo.
        Ciclo: pensar → pedir tenedores → comer → liberar tenedores
        """
        while self._activo:
            self.pensar()
            if not self._activo:
                break
            self.tomar_tenedores()
            if not self._activo:
                break
            self.comer()
            if not self._activo:
                break
            self.soltar_tenedores()
    
    def detener(self):
        """
        Detiene el hilo del filósofo.
        """
        self._activo = False
    
    def iniciar(self):
        """
        Inicia el hilo de ejecución del filósofo.
        """
        self.hilo.start()
