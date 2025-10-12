#ifndef PROCESO_FILOSOFO_H
#define PROCESO_FILOSOFO_H

#include <sys/types.h>

// Forward declaration
typedef struct MesaIPC MesaIPC;

/**
 * Enumeración para los estados del filósofo
 */
typedef enum {
    PROC_PENSANDO = 0,
    PROC_HAMBRIENTO = 1,
    PROC_COMIENDO = 2
} EstadoProceso;

/**
 * Estructura que representa un filósofo como proceso.
 */
typedef struct {
    int id;
    pid_t pid;
    MesaIPC* mesa_ipc;
} ProcesoFilosofo;

/**
 * Inicializa un filósofo proceso.
 * 
 * @param filosofo Puntero al filósofo
 * @param id Identificador único
 * @param mesa_ipc Puntero a la mesa IPC compartida
 */
void proceso_filosofo_init(ProcesoFilosofo* filosofo, int id, MesaIPC* mesa_ipc);

/**
 * Inicia el proceso del filósofo (hace fork).
 * 
 * @param filosofo Puntero al filósofo
 * @return PID del proceso hijo, o -1 en error
 */
pid_t proceso_filosofo_iniciar(ProcesoFilosofo* filosofo);

/**
 * Ciclo principal del proceso hijo.
 * 
 * @param filosofo Puntero al filósofo
 */
void proceso_filosofo_ciclo(ProcesoFilosofo* filosofo);

/**
 * Termina el proceso del filósofo.
 * 
 * @param filosofo Puntero al filósofo
 */
void proceso_filosofo_terminar(ProcesoFilosofo* filosofo);

#endif // PROCESO_FILOSOFO_H
