import multiprocessing
import time
import random
import os
from enum import Enum


class Estado(Enum):
    """Enum para los estados del filósofo"""
    PENSANDO = "PENSANDO"
    HAMBRIENTO = "HAMBRIENTO"
    COMIENDO = "COMIENDO"


class ProcesoFilosofo:
    """
    Clase que representa un filósofo usando procesos independientes.
    Utiliza IPC (Inter-Process Communication) para coordinarse con la mesa.
    """
    
    def __init__(self, id: int, mesaIPC):
        """
        Inicializa un filósofo basado en procesos.
        
        Args:
            id: Identificador único del filósofo
            mesaIPC: Referencia al mecanismo de comunicación compartido (MesaIPC)
        """
        self.id = id
        self.mesaIPC = mesaIPC
        self.pid = None  # Se asignará cuando se cree el proceso
        self.estado = Estado.PENSANDO
        self.proceso = None  # Referencia al objeto Process
        self._activo = multiprocessing.Value('i', 1)  # Flag para controlar la ejecución
    
    def pensar(self):
        """
        Simula el tiempo de pensamiento del filósofo.
        El tiempo es aleatorio entre 1 y 3 segundos.
        """
        self.estado = Estado.PENSANDO
        tiempo = random.uniform(1, 3)
        print(f"[PID {os.getpid()}] Filósofo {self.id} está PENSANDO por {tiempo:.2f} segundos")
        time.sleep(tiempo)
    
    def comer(self):
        """
        Simula el tiempo de comida del filósofo.
        El tiempo es aleatorio entre 1 y 3 segundos.
        """
        self.estado = Estado.COMIENDO
        tiempo = random.uniform(1, 3)
        print(f"[PID {os.getpid()}] Filósofo {self.id} está COMIENDO por {tiempo:.2f} segundos")
        time.sleep(tiempo)
    
    def solicitar_recursos(self):
        """
        Envía solicitud a la mesa para obtener los tenedores.
        Utiliza el mecanismo IPC (pipes, semáforos, memoria compartida, etc.)
        para comunicarse con la mesa.
        """
        self.estado = Estado.HAMBRIENTO
        print(f"[PID {os.getpid()}] Filósofo {self.id} está HAMBRIENTO y solicita recursos")
        
        # Delegar a la mesa IPC para manejar la solicitud
        self.mesaIPC.tomar_tenedores(self.id)
        
        print(f"[PID {os.getpid()}] Filósofo {self.id} obtuvo los recursos")
    
    def liberar_recursos(self):
        """
        Libera los tenedores a través del mecanismo IPC.
        Notifica a la mesa que ha terminado de usar los recursos.
        """
        print(f"[PID {os.getpid()}] Filósofo {self.id} libera recursos")
        
        # Delegar a la mesa IPC para liberar los tenedores
        self.mesaIPC.soltar_tenedores(self.id)
    
    def _ciclo_principal(self):
        """
        Ciclo principal del proceso: pensar → solicitar → comer → liberar.
        Este método se ejecuta en el proceso hijo.
        """
        # Establecer el PID del proceso hijo
        pid_actual = os.getpid()
        print(f"[PID {pid_actual}] Filósofo {self.id} inició su proceso")
        
        try:
            while self._activo.value:
                self.pensar()
                self.solicitar_recursos()
                self.comer()
                self.liberar_recursos()
        except KeyboardInterrupt:
            print(f"[PID {pid_actual}] Filósofo {self.id} interrumpido")
        except Exception as e:
            print(f"[PID {pid_actual}] Filósofo {self.id} error: {e}")
        finally:
            print(f"[PID {pid_actual}] Filósofo {self.id} finalizó")
    
    def iniciar(self):
        """
        Ejecuta el ciclo principal pensar → solicitar → comer → liberar.
        Crea y arranca un proceso hijo que ejecutará el ciclo.
        """
        self.proceso = multiprocessing.Process(
            target=self._ciclo_principal,
            name=f"Filosofo-{self.id}"
        )
        self.proceso.daemon = False  # No daemon para poder controlar la terminación
        self.proceso.start()
        self.pid = self.proceso.pid
        print(f"Filósofo {self.id} iniciado con PID {self.pid}")
    
    def terminar(self):
        """
        Finaliza el proceso hijo de manera ordenada.
        Primero intenta una terminación limpia, luego fuerza si es necesario.
        """
        if self.proceso and self.proceso.is_alive():
            print(f"Terminando Filósofo {self.id} (PID {self.pid})...")
            
            # Señalar que debe terminar
            self._activo.value = 0
            
            # Esperar un tiempo razonable para terminación limpia
            self.proceso.join(timeout=5)
            
            # Si aún está vivo, forzar terminación
            if self.proceso.is_alive():
                print(f"Forzando terminación de Filósofo {self.id}...")
                self.proceso.terminate()
                self.proceso.join(timeout=2)
                
                # Último recurso: kill
                if self.proceso.is_alive():
                    self.proceso.kill()
                    self.proceso.join()
            
            print(f"Filósofo {self.id} terminado")
    
    def esta_vivo(self):
        """
        Verifica si el proceso está activo.
        
        Returns:
            True si el proceso está vivo, False en caso contrario
        """
        return self.proceso is not None and self.proceso.is_alive()
    
    def esperar(self):
        """
        Espera a que el proceso hijo termine.
        """
        if self.proceso:
            self.proceso.join()
