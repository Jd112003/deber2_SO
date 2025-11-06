#include "filosofo.h"
#include "mesa.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <time.h>

const char* estado_to_string(Estado estado) {
    switch (estado) {
        case PENSANDO: return "PENSANDO";
        case HAMBRIENTO: return "HAMBRIENTO";
        case COMIENDO: return "COMIENDO";
        default: return "DESCONOCIDO";
    }
}

/**
 * Genera un número aleatorio entre min y max (en milisegundos).
 */
static int random_sleep_time(int min_ms, int max_ms) {
    return min_ms + (rand() % (max_ms - min_ms + 1));
}

/**
 * Simula el tiempo de pensamiento del filósofo.
 */
static void pensar(Filosofo* filosofo) {
    filosofo->estado = PENSANDO;
    int tiempo_ms = random_sleep_time(1000, 3000);
    printf("Filósofo %d está PENSANDO por %.2f segundos\n", 
           filosofo->id, tiempo_ms / 1000.0);
    usleep(tiempo_ms * 1000);  // usleep toma microsegundos
}

/**
 * Simula el tiempo de comida del filósofo.
 */
static void comer(Filosofo* filosofo) {
    filosofo->estado = COMIENDO;
    filosofo->veces_comido++;
    int tiempo_ms = random_sleep_time(1000, 3000);
    printf("Filósofo %d está COMIENDO por %.2f segundos\n", 
           filosofo->id, tiempo_ms / 1000.0);
    usleep(tiempo_ms * 1000);
}

/**
 * Solicita los tenedores a la mesa.
 */
static void tomar_tenedores(Filosofo* filosofo) {
    filosofo->estado = HAMBRIENTO;
    printf("Filósofo %d está HAMBRIENTO y quiere comer\n", filosofo->id);
    mesa_tomar_tenedores(filosofo->mesa, filosofo->id);
}

/**
 * Libera los tenedores.
 */
static void soltar_tenedores(Filosofo* filosofo) {
    printf("Filósofo %d soltó los tenedores\n", filosofo->id);
    mesa_soltar_tenedores(filosofo->mesa, filosofo->id);
}

void filosofo_init(Filosofo* filosofo, int id, Mesa* mesa) {
    filosofo->id = id;
    filosofo->mesa = mesa;
    filosofo->estado = PENSANDO;
    filosofo->veces_comido = 0;
}

void* filosofo_run(void* arg) {
    Filosofo* filosofo = (Filosofo*)arg;
    
    // Inicializar semilla aleatoria para este hilo
    srand(time(NULL) + filosofo->id);
    
    while (!filosofo->mesa->terminar) {
        pensar(filosofo);
        if (filosofo->mesa->terminar) break;
        
        tomar_tenedores(filosofo);
        if (filosofo->mesa->terminar) break;
        
        comer(filosofo);
        if (filosofo->mesa->terminar) break;
        
        soltar_tenedores(filosofo);
    }
    
    printf("Filósofo %d finalizó su ejecución\n", filosofo->id);
    return NULL;
}

void filosofo_iniciar(Filosofo* filosofo) {
    pthread_create(&filosofo->hilo, NULL, filosofo_run, filosofo);
}
