# Makefile para el proyecto de Filósofos Comensales en C

CC = gcc
CFLAGS_HILOS = -Wall -Wextra -pthread -I./src/hilos
CFLAGS_PROCESOS = -Wall -Wextra -pthread -I./src/procesos
LDFLAGS = -pthread -lrt
TARGET_HILOS = bin/filosofos
TARGET_PROCESOS = bin/filosofos_procesos
SRC_HILOS_DIR = src/hilos
SRC_PROCESOS_DIR = src/procesos
OBJ_DIR = obj
BIN_DIR = bin

# Archivos fuente para versión con hilos
SOURCES_HILOS = $(SRC_HILOS_DIR)/main.c $(SRC_HILOS_DIR)/filosofo.c $(SRC_HILOS_DIR)/mesa.c $(SRC_HILOS_DIR)/tenedor.c
OBJECTS_HILOS = $(OBJ_DIR)/hilos/main.o $(OBJ_DIR)/hilos/filosofo.o $(OBJ_DIR)/hilos/mesa.o $(OBJ_DIR)/hilos/tenedor.o

# Archivos fuente para versión con procesos
SOURCES_PROCESOS = $(SRC_PROCESOS_DIR)/main.c $(SRC_PROCESOS_DIR)/proceso_filosofo.c $(SRC_PROCESOS_DIR)/mesa_ipc.c
OBJECTS_PROCESOS = $(OBJ_DIR)/procesos/main.o $(OBJ_DIR)/procesos/proceso_filosofo.o $(OBJ_DIR)/procesos/mesa_ipc.o

# Regla principal
all: $(BIN_DIR) $(OBJ_DIR) $(TARGET_HILOS) $(TARGET_PROCESOS)

# Crear directorios si no existen
$(BIN_DIR):
	mkdir -p $(BIN_DIR)

$(OBJ_DIR):
	mkdir -p $(OBJ_DIR)/hilos
	mkdir -p $(OBJ_DIR)/procesos

# Compilar el ejecutable de hilos
$(TARGET_HILOS): $(OBJECTS_HILOS)
	$(CC) $(OBJECTS_HILOS) -o $(TARGET_HILOS) $(LDFLAGS)
	@echo "Compilación exitosa: $(TARGET_HILOS)"

# Compilar el ejecutable de procesos
$(TARGET_PROCESOS): $(OBJECTS_PROCESOS)
	$(CC) $(OBJECTS_PROCESOS) -o $(TARGET_PROCESOS) $(LDFLAGS)
	@echo "Compilación exitosa: $(TARGET_PROCESOS)"

# Compilar archivos objeto para hilos
$(OBJ_DIR)/hilos/%.o: $(SRC_HILOS_DIR)/%.c
	$(CC) $(CFLAGS_HILOS) -c $< -o $@

# Compilar archivos objeto para procesos
$(OBJ_DIR)/procesos/%.o: $(SRC_PROCESOS_DIR)/%.c
	$(CC) $(CFLAGS_PROCESOS) -c $< -o $@

# Limpiar archivos generados
clean:
	rm -rf $(OBJ_DIR) $(BIN_DIR)
	@echo "Archivos limpios"

# Ejecutar el programa con hilos
run-hilos: $(TARGET_HILOS)
	$(TARGET_HILOS) 5 30

# Ejecutar el programa con procesos
run-procesos: $(TARGET_PROCESOS)
	$(TARGET_PROCESOS) 5 30

# Ayuda
help:
	@echo "Uso del Makefile:"
	@echo "  make              - Compila ambos proyectos (hilos y procesos)"
	@echo "  make run-hilos    - Compila y ejecuta versión con hilos"
	@echo "  make run-procesos - Compila y ejecuta versión con procesos"
	@echo "  make clean        - Elimina archivos objeto y ejecutables"
	@echo "  make help         - Muestra esta ayuda"

.PHONY: all clean run help
