import java.math.BigDecimal;

public abstract class OrdenServicio {
    private final String codigo, cliente, direccion;
    private final double horasEstimadas;
    private final Zona zona;
    private EstadoOrden estado = EstadoOrden.PENDIENTE;
    private int visitas;

    public OrdenServicio(String codigo, String cliente, String direccion, double horas, Zona zona) {
        this.codigo = Validacion.texto(codigo, "Código");
        this.cliente = Validacion.texto(cliente, "Cliente");
        this.direccion = Validacion.texto(direccion, "Dirección");
        this.horasEstimadas = Validacion.positivo(horas, "Horas");
        Validacion.exigir(zona != null, "Zona requerida.");
        this.zona = zona;
    }
    public abstract BigDecimal calcularCosto();
    public abstract int diasAtencion();
    public abstract int maxVisitas();
    public abstract String categoria();
    public boolean compatible(Cuadrilla cuadrilla) { return cuadrilla != null; }
    protected BigDecimal tarifaBase() { return new BigDecimal(zona == Zona.METROPOLITANA ? "100" : "175"); }
    public String getCodigo() { return codigo; }
    public String getCliente() { return cliente; }
    public String getDireccion() { return direccion; }
    public double getHorasEstimadas() { return horasEstimadas; }
    public Zona getZona() { return zona; }
    public EstadoOrden getEstado() { return estado; }
    public int getVisitas() { return visitas; }
    void asignar() {
        Validacion.exigir(estado == EstadoOrden.PENDIENTE && visitas < maxVisitas(), "La orden no está pendiente o agotó sus visitas.");
        estado = EstadoOrden.ASIGNADA;
    }
    void retirar() {
        Validacion.exigir(estado == EstadoOrden.ASIGNADA, "La orden no está asignada.");
        estado = EstadoOrden.PENDIENTE;
    }
    BigDecimal iniciarVisita() {
        Validacion.exigir(estado == EstadoOrden.ASIGNADA && visitas < maxVisitas(), "La orden no permite iniciar una visita.");
        visitas++;
        estado = EstadoOrden.EN_ATENCION;
        return visitas > 1 ? new BigDecimal("40") : BigDecimal.ZERO;
    }
    void finalizarVisita(boolean completada) {
        Validacion.exigir(estado == EstadoOrden.EN_ATENCION, "La orden no está en atención.");
        estado = completada ? EstadoOrden.COMPLETADA : visitas >= maxVisitas() ? EstadoOrden.CANCELADA : EstadoOrden.PENDIENTE;
    }
    @Override public String toString() {
        return codigo + " | " + categoria() + " | " + cliente + " | " + direccion + " | " + zona
            + " | " + horasEstimadas + " h | " + estado + " | visitas " + visitas + "/" + maxVisitas()
            + " | costo " + Sistema.formato(calcularCosto()) + " | " + diasAtencion() + " días hábiles";
    }
}
