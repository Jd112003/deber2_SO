"""
Script para ejecutar únicamente la solución con PROCESOS.

Uso:
    python ejecutar_procesos.py [num_filosofos] [duracion_segundos]

Ejemplo:
    python ejecutar_procesos.py 5 30
"""

import sys
import time
import multiprocessing
from solucion_procesos.MesaIPC import MesaIPC
from solucion_procesos.ProcesoFilosofo import ProcesoFilosofo


def main():
    # Parsear argumentos de línea de comandos
    num_filosofos = 5
    duracion = 30
    
    if len(sys.argv) > 1:
        try:
            num_filosofos = int(sys.argv[1])
        except ValueError:
            print("Error: El número de filósofos debe ser un entero.")
            sys.exit(1)
    
    if len(sys.argv) > 2:
        try:
            duracion = int(sys.argv[2])
        except ValueError:
            print("Error: La duración debe ser un entero.")
            sys.exit(1)
    
    print("\n" + "="*70)
    print("PROBLEMA DE LOS FILÓSOFOS COMENSALES - SOLUCIÓN CON PROCESOS")
    print("="*70)
    print(f"Configuración:")
    print(f"  - Número de filósofos: {num_filosofos}")
    print(f"  - Duración: {duracion} segundos")
    print("="*70 + "\n")
    
    mesa_ipc = None
    filosofos = []
    
    try:
        # Crear la mesa IPC
        mesa_ipc = MesaIPC(num_filosofos)
        
        # Crear los filósofos
        filosofos = [ProcesoFilosofo(i, mesa_ipc) for i in range(num_filosofos)]
        
        print("Iniciando procesos...\n")
        
        # Iniciar todos los procesos
        for filosofo in filosofos:
            filosofo.iniciar()
        
        print(f"\nSimulación corriendo por {duracion} segundos...")
        print("Presiona Ctrl+C para detener antes.\n")
        
        # Ejecutar por el tiempo especificado
        time.sleep(duracion)
        
    except KeyboardInterrupt:
        print("\n\nSimulación interrumpida por el usuario.\n")
    
    finally:
        # Terminar todos los procesos
        print("\nTerminando procesos...")
        for filosofo in filosofos:
            filosofo.terminar()
        
        print("\n" + "="*70)
        print("Fin de la simulación")
        print("="*70 + "\n")
        
        # Imprimir estadísticas finales (después de que todos terminaron)
        if mesa_ipc:
            mesa_ipc.imprimir_estadisticas()
        
        # Limpiar recursos IPC
        if mesa_ipc:
            mesa_ipc.limpiar()



if __name__ == "__main__":
    # Necesario en Windows para multiprocessing
    multiprocessing.freeze_support()
    main()
