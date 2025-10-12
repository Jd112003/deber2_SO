#include "tenedor.h"
#include <stdio.h>

void tenedor_init(Tenedor* tenedor, int id) {
    tenedor->id = id;
    pthread_mutex_init(&tenedor->mutex, NULL);
}

void tenedor_destroy(Tenedor* tenedor) {
    pthread_mutex_destroy(&tenedor->mutex);
}

void tenedor_tomar(Tenedor* tenedor) {
    pthread_mutex_lock(&tenedor->mutex);
    printf("  -> Tenedor %d tomado\n", tenedor->id);
}

void tenedor_soltar(Tenedor* tenedor) {
    pthread_mutex_unlock(&tenedor->mutex);
    printf("  -> Tenedor %d liberado\n", tenedor->id);
}
