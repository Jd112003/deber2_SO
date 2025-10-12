"""
Solución del Problema de los Filósofos Comensales usando PROCESOS.

Este paquete contiene la implementación basada en multiprocessing con:
- Clase ProcesoFilosofo: Representa los actores concurrentes (procesos)
- Clase MesaIPC: Coordina el acceso usando mecanismos IPC
"""

from .ProcesoFilosofo import ProcesoFilosofo, Estado
from .MesaIPC import MesaIPC, EstadoFilosofo

__all__ = ['ProcesoFilosofo', 'Estado', 'MesaIPC', 'EstadoFilosofo']
