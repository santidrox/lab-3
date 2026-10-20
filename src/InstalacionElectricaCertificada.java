import java.math.BigDecimal;

public class InstalacionElectricaCertificada extends Instalacion {
    private final BigDecimal valorMateriales;
    public InstalacionElectricaCertificada(String codigo, String cliente, String direccion, double horas, Zona zona, int puntos, BigDecimal materiales) {
        super(codigo, cliente, direccion, horas, zona, puntos);
        valorMateriales = Validacion.dinero(materiales, "Valor de materiales");
    }
    public BigDecimal getValorMateriales() { return valorMateriales; }
    @Override public BigDecimal calcularCosto() { return super.calcularCosto().add(new BigDecimal("120")).add(valorMateriales.multiply(new BigDecimal("0.05"))); }
    @Override public int diasAtencion() { return super.diasAtencion() + 2; }
    @Override public int maxVisitas() { return 2; }
    @Override public String categoria() { return "Instalación eléctrica certificada"; }
}
