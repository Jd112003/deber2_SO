package procesos;

/**
 * Enumeración para los estados del filósofo en memoria compartida
 */
public enum EstadoFilosofo {
    PENSANDO(0),
    HAMBRIENTO(1),
    COMIENDO(2);
    
    private final int valor;
    
    EstadoFilosofo(int valor) {
        this.valor = valor;
    }
    
    public int getValor() {
        return valor;
    }
    
    public static EstadoFilosofo fromValor(int valor) {
        for (EstadoFilosofo estado : values()) {
            if (estado.valor == valor) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Valor de estado inválido: " + valor);
    }
}
