package procesosreales;

/**
 * Enum para los estados del filósofo.
 */
public enum Estado {
    PENSANDO("PENSANDO"),
    HAMBRIENTO("HAMBRIENTO"),
    COMIENDO("COMIENDO");
    
    private final String nombre;
    
    Estado(String nombre) {
        this.nombre = nombre;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}
