#ifndef TENEDOR_H
#define TENEDOR_H

#include <pthread.h>

/**
 * Estructura que representa un tenedor.
 * Utiliza un mutex para controlar el acceso exclusivo.
 */
typedef struct {
    int id;
    pthread_mutex_t mutex;
} Tenedor;

/**
 * Inicializa un tenedor.
 * 
 * @param tenedor Puntero al tenedor a inicializar
 * @param id Identificador único del tenedor
 */
void tenedor_init(Tenedor* tenedor, int id);

/**
 * Destruye un tenedor y libera sus recursos.
 * 
 * @param tenedor Puntero al tenedor a destruir
 */
void tenedor_destroy(Tenedor* tenedor);

/**
 * Toma el tenedor (bloquea el mutex).
 * 
 * @param tenedor Puntero al tenedor
 */
void tenedor_tomar(Tenedor* tenedor);

/**
 * Suelta el tenedor (desbloquea el mutex).
 * 
 * @param tenedor Puntero al tenedor
 */
void tenedor_soltar(Tenedor* tenedor);

#endif // TENEDOR_H
