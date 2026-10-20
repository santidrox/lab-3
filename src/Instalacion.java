import java.math.BigDecimal;

public class Instalacion extends OrdenServicio {
    private final int puntos;
    public Instalacion(String codigo, String cliente, String direccion, double horas, Zona zona, int puntos) {
        super(codigo, cliente, direccion, horas, zona);
        Validacion.exigir(puntos > 0, "La cantidad de puntos debe ser un entero positivo.");
        this.puntos = puntos;
    }
    public int getPuntos() { return puntos; }
    public double horasFacturables() { return Math.ceil(Math.max(getHorasEstimadas(), puntos * 0.75)); }
    @Override public BigDecimal calcularCosto() { return tarifaBase().add(BigDecimal.valueOf(horasFacturables()).multiply(new BigDecimal("90"))); }
    @Override public int diasAtencion() { return getZona() == Zona.METROPOLITANA ? 3 : 5; }
    @Override public int maxVisitas() { return 3; }
    @Override public String categoria() { return "Instalación"; }
}
