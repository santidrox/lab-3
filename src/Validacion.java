import java.math.BigDecimal;

public final class Validacion {
    private Validacion() {}
    public static String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) throw new IllegalArgumentException(campo + " no puede estar vacío.");
        return valor.trim();
    }
    public static double positivo(double valor, String campo) {
        if (!Double.isFinite(valor) || valor <= 0) throw new IllegalArgumentException(campo + " debe ser positivo y finito.");
        return valor;
    }
    public static BigDecimal dinero(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) throw new IllegalArgumentException(campo + " debe ser positivo.");
        return valor;
    }
    public static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new IllegalArgumentException(mensaje);
    }
}
