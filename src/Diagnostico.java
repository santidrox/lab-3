import java.math.BigDecimal;

public class Diagnostico extends OrdenServicio {
    public Diagnostico(String codigo, String cliente, String direccion, double horas, Zona zona) {
        super(codigo, cliente, direccion, horas, zona);
        Validacion.exigir(horas <= 1.5, "El diagnóstico permite como máximo 1.5 horas.");
    }
    @Override public BigDecimal calcularCosto() { return new BigDecimal(getZona() == Zona.METROPOLITANA ? "150" : "225"); }
    @Override public int diasAtencion() { return getZona() == Zona.METROPOLITANA ? 1 : 2; }
    @Override public int maxVisitas() { return 3; }
    @Override public String categoria() { return "Diagnóstico"; }
}
