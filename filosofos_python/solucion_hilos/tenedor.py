import threading


class Tenedor:
    """
    Clase que representa un tenedor en el problema de los filósofos comensales.
    Utiliza un mutex para controlar el acceso exclusivo al tenedor.
    """
    
    def __init__(self, id: int):
        """
        Inicializa un tenedor.
        
        Args:
            id: Identificador único del tenedor
        """
        self.id = id
        self.mutex = threading.Lock()
    
    def tomar(self):
        """
        Bloquea el mutex si el tenedor está libre.
        Si el tenedor está siendo usado, el hilo se bloquea hasta que esté disponible.
        """
        self.mutex.acquire()
        print(f"  -> Tenedor {self.id} tomado")
    
    def soltar(self):
        """
        Desbloquea el mutex, liberando el tenedor para que otros lo puedan usar.
        """
        self.mutex.release()
        print(f"  -> Tenedor {self.id} liberado")
