#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>
#include "mesa_ipc.h"
#include "proceso_filosofo.h"

static MesaIPC mesa_ipc_global;
static ProcesoFilosofo* filosofos_global;
static int num_filosofos_global;

/**
 * Manejador de señales para terminación limpia.
 */
void signal_handler(int signum) {
    printf("\n\nSimulación interrumpida por el usuario.\n\n");
    
    // Terminar todos los procesos hijo
    for (int i = 0; i < num_filosofos_global; i++) {
        proceso_filosofo_terminar(&filosofos_global[i]);
    }
    
    // Limpiar recursos IPC
    mesa_ipc_destroy(&mesa_ipc_global);
    free(filosofos_global);
    
    exit(0);
}

/**
 * Script para ejecutar la solución con PROCESOS (usando fork).
 * 
 * Uso:
 *     ./filosofos_procesos [num_filosofos] [duracion_segundos]
 * 
 * Ejemplo:
 *     ./filosofos_procesos 5 30
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
    
    num_filosofos_global = num_filosofos;
    
    // Configurar manejador de señales
    signal(SIGINT, signal_handler);
    
    printf("\n");
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n");
    printf("PROBLEMA DE LOS FILÓSOFOS COMENSALES - SOLUCIÓN CON PROCESOS\n");
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n");
    printf("Configuración:\n");
    printf("  - Número de filósofos: %d\n", num_filosofos);
    printf("  - Duración: %d segundos\n", duracion);
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n\n");
    
    // Crear la mesa IPC
    if (mesa_ipc_init(&mesa_ipc_global, num_filosofos) < 0) {
        fprintf(stderr, "Error al inicializar mesa IPC\n");
        return 1;
    }
    
    // Crear los filósofos
    filosofos_global = malloc(num_filosofos * sizeof(ProcesoFilosofo));
    for (int i = 0; i < num_filosofos; i++) {
        proceso_filosofo_init(&filosofos_global[i], i, &mesa_ipc_global);
    }
    
    printf("Iniciando procesos...\n\n");
    
    // Iniciar todos los procesos
    for (int i = 0; i < num_filosofos; i++) {
        if (proceso_filosofo_iniciar(&filosofos_global[i]) < 0) {
            fprintf(stderr, "Error al iniciar filósofo %d\n", i);
            return 1;
        }
    }
    
    printf("\nSimulación corriendo por %d segundos...\n", duracion);
    printf("Presiona Ctrl+C para detener antes.\n\n");
    
    // Ejecutar por el tiempo especificado
    sleep(duracion);
    
    // Indicar a los procesos que deben terminar
    *mesa_ipc_global.terminar = 1;
    
    // Despertar a todos los procesos que puedan estar esperando
    for (int i = 0; i < num_filosofos; i++) {
        sem_post(&mesa_ipc_global.sem_espera[i]);
    }
    
    // Esperar a que todos los procesos terminen naturalmente
    printf("\nEsperando a que los procesos terminen...\n");
    for (int i = 0; i < num_filosofos; i++) {
        waitpid(filosofos_global[i].pid, NULL, 0);
    }
    
    printf("\n");
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n");
    printf("Fin de la simulación\n");
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n\n");
    
    // Mostrar estadísticas finales (después de que todos terminaron)
    mesa_ipc_mostrar_estadisticas(&mesa_ipc_global);
    
    // Limpiar recursos
    mesa_ipc_destroy(&mesa_ipc_global);
    free(filosofos_global);
    
    return 0;
}
