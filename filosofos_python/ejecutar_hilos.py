"""
Script para ejecutar únicamente la solución con HILOS.

Uso:
    python ejecutar_hilos.py [num_filosofos] [duracion_segundos]

Ejemplo:
    python ejecutar_hilos.py 5 30
"""

import sys
import time
from solucion_hilos.mesa import Mesa


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
    print("PROBLEMA DE LOS FILÓSOFOS COMENSALES - SOLUCIÓN CON HILOS")
    print("="*70)
    print(f"Configuración:")
    print(f"  - Número de filósofos: {num_filosofos}")
    print(f"  - Duración: {duracion} segundos")
    print("="*70 + "\n")
    
    try:
        # Crear la mesa
        mesa = Mesa(num_filosofos)
        
        # Iniciar la simulación
        mesa.iniciar_cena()
        
        print(f"\nSimulación corriendo por {duracion} segundos...")
        print("Presiona Ctrl+C para detener antes.\n")
        
        # Ejecutar por el tiempo especificado
        time.sleep(duracion)
        
        print("\n" + "="*70)
        print("Fin de la simulación")
        print("="*70 + "\n")
        
        # Detener todos los filósofos y esperar a que terminen
        mesa.detener()
        
        # Imprimir estadísticas finales
        mesa.imprimir_estadisticas()
        
    except KeyboardInterrupt:
        print("\n\nSimulación interrumpida por el usuario.\n")


if __name__ == "__main__":
    main()
