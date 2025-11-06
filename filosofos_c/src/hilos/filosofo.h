#ifndef FILOSOFO_H
#define FILOSOFO_H

#include <pthread.h>

// Forward declaration
typedef struct Mesa Mesa;

/**
 * Enumeración para los estados del filósofo
 */
typedef enum {
    PENSANDO,
    HAMBRIENTO,
    COMIENDO
} Estado;

/**
 * Estructura que representa un filósofo.
 */
typedef struct {
    int id;
    Mesa* mesa;
    Estado estado;
    pthread_t hilo;
    int veces_comido;
} Filosofo;

/**
 * Inicializa un filósofo.
 * 
 * @param filosofo Puntero al filósofo a inicializar
 * @param id Identificador único del filósofo
 * @param mesa Puntero a la mesa compartida
 */
void filosofo_init(Filosofo* filosofo, int id, Mesa* mesa);

/**
 * Inicia la ejecución del filósofo en un hilo separado.
 * 
 * @param filosofo Puntero al filósofo
 */
void filosofo_iniciar(Filosofo* filosofo);

/**
 * Función principal que ejecuta el ciclo del filósofo.
 * 
 * @param arg Puntero al filósofo (void* para compatibilidad con pthread)
 * @return NULL
 */
void* filosofo_run(void* arg);

/**
 * Obtiene el nombre del estado como string.
 * 
 * @param estado Estado del filósofo
 * @return Nombre del estado
 */
const char* estado_to_string(Estado estado);

#endif // FILOSOFO_H
