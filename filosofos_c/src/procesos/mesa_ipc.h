#ifndef MESA_IPC_H
#define MESA_IPC_H

#include <semaphore.h>
#include <sys/mman.h>

/**
 * Estructura que representa la mesa usando IPC con memoria compartida.
 * Utiliza semáforos POSIX para sincronización entre procesos.
 */
typedef struct MesaIPC {
    int num_filosofos;
    sem_t* tenedores_sem;      // Array de semáforos de tenedores
    sem_t* mutex_global;        // Semáforo mutex global
    int* tabla_estados;         // Array de estados en memoria compartida
    sem_t* sem_espera;         // Array de semáforos de espera
    int* solicitudes_atendidas; // Contador en memoria compartida
    int* terminar;             // Bandera de terminación en memoria compartida
    int* veces_comido;         // Array de contadores de veces que comió cada filósofo
} MesaIPC;

/**
 * Inicializa la mesa IPC con memoria compartida y semáforos.
 * 
 * @param mesa Puntero a la mesa
 * @param num_filosofos Número de filósofos
 * @return 0 en éxito, -1 en error
 */
int mesa_ipc_init(MesaIPC* mesa, int num_filosofos);

/**
 * Destruye la mesa IPC y libera recursos.
 * 
 * @param mesa Puntero a la mesa
 */
void mesa_ipc_destroy(MesaIPC* mesa);

/**
 * Procesa la solicitud de un filósofo para tomar tenedores.
 * 
 * @param mesa Puntero a la mesa
 * @param id Índice del filósofo
 */
void mesa_ipc_tomar_tenedores(MesaIPC* mesa, int id);

/**
 * Procesa la liberación de tenedores de un filósofo.
 * 
 * @param mesa Puntero a la mesa
 * @param id Índice del filósofo
 */
void mesa_ipc_soltar_tenedores(MesaIPC* mesa, int id);

/**
 * Muestra estadísticas de la mesa.
 * 
 * @param mesa Puntero a la mesa
 */
void mesa_ipc_mostrar_estadisticas(MesaIPC* mesa);

#endif // MESA_IPC_H
