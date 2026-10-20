import java.math.BigDecimal;

public class MantenimientoAire extends OrdenServicio {
    private final int btu;
    public MantenimientoAire(String codigo, String cliente, String direccion, double horas, Zona zona, int btu) {
        super(codigo, cliente, direccion, horas, zona);
        Validacion.exigir(zona == Zona.METROPOLITANA, "El mantenimiento de aire solo admite zona metropolitana.");
        Validacion.exigir(btu >= 9000 && btu <= 60000, "La capacidad debe estar entre 9000 y 60000 BTU.");
        this.btu = btu;
    }
    public int getBtu() { return btu; }
    @Override public BigDecimal calcularCosto() { return tarifaBase().add(BigDecimal.valueOf(getHorasEstimadas()).multiply(new BigDecimal("110"))).add(new BigDecimal("80")); }
    @Override public int diasAtencion() { return 2; }
    @Override public int maxVisitas() { return 1; }
    @Override public String categoria() { return "Mantenimiento de aire acondicionado"; }
    @Override public boolean compatible(Cuadrilla cuadrilla) { return cuadrilla != null && cuadrilla.isRefrigeracion(); }
}
