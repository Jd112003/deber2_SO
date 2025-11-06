#include "mesa.h"
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
static int der(Mesa* mesa, int i) {
    return (i + 1) % mesa->num_filosofos;
}

/**
 * Verifica si el filósofo puede comer.
 */
static int permitir_comer(Mesa* mesa, int i) {
    int vecino_izq = (i - 1 + mesa->num_filosofos) % mesa->num_filosofos;
    int vecino_der = (i + 1) % mesa->num_filosofos;
    
    return (mesa->estados[i] == HAMBRIENTO &&
            mesa->estados[vecino_izq] != COMIENDO &&
            mesa->estados[vecino_der] != COMIENDO);
}

void mesa_init(Mesa* mesa, int num_filosofos) {
    mesa->num_filosofos = num_filosofos;
    mesa->terminar = 0;  // Inicializar bandera de terminación
    
    // Crear tenedores
    mesa->tenedores = (Tenedor*)malloc(num_filosofos * sizeof(Tenedor));
    for (int i = 0; i < num_filosofos; i++) {
        tenedor_init(&mesa->tenedores[i], i);
    }
    
    // Inicializar mutex y condición
    pthread_mutex_init(&mesa->mutex_mesa, NULL);
    pthread_cond_init(&mesa->monitor, NULL);
    
    // Inicializar estados
    mesa->estados = (Estado*)malloc(num_filosofos * sizeof(Estado));
    for (int i = 0; i < num_filosofos; i++) {
        mesa->estados[i] = PENSANDO;
    }
    
    // Crear filósofos
    mesa->filosofos = (Filosofo*)malloc(num_filosofos * sizeof(Filosofo));
    for (int i = 0; i < num_filosofos; i++) {
        filosofo_init(&mesa->filosofos[i], i, mesa);
    }
}

void mesa_destroy(Mesa* mesa) {
    // Indicar a los hilos que deben terminar
    mesa->terminar = 1;
    
    // Despertar a todos los hilos que puedan estar esperando
    pthread_cond_broadcast(&mesa->monitor);
    
    // Esperar a que terminen
    for (int i = 0; i < mesa->num_filosofos; i++) {
        pthread_join(mesa->filosofos[i].hilo, NULL);
    }
    
    // Destruir tenedores
    for (int i = 0; i < mesa->num_filosofos; i++) {
        tenedor_destroy(&mesa->tenedores[i]);
    }
    free(mesa->tenedores);
    
    // Destruir mutex y condición
    pthread_mutex_destroy(&mesa->mutex_mesa);
    pthread_cond_destroy(&mesa->monitor);
    
    // Liberar estados y filósofos
    free(mesa->estados);
    free(mesa->filosofos);
}

void mesa_tomar_tenedores(Mesa* mesa, int i) {
    pthread_mutex_lock(&mesa->mutex_mesa);
    
    // Verificar si debe terminar antes de proceder
    if (mesa->terminar) {
        pthread_mutex_unlock(&mesa->mutex_mesa);
        return;
    }
    
    // Cambiar estado a HAMBRIENTO
    mesa->estados[i] = HAMBRIENTO;
    printf("Filósofo %d intenta tomar tenedores %d y %d\n", 
           i, izq(i), der(mesa, i));
    
    // Intentar obtener permiso para comer
    while (!permitir_comer(mesa, i) && !mesa->terminar) {
        // Si no puede comer, espera en la condición
        pthread_cond_wait(&mesa->monitor, &mesa->mutex_mesa);
    }
    
    // Verificar nuevamente si debe terminar
    if (mesa->terminar) {
        pthread_mutex_unlock(&mesa->mutex_mesa);
        return;
    }
    
    // Puede comer: cambiar estado y tomar tenedores
    mesa->estados[i] = COMIENDO;
    tenedor_tomar(&mesa->tenedores[izq(i)]);
    tenedor_tomar(&mesa->tenedores[der(mesa, i)]);
    printf("Filósofo %d tomó los tenedores y está COMIENDO\n", i);
    
    pthread_mutex_unlock(&mesa->mutex_mesa);
}

void mesa_soltar_tenedores(Mesa* mesa, int i) {
    pthread_mutex_lock(&mesa->mutex_mesa);
    
    // Cambiar estado a PENSANDO
    mesa->estados[i] = PENSANDO;
    
    // Soltar los tenedores
    tenedor_soltar(&mesa->tenedores[izq(i)]);
    tenedor_soltar(&mesa->tenedores[der(mesa, i)]);
    printf("Filósofo %d soltó los tenedores\n", i);
    
    // Notificar a TODOS los filósofos que pueden intentar comer
    pthread_cond_broadcast(&mesa->monitor);
    
    pthread_mutex_unlock(&mesa->mutex_mesa);
}

void mesa_iniciar_cena(Mesa* mesa) {
    printf("\n");
    for (int i = 0; i < 60; i++) printf("=");
    printf("\n");
    printf("Iniciando cena con %d filósofos\n", mesa->num_filosofos);
    for (int i = 0; i < 60; i++) printf("=");
    printf("\n\n");
    
    for (int i = 0; i < mesa->num_filosofos; i++) {
        filosofo_iniciar(&mesa->filosofos[i]);
    }
}

void mesa_esperar(Mesa* mesa) {
    for (int i = 0; i < mesa->num_filosofos; i++) {
        pthread_join(mesa->filosofos[i].hilo, NULL);
    }
}

void mesa_imprimir_estadisticas(Mesa* mesa) {
    printf("\n");
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n");
    printf("ESTADÍSTICAS FINALES\n");
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n");
    
    int total = 0;
    for (int i = 0; i < mesa->num_filosofos; i++) {
        printf("Filósofo %d comió %d veces\n", i, mesa->filosofos[i].veces_comido);
        total += mesa->filosofos[i].veces_comido;
    }
    
    printf("\nTotal de veces que se comió: %d\n", total);
    printf("Promedio por filósofo: %.2f\n", (double)total / mesa->num_filosofos);
    
    for (int i = 0; i < 70; i++) printf("=");
    printf("\n");
}
