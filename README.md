# 🍽️ Problema de los Filósofos Comensales

Implementación del clásico problema de sincronización de los Filósofos Comensales en tres lenguajes de programación: C, Java y Python. Cada implementación incluye soluciones tanto con **hilos** (threads) como con **procesos**.

## 📋 Descripción del Problema

El problema de los Filósofos Comensales es un problema clásico de sincronización propuesto por Edsger Dijkstra. En este problema:

- 🧑‍🎓 **N filósofos** se sientan alrededor de una mesa circular
- 🍴 Hay **N tenedores**, uno entre cada par de filósofos
- 🤔 Cada filósofo alterna entre **pensar** y **comer**
- 🍝 Para comer, un filósofo necesita **dos tenedores** (el de su izquierda y el de su derecha)
- ⚠️ El desafío es evitar **deadlocks** (bloqueos mutuos) y **starvation** (inanición)

## 🎯 Objetivos del Proyecto

Este proyecto implementa soluciones que:

1. ✅ **Evitan deadlocks** mediante estrategias de adquisición de recursos
2. ✅ **Previenen starvation** garantizando que todos los filósofos puedan comer
3. ✅ **Demuestran concurrencia** usando hilos y procesos
4. ✅ **Implementan sincronización** con semáforos y mecanismos IPC
5. ✅ **Proporcionan estadísticas** del comportamiento del sistema

## 📁 Estructura del Proyecto

```
deber2/
├── filosofos_c/          # Implementación en C
├── filosofos_java/       # Implementación en Java
└── filosofos_python/     # Implementación en Python
```

---

## 🔵 Implementación en C

### 📂 Estructura

```
filosofos_c/
├── Makefile
├── bin/
│   ├── filosofos           # Ejecutable con hilos
│   └── filosofos_procesos  # Ejecutable con procesos
├── obj/                    # Archivos objeto compilados
└── src/
    ├── hilos/             # Solución con threads POSIX
    │   ├── main.c
    │   ├── filosofo.c/h
    │   ├── mesa.c/h
    │   └── tenedor.c/h
    └── procesos/          # Solución con fork()
        ├── main.c
        ├── proceso_filosofo.c/h
        └── mesa_ipc.c/h
```

### 🛠️ Compilación

```bash
cd filosofos_c
make                # Compila ambas versiones
make clean          # Limpia archivos compilados
```

### ▶️ Ejecución

**Versión con Hilos:**
```bash
./bin/filosofos [num_filosofos] [duracion_segundos]
# Ejemplo:
./bin/filosofos 5 30
make run-hilos      # Atajo: ejecuta con 5 filósofos por 30 segundos
```

**Versión con Procesos:**
```bash
./bin/filosofos_procesos [num_filosofos] [duracion_segundos]
# Ejemplo:
./bin/filosofos_procesos 5 30
make run-procesos   # Atajo: ejecuta con 5 filósofos por 30 segundos
```

### 🔧 Tecnologías Utilizadas

- **POSIX Threads** (`pthread`) para la versión con hilos
- **Mutexes** (`pthread_mutex_t`) para sincronización de hilos
- **Fork/Exec** para creación de procesos
- **Memoria compartida** (`shm_open`) para IPC
- **Semáforos POSIX** (`sem_t`) para sincronización entre procesos

---

## ☕ Implementación en Java

### 📂 Estructura

```
filosofos_java/
├── bin/                   # Archivos .class compilados
│   ├── hilos/
│   ├── procesos/
│   └── procesosreales/
└── src/
    ├── EjecutarHilos.java      # Script principal para hilos
    ├── EjecutarProcesos.java   # Script principal para procesos
    ├── hilos/                  # Solución con threads Java
    │   ├── Estado.java
    │   ├── Filosofo.java
    │   ├── Mesa.java
    │   └── Tenedor.java
    ├── procesos/               # Solución con procesos simulados
    │   ├── EstadoFilosofo.java
    │   ├── MesaIPC.java
    │   └── ProcesoFilosofo.java
    └── procesosreales/         # Solución con procesos reales
        ├── Estado.java
        ├── FilosofoClient.java
        ├── Launcher.java
        └── MesaServer.java
```

### 🛠️ Compilación

```bash
cd filosofos_java
javac -d bin src/hilos/*.java src/EjecutarHilos.java
javac -d bin src/procesos/*.java src/EjecutarProcesos.java
# O compilar todo:
javac -d bin src/**/*.java src/*.java
```

### ▶️ Ejecución

**Versión con Hilos:**
```bash
cd filosofos_java
java -cp bin EjecutarHilos [num_filosofos] [duracion_segundos]
# Ejemplo:
java -cp bin EjecutarHilos 5 30
```

**Versión con Procesos:**
```bash
cd filosofos_java
java -cp bin EjecutarProcesos [num_filosofos] [duracion_segundos]
# Ejemplo:
java -cp bin EjecutarProcesos 5 30
```

### 🔧 Tecnologías Utilizadas

- **Java Threads** (`Thread` class) para concurrencia
- **Synchronized** blocks y métodos para sincronización
- **Wait/Notify** para comunicación entre hilos
- **ProcessBuilder** para procesos reales (versión procesosreales)

---

## 🐍 Implementación en Python

### 📂 Estructura

```
filosofos_python/
├── ejecutar_hilos.py       # Script principal para hilos
├── ejecutar_procesos.py    # Script principal para procesos
├── solucion_hilos/         # Solución con threading
│   ├── __init__.py
│   ├── filosofo.py
│   ├── mesa.py
│   └── tenedor.py
└── solucion_procesos/      # Solución con multiprocessing
    ├── __init__.py
    ├── MesaIPC.py
    └── ProcesoFilosofo.py
```

### 🛠️ Requisitos

```bash
# Python 3.7 o superior
python --version
```

No se requieren dependencias externas. Todo está implementado con la biblioteca estándar de Python.

### ▶️ Ejecución

**Versión con Hilos:**
```bash
cd filosofos_python
python ejecutar_hilos.py [num_filosofos] [duracion_segundos]
# Ejemplo:
python ejecutar_hilos.py 5 30
```

**Versión con Procesos:**
```bash
cd filosofos_python
python ejecutar_procesos.py [num_filosofos] [duracion_segundos]
# Ejemplo:
python ejecutar_procesos.py 5 30
```

### 🔧 Tecnologías Utilizadas

- **threading** module para hilos
- **Lock** y **Condition** para sincronización de hilos
- **multiprocessing** module para procesos
- **Value, Array** de multiprocessing para memoria compartida
- **Semaphore** de multiprocessing para sincronización entre procesos

---

## 🎮 Parámetros de Ejecución

Todas las implementaciones aceptan los mismos parámetros opcionales:

| Parámetro | Descripción | Valor por defecto |
|-----------|-------------|-------------------|
| `num_filosofos` | Número de filósofos en la mesa | 5 |
| `duracion_segundos` | Duración de la simulación en segundos | 30 |

### Ejemplos de uso:

```bash
# 3 filósofos por 10 segundos
./bin/filosofos 3 10
java -cp bin EjecutarHilos 3 10
python ejecutar_hilos.py 3 10

# 7 filósofos por 60 segundos
./bin/filosofos_procesos 7 60
java -cp bin EjecutarProcesos 7 60
python ejecutar_procesos.py 7 60
```

---

## 📊 Salida del Programa

Durante la ejecución, el programa muestra:

```
======================================================================
PROBLEMA DE LOS FILÓSOFOS COMENSALES - SOLUCIÓN CON HILOS
======================================================================
Configuración:
  - Número de filósofos: 5
  - Duración: 30 segundos
======================================================================

Filósofo 0 está PENSANDO...
Filósofo 1 está PENSANDO...
Filósofo 2 está PENSANDO...
Filósofo 0 tiene HAMBRE, intentando tomar tenedores...
Filósofo 0 está COMIENDO
...
```

Al finalizar, se muestran estadísticas del comportamiento del sistema.

---

## 🔍 Conceptos Clave Implementados

### 1. **Hilos (Threads)**
- Procesos ligeros que comparten el mismo espacio de memoria
- Comunicación eficiente a través de variables compartidas
- Sincronización mediante mutexes/locks

### 2. **Procesos**
- Procesos independientes con espacios de memoria separados
- Comunicación mediante IPC (Inter-Process Communication)
- Sincronización mediante semáforos nombrados o memoria compartida

### 3. **Prevención de Deadlock**
Implementaciones usan diferentes estrategias:
- **Orden de recursos**: Adquisición ordenada de tenedores
- **Asimetría**: Filósofos pares/impares toman tenedores en orden diferente
- **Arbitraje**: Un árbitro (mesa) controla el acceso a los recursos

### 4. **Prevención de Starvation**
- Sistemas de colas FIFO
- Mecanismos de espera justa
- Monitoreo de tiempos de espera

---

## 🧪 Casos de Prueba Sugeridos

1. **Caso pequeño**: 3 filósofos, 10 segundos
   - Verificar comportamiento básico
   
2. **Caso estándar**: 5 filósofos, 30 segundos
   - Configuración clásica del problema
   
3. **Caso grande**: 10 filósofos, 60 segundos
   - Probar escalabilidad y concurrencia intensa
   
4. **Caso extremo**: 2 filósofos, 20 segundos
   - Caso límite mínimo
   
5. **Interrupción**: Cualquier configuración + Ctrl+C
   - Verificar limpieza apropiada de recursos

---

## 🛡️ Manejo de Recursos

Todas las implementaciones garantizan:

- ✅ **Liberación de memoria** al finalizar
- ✅ **Cierre de threads/procesos** de forma limpia
- ✅ **Limpieza de IPC** (semáforos, memoria compartida)
- ✅ **Manejo de señales** (Ctrl+C) para terminación controlada

---

## 📝 Notas Técnicas

### C
- Requiere compilador GCC con soporte POSIX
- Probado en Linux/Unix
- Usar `-pthread` flag para compilación

### Java
- Requiere JDK 8 o superior
- Compatible con cualquier plataforma (Windows, Linux, macOS)

### Python
- Requiere Python 3.7+
- En Windows, `multiprocessing.freeze_support()` es necesario
- Compatible con cualquier plataforma

---

## 🐛 Solución de Problemas

### C - Error de compilación
```bash
# Asegúrate de tener las herramientas necesarias
sudo apt-get install build-essential
```

### Java - ClassNotFoundException
```bash
# Asegúrate de estar en el directorio correcto
cd filosofos_java
# Usa -cp bin para especificar el classpath
java -cp bin EjecutarHilos
```

### Python - ModuleNotFoundError
```bash
# Ejecuta desde el directorio filosofos_python
cd filosofos_python
python ejecutar_hilos.py
```
---

## 🔗 Referencias

1. Dijkstra, E. W. (1971). "Hierarchical ordering of sequential processes"
2. Tanenbaum, A. S. "Modern Operating Systems"
3. POSIX Threads Programming: https://computing.llnl.gov/tutorials/pthreads/
4. Java Concurrency Tutorial: https://docs.oracle.com/javase/tutorial/essential/concurrency/
5. Python Threading Documentation: https://docs.python.org/3/library/threading.html


## Documentación del uso de IA

Uso de LLMs (documentación mínima)

- Se utilizó un LLM como apoyo puntual para:
   - Validar el enfoque tipo **monitor** para evitar deadlocks: cada filósofo tiene un estado y solo se autoriza comer si sus vecinos no están comiendo. Esto se materializó con `pthread_mutex + pthread_cond` en C (hilos), con **semáforos y memoria compartida** en C (procesos), con **Semaphore** en Java y con `multiprocessing.Semaphore/Lock` en Python.
   - Recomendar un patrón de **terminación limpia** para permitir reruns sin residuos: bandera de salida y señales para despertar esperas. Aplicado como: `mesa->terminar` + `pthread_cond_broadcast` y `pthread_join` en C (hilos); `sem_destroy` + `munmap` en C (procesos); liberación de semáforos y **cierre de sockets** en `MesaServer` (Java); y finalización ordenada de procesos/hilos con `Value/Array` compartidos en Python.
   - Diseñar un **protocolo mínimo TCP** para procesos Java reales: comandos `REGISTER`, `TOMAR`, `SOLTAR`, `PING` entre clientes y `MesaServer` usando `ServerSocket/Socket` y `Semaphore`, en lugar de usar RMI u otras colas. Esto está implementado en `procesosreales/MesaServer.java` y clientes.
   - Afinar detalles de **sincronización en Python** con `multiprocessing`: semáforo por tenedor, `Lock` global y contadores/estados con `Value`/`Array` (ver `solucion_procesos/MesaIPC.py`).

- Verificación local: las implementaciones se ejecutaron y verificaron en **WSL Ubuntu**.

- Criterios finales de diseño: se ajustaron los **nombres y estados**, el **protocolo TCP** y el **formato de impresión** para claridad, trazabilidad y facilidad de rerun. No se usaron semáforos o memoria compartida POSIX con nombre; en su lugar se emplearon semáforos anónimos y memoria compartida vía `mmap` en C (procesos).