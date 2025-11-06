#include "mesa_ipc.h"
#include "proceso_filosofo.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * Calcula el índice del tenedor izquierdo.
 */
static int izq(int i) {
    return i;
}

/**
 * Calcula el índice del tenedor derecho.
 */
static int der(MesaIPC* mesa, int i) {
    return (i + 1) % mesa->num_filosofos;
}

/**
 * Calcula el índice del vecino izquierdo.
 */
static int vecino_izq(MesaIPC* mesa, int i) {
    return (i - 1 + mesa->num_filosofos) % mesa->num_filosofos;
}

/**
 * Calcula el índice del vecino derecho.
 */
static int vecino_der(MesaIPC* mesa, int i) {
    return (i + 1) % mesa->num_filosofos;
}

/**
 * Verifica si el filósofo puede comer.
 */
static int autorizar(MesaIPC* mesa, int id) {
    if (mesa->tabla_estados[id] != PROC_HAMBRIENTO) {
        return 0;
    }
    
    int vec_izq = vecino_izq(mesa, id);
    int vec_der = vecino_der(mesa, id);
    
    int puede_comer = (
        mesa->tabla_estados[vec_izq] != PROC_COMIENDO &&
        mesa->tabla_estados[vec_der] != PROC_COMIENDO
    );
    
    if (puede_comer) {
        mesa->tabla_estados[id] = PROC_COMIENDO;
        printf("  [MESA] Filósofo %d autorizado para COMER\n", id);
        return 1;
    } else {
        printf("  [MESA] Filósofo %d debe esperar (vecinos comiendo)\n", id);
        return 0;
    }
}

/**
 * Notifica a un filósofo.
 */
static void notificar(MesaIPC* mesa, int id) {
    printf("  [MESA] Notificando a Filósofo %d\n", id);
    sem_post(&mesa->sem_espera[id]);
}

int mesa_ipc_init(MesaIPC* mesa, int num_filosofos) {
    mesa->num_filosofos = num_filosofos;
    
    // Asignar memoria compartida para array de semáforos de tenedores
    mesa->tenedores_sem = mmap(NULL, num_filosofos * sizeof(sem_t),
                                PROT_READ | PROT_WRITE,
                                MAP_SHARED | MAP_ANONYMOUS, -1, 0);
    
    // Inicializar semáforos de tenedores
    for (int i = 0; i < num_filosofos; i++) {
        sem_init(&mesa->tenedores_sem[i], 1, 1); // 1 = compartido entre procesos
    }
    
    // Asignar memoria compartida para mutex global
    mesa->mutex_global = mmap(NULL, sizeof(sem_t),
                              PROT_READ | PROT_WRITE,
                              MAP_SHARED | MAP_ANONYMOUS, -1, 0);
    sem_init(mesa->mutex_global, 1, 1);
    
    // Asignar memoria compartida para tabla de estados
    mesa->tabla_estados = mmap(NULL, num_filosofos * sizeof(int),
                               PROT_READ | PROT_WRITE,
                               MAP_SHARED | MAP_ANONYMOUS, -1, 0);
    for (int i = 0; i < num_filosofos; i++) {
        mesa->tabla_estados[i] = PROC_PENSANDO;
    }
    
    // Asignar memoria compartida para semáforos de espera
    mesa->sem_espera = mmap(NULL, num_filosofos * sizeof(sem_t),
                            PROT_READ | PROT_WRITE,
                            MAP_SHARED | MAP_ANONYMOUS, -1, 0);
    for (int i = 0; i < num_filosofos; i++) {
        sem_init(&mesa->sem_espera[i], 1, 0);
    }
    
    // Asignar memoria compartida para contador
    mesa->solicitudes_atendidas = mmap(NULL, sizeof(int),
                                       PROT_READ | PROT_WRITE,
                                       MAP_SHARED | MAP_ANONYMOUS, -1, 0);
    *mesa->solicitudes_atendidas = 0;
    
    // Asignar memoria compartida para bandera de terminación
    mesa->terminar = mmap(NULL, sizeof(int),
                         PROT_READ | PROT_WRITE,
                         MAP_SHARED | MAP_ANONYMOUS, -1, 0);
    *mesa->terminar = 0;
    
    // Asignar memoria compartida para contador de veces que comió cada filósofo
    mesa->veces_comido = mmap(NULL, num_filosofos * sizeof(int),
                              PROT_READ | PROT_WRITE,
                              MAP_SHARED | MAP_ANONYMOUS, -1, 0);
    for (int i = 0; i < num_filosofos; i++) {
        mesa->veces_comido[i] = 0;
    }
    
    return 0;
}

void mesa_ipc_destroy(MesaIPC* mesa) {
    // Destruir semáforos de tenedores
    for (int i = 0; i < mesa->num_filosofos; i++) {
        sem_destroy(&mesa->tenedores_sem[i]);
    }
    munmap(mesa->tenedores_sem, mesa->num_filosofos * sizeof(sem_t));
    
    // Destruir mutex global
    sem_destroy(mesa->mutex_global);
    munmap(mesa->mutex_global, sizeof(sem_t));
    
    // Liberar tabla de estados
    munmap(mesa->tabla_estados, mesa->num_filosofos * sizeof(int));
    
    // Destruir semáforos de espera
    for (int i = 0; i < mesa->num_filosofos; i++) {
        sem_destroy(&mesa->sem_espera[i]);
    }
    munmap(mesa->sem_espera, mesa->num_filosofos * sizeof(sem_t));
    
    // Liberar contador
    munmap(mesa->solicitudes_atendidas, sizeof(int));
    
    // Liberar bandera de terminación
    munmap(mesa->terminar, sizeof(int));
    
    // Liberar contador de veces comido
    munmap(mesa->veces_comido, mesa->num_filosofos * sizeof(int));
    
    printf("Recursos IPC liberados\n");
}

void mesa_ipc_tomar_tenedores(MesaIPC* mesa, int id) {
    // Verificar si debe terminar
    if (*mesa->terminar) {
        return;
    }
    
    sem_wait(mesa->mutex_global);
    
    // Cambiar estado a HAMBRIENTO
    mesa->tabla_estados[id] = PROC_HAMBRIENTO;
    printf("  [MESA] Filósofo %d solicita tenedores %d y %d\n", 
           id, izq(id), der(mesa, id));
    
    // Intentar autorizar inmediatamente
    if (autorizar(mesa, id)) {
        sem_post(&mesa->sem_espera[id]);
    } else {
        printf("  [MESA] Filósofo %d bloqueado esperando recursos\n", id);
    }
    
    sem_post(mesa->mutex_global);
    
    // Esperar hasta que sea autorizado
    sem_wait(&mesa->sem_espera[id]);
    
    // Verificar nuevamente si debe terminar
    if (*mesa->terminar) {
        return;
    }
    
    // Tomar los semáforos de los tenedores
    sem_wait(&mesa->tenedores_sem[izq(id)]);
    sem_wait(&mesa->tenedores_sem[der(mesa, id)]);
    
    printf("  [MESA] Filósofo %d tomó tenedores %d y %d\n", 
           id, izq(id), der(mesa, id));
    
    (*mesa->solicitudes_atendidas)++;
}

void mesa_ipc_soltar_tenedores(MesaIPC* mesa, int id) {
    // Liberar los semáforos de los tenedores
    sem_post(&mesa->tenedores_sem[der(mesa, id)]);
    sem_post(&mesa->tenedores_sem[izq(id)]);
    
    printf("  [MESA] Filósofo %d liberó tenedores %d y %d\n", 
           id, izq(id), der(mesa, id));
    
    sem_wait(mesa->mutex_global);
    
    // Cambiar estado a PENSANDO
    mesa->tabla_estados[id] = PROC_PENSANDO;
    
    // Intentar despertar a los vecinos
    int vec_izq = vecino_izq(mesa, id);
    int vec_der = vecino_der(mesa, id);
    
    if (autorizar(mesa, vec_izq)) {
        notificar(mesa, vec_izq);
    }
    
    if (autorizar(mesa, vec_der)) {
        notificar(mesa, vec_der);
    }
    
    sem_post(mesa->mutex_global);
}

void mesa_ipc_mostrar_estadisticas(MesaIPC* mesa) {
    printf("\n");
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n");
    printf("ESTADÍSTICAS FINALES\n");
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n");
    
    int total = 0;
    for (int i = 0; i < mesa->num_filosofos; i++) {
        printf("Filósofo %d comió %d veces\n", i, mesa->veces_comido[i]);
        total += mesa->veces_comido[i];
    }
    
    printf("\nTotal de veces que se comió: %d\n", total);
    printf("Promedio por filósofo: %.2f\n", (double)total / mesa->num_filosofos);
    
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n");
}
