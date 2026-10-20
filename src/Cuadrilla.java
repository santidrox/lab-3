public class Cuadrilla {
    private final String codigo;
    private final double capacidad;
    private final boolean refrigeracion;
    public Cuadrilla(String codigo, double capacidad, boolean refrigeracion) {
        this.codigo = Validacion.texto(codigo, "Código de cuadrilla");
        this.capacidad = Validacion.positivo(capacidad, "Capacidad");
        this.refrigeracion = refrigeracion;
    }
    public String getCodigo() { return codigo; }
    public double getCapacidad() { return capacidad; }
    public boolean isRefrigeracion() { return refrigeracion; }
    @Override public String toString() { return codigo + " | capacidad " + capacidad + " h | refrigeración: " + (refrigeracion ? "sí" : "no"); }
}
