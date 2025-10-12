#include "proceso_filosofo.h"
#include "mesa_ipc.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <time.h>
#include <signal.h>

/**
 * Genera un número aleatorio entre min y max (en milisegundos).
 */
static int random_sleep_time(int min_ms, int max_ms) {
    return min_ms + (rand() % (max_ms - min_ms + 1));
}

/**
 * Simula el tiempo de pensamiento del filósofo.
 */
static void pensar(ProcesoFilosofo* filosofo) {
    int tiempo_ms = random_sleep_time(1000, 3000);
    printf("[PID %d] Filósofo %d está PENSANDO por %.2f segundos\n", 
           getpid(), filosofo->id, tiempo_ms / 1000.0);
    usleep(tiempo_ms * 1000);
}

/**
 * Simula el tiempo de comida del filósofo.
 */
static void comer(ProcesoFilosofo* filosofo) {
    int tiempo_ms = random_sleep_time(1000, 3000);
    printf("[PID %d] Filósofo %d está COMIENDO por %.2f segundos\n", 
           getpid(), filosofo->id, tiempo_ms / 1000.0);
    usleep(tiempo_ms * 1000);
}

/**
 * Solicita los recursos a la mesa.
 */
static void solicitar_recursos(ProcesoFilosofo* filosofo) {
    printf("[PID %d] Filósofo %d está HAMBRIENTO y solicita recursos\n", 
           getpid(), filosofo->id);
    mesa_ipc_tomar_tenedores(filosofo->mesa_ipc, filosofo->id);
    printf("[PID %d] Filósofo %d obtuvo los recursos\n", 
           getpid(), filosofo->id);
}

/**
 * Libera los recursos.
 */
static void liberar_recursos(ProcesoFilosofo* filosofo) {
    printf("[PID %d] Filósofo %d libera recursos\n", 
           getpid(), filosofo->id);
    mesa_ipc_soltar_tenedores(filosofo->mesa_ipc, filosofo->id);
}

void proceso_filosofo_init(ProcesoFilosofo* filosofo, int id, MesaIPC* mesa_ipc) {
    filosofo->id = id;
    filosofo->pid = 0;
    filosofo->mesa_ipc = mesa_ipc;
}

void proceso_filosofo_ciclo(ProcesoFilosofo* filosofo) {
    // Inicializar semilla aleatoria para este proceso
    srand(time(NULL) ^ getpid());
    
    printf("[PID %d] Filósofo %d inició su proceso\n", getpid(), filosofo->id);
    
    while (!(*filosofo->mesa_ipc->terminar)) {
        pensar(filosofo);
        if (*filosofo->mesa_ipc->terminar) break;
        
        solicitar_recursos(filosofo);
        if (*filosofo->mesa_ipc->terminar) break;
        
        comer(filosofo);
        if (*filosofo->mesa_ipc->terminar) break;
        
        liberar_recursos(filosofo);
    }
    
    printf("[PID %d] Filósofo %d finalizó su ejecución\n", getpid(), filosofo->id);
}

pid_t proceso_filosofo_iniciar(ProcesoFilosofo* filosofo) {
    pid_t pid = fork();
    
    if (pid < 0) {
        perror("Error en fork");
        return -1;
    } else if (pid == 0) {
        // Código del proceso hijo
        proceso_filosofo_ciclo(filosofo);
        exit(0); // Nunca debería llegar aquí
    } else {
        // Código del proceso padre
        filosofo->pid = pid;
        printf("Filósofo %d iniciado con PID %d\n", filosofo->id, pid);
        return pid;
    }
}

void proceso_filosofo_terminar(ProcesoFilosofo* filosofo) {
    if (filosofo->pid > 0) {
        printf("Terminando Filósofo %d (PID %d)...\n", filosofo->id, filosofo->pid);
        kill(filosofo->pid, SIGTERM);
        usleep(100000); // Esperar 100ms
        kill(filosofo->pid, SIGKILL); // Forzar si es necesario
        printf("Filósofo %d terminado\n", filosofo->id);
    }
}
