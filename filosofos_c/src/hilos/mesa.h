#ifndef MESA_H
#define MESA_H

#include <pthread.h>
#include "tenedor.h"
#include "filosofo.h"

/**
 * Estructura que representa la mesa donde comen los filósofos.
 * Coordina el acceso a los tenedores y evita deadlocks.
 */
typedef struct Mesa {
    int num_filosofos;
    Tenedor* tenedores;
    pthread_mutex_t mutex_mesa;
    pthread_cond_t monitor;
    Estado* estados;
    Filosofo* filosofos;
    volatile int terminar;  // Bandera para detener la ejecución
} Mesa;

/**
 * Inicializa la mesa con el número especificado de filósofos.
 * 
 * @param mesa Puntero a la mesa
 * @param num_filosofos Número de filósofos (y tenedores)
 */
void mesa_init(Mesa* mesa, int num_filosofos);

/**
 * Destruye la mesa y libera todos sus recursos.
 * 
 * @param mesa Puntero a la mesa
 */
void mesa_destroy(Mesa* mesa);

/**
 * Inicia la cena (ejecuta todos los filósofos).
 * 
 * @param mesa Puntero a la mesa
 */
void mesa_iniciar_cena(Mesa* mesa);

/**
 * Procesa la solicitud de un filósofo para tomar tenedores.
 * 
 * @param mesa Puntero a la mesa
 * @param i Índice del filósofo
 */
void mesa_tomar_tenedores(Mesa* mesa, int i);

/**
 * Procesa la liberación de tenedores de un filósofo.
 * 
 * @param mesa Puntero a la mesa
 * @param i Índice del filósofo
 */
void mesa_soltar_tenedores(Mesa* mesa, int i);

/**
 * Espera a que todos los hilos terminen.
 * 
 * @param mesa Puntero a la mesa
 */
void mesa_esperar(Mesa* mesa);

#endif // MESA_H
