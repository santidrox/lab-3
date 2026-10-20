import java.math.BigDecimal;
import java.util.Scanner;
import java.util.NoSuchElementException;

public class Main {
    private final Scanner entrada = new Scanner(System.in);
    private final Sistema sistema = new Sistema();
    public Main() { sistema.cargarDatosIniciales(); }
    public static void main(String[] args) { new Main().ejecutar(); }
    public void ejecutar() {
        boolean activo = true;
        while (activo) {
            System.out.println("\nTECNIHOGAR\n1. Cotizar orden\n2. Registrar orden\n3. Registrar cuadrilla\n4. Crear jornada\n5. Asignar orden\n6. Retirar orden\n7. Iniciar jornada\n8. Registrar resultado\n9. Consultar órdenes\n10. Consultar cuadrillas\n11. Reportes\n12. Consultar jornadas y resultados\n0. Salir");
            try {
                int opcion = entero("Opción: ");
                switch (opcion) {
                    case 1: System.out.println(sistema.cotizar(leerOrden())); break;
                    case 2:
                        OrdenServicio o = leerOrden();
                        System.out.println(sistema.cotizar(o));
                        if (siNo("¿Acepta y registra el cobro completo? (s/n): ")) {
                            sistema.registrarOrden(o);
                            System.out.println("Orden registrada y cobro registrado.");
                        } else System.out.println("Cotización descartada.");
                        break;
                    case 3: sistema.registrarCuadrilla(new Cuadrilla(texto("Código: "), decimal("Capacidad en horas: "), siNo("¿Certificada en refrigeración? (s/n): "))); break;
                    case 4: sistema.crearJornada(texto("Código de jornada: "), texto("Código de cuadrilla: ")); break;
                    case 5: sistema.asignar(texto("Jornada: "), texto("Orden: ")); break;
                    case 6: sistema.retirar(texto("Jornada: "), texto("Orden: ")); break;
                    case 7: sistema.iniciar(texto("Jornada: ")); break;
                    case 8: sistema.registrarResultado(texto("Jornada: "), texto("Orden: "), siNo("¿Visita completada? (s/n; n = fallida): ")); break;
                    case 9: sistema.getOrdenes().values().forEach(System.out::println); break;
                    case 10: sistema.getCuadrillas().values().forEach(System.out::println); break;
                    case 11: System.out.println(sistema.reporte()); break;
                    case 12:
                        for (Jornada j : sistema.getJornadas().values()) {
                            System.out.println(j);
                            for (OrdenServicio orden : j.getOrdenes()) {
                                Boolean resultado = j.getResultados().get(orden.getCodigo());
                                System.out.println("  " + orden.getCodigo() + " | resultado en esta jornada: " + (resultado == null ? "sin registrar" : resultado ? "completada" : "fallida"));
                            }
                        }
                        break;
                    case 0: activo = false; break;
                    default: System.out.println("Opción inexistente.");
                }
            } catch (IllegalArgumentException e) { System.out.println("Operación rechazada: " + e.getMessage()); }
              catch (NoSuchElementException e) { System.out.println("Fin de entrada."); activo = false; }
        }
    }
    private String texto(String mensaje) { System.out.print(mensaje); return Validacion.texto(entrada.nextLine(), "Entrada"); }
    private int entero(String mensaje) {
        try { return Integer.parseInt(texto(mensaje)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Ingrese un número entero válido."); }
    }
    private double decimal(String mensaje) {
        try { return Validacion.positivo(Double.parseDouble(texto(mensaje)), "Valor"); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Ingrese un número decimal válido usando punto."); }
    }
    private BigDecimal dinero(String mensaje) {
        try { return Validacion.dinero(new BigDecimal(texto(mensaje)), "Monto"); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Ingrese un monto válido usando punto."); }
    }
    private boolean siNo(String mensaje) {
        String valor = texto(mensaje);
        Validacion.exigir(valor.equalsIgnoreCase("s") || valor.equalsIgnoreCase("n"), "Responda s o n.");
        return valor.equalsIgnoreCase("s");
    }
    private OrdenServicio leerOrden() {
        int tipo = entero("Categoría (1 diagnóstico, 2 instalación, 3 eléctrica certificada, 4 aire): ");
        Validacion.exigir(tipo >= 1 && tipo <= 4, "Categoría inválida.");
        String codigo = texto("Código: "), cliente = texto("Cliente: "), direccion = texto("Dirección: ");
        double horas = decimal("Horas estimadas: ");
        int zona = entero("Zona (1 metropolitana, 2 periférica): ");
        Validacion.exigir(zona == 1 || zona == 2, "Zona inválida.");
        Zona z = zona == 1 ? Zona.METROPOLITANA : Zona.PERIFERICA;
        switch (tipo) {
            case 1: return new Diagnostico(codigo, cliente, direccion, horas, z);
            case 2: return new Instalacion(codigo, cliente, direccion, horas, z, entero("Puntos: "));
            case 3: return new InstalacionElectricaCertificada(codigo, cliente, direccion, horas, z, entero("Puntos: "), dinero("Valor de materiales: "));
            case 4: return new MantenimientoAire(codigo, cliente, direccion, horas, z, entero("Capacidad BTU: "));
            default: throw new IllegalArgumentException("Categoría inválida.");
        }
    }
}
