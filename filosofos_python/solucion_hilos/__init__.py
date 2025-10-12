"""
Solución del Problema de los Filósofos Comensales usando HILOS.

Este paquete contiene la implementación basada en threading con:
- Clase Tenedor: Representa los recursos compartidos
- Clase Filosofo: Representa los actores concurrentes (hilos)
- Clase Mesa: Coordina el acceso a los tenedores usando monitores
"""

from .filosofo import Filosofo, Estado
from .mesa import Mesa
from .tenedor import Tenedor

__all__ = ['Filosofo', 'Estado', 'Mesa', 'Tenedor']
