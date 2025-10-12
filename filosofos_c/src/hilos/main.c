#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include "mesa.h"

static Mesa mesa_global;

/**
 * Manejador de señales para terminación limpia.
 */
void signal_handler(int signum) {
    printf("\n\nSimulación interrumpida por el usuario.\n\n");
    mesa_destroy(&mesa_global);
    exit(0);
}

/**
 * Script para ejecutar únicamente la solución con HILOS (threads POSIX).
 * 
 * Uso:
 *     ./filosofos [num_filosofos] [duracion_segundos]
 * 
 * Ejemplo:
 *     ./filosofos 5 30
 */
int main(int argc, char* argv[]) {
    // Parsear argumentos de línea de comandos
    int num_filosofos = 5;
    int duracion = 30;
    
    if (argc > 1) {
        num_filosofos = atoi(argv[1]);
        if (num_filosofos <= 0) {
            fprintf(stderr, "Error: El número de filósofos debe ser un entero positivo.\n");
            return 1;
        }
    }
    
    if (argc > 2) {
        duracion = atoi(argv[2]);
        if (duracion <= 0) {
            fprintf(stderr, "Error: La duración debe ser un entero positivo.\n");
            return 1;
        }
    }
    
    // Configurar manejador de señales
    signal(SIGINT, signal_handler);
    
    printf("\n");
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n");
    printf("PROBLEMA DE LOS FILÓSOFOS COMENSALES - SOLUCIÓN CON HILOS\n");
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n");
    printf("Configuración:\n");
    printf("  - Número de filósofos: %d\n", num_filosofos);
    printf("  - Duración: %d segundos\n", duracion);
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n\n");
    
    // Crear la mesa
    mesa_init(&mesa_global, num_filosofos);
    
    // Iniciar la simulación
    mesa_iniciar_cena(&mesa_global);
    
    printf("\nSimulación corriendo por %d segundos...\n", duracion);
    printf("Presiona Ctrl+C para detener antes.\n\n");
    
    // Ejecutar por el tiempo especificado
    sleep(duracion);
    
    printf("\n");
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n");
    printf("Fin de la simulación\n");
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n\n");
    
    // Limpiar recursos
    mesa_destroy(&mesa_global);
    
    return 0;
}
