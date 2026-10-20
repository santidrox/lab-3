import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

public class Pruebas {
    private static final List<String> evidencia = new ArrayList<>();
    private static int numero;
    private static void verificar(String datos, String esperado, String obtenido) {
        numero++;
        boolean ok = esperado.equals(obtenido);
        evidencia.add(numero + "\t" + datos + "\t" + esperado + "\t" + obtenido + "\t" + (ok ? "PASA" : "FALLA"));
        if (!ok) throw new AssertionError(datos + ": esperado " + esperado + ", obtenido " + obtenido);
    }
    private static void valor(String datos, Object esperado, Object obtenido) { verificar(datos, String.valueOf(esperado), String.valueOf(obtenido)); }
    private static void costo(String datos, String esperado, OrdenServicio orden, int dias) {
        valor(datos + "; costo", esperado, Sistema.formato(orden.calcularCosto()));
        valor(datos + "; plazo", dias, orden.diasAtencion());
    }
    private static void rechaza(String datos, Runnable accion) {
        String resultado = "ACEPTADA";
        try { accion.run(); } catch (IllegalArgumentException e) { resultado = "RECHAZADA"; }
        valor(datos, "RECHAZADA", resultado);
    }
    private static String foto(Sistema s) {
        StringBuilder b = new StringBuilder(s.reporte());
        s.getOrdenes().values().forEach(o -> b.append(o.toString()));
        s.getCuadrillas().values().forEach(c -> b.append(c.toString()));
        s.getJornadas().values().forEach(j -> b.append(j.toString()).append(j.getOrdenes()).append(j.getResultados()));
        return b.toString();
    }
    private static void rechazoIntacto(String datos, Sistema s, Runnable accion) {
        String antes = foto(s);
        rechaza(datos, accion);
        valor(datos + "; datos e ingresos intactos", true, antes.equals(foto(s)));
    }
    public static void main(String[] args) throws Exception {
        Sistema s = new Sistema(); s.cargarDatosIniciales();
        valor("Inicio; órdenes", 8, s.getOrdenes().size());
        valor("Inicio; cuadrillas", 3, s.getCuadrillas().size());
        valor("Inicio; pendientes", true, s.getOrdenes().values().stream().allMatch(o -> o.getEstado() == EstadoOrden.PENDIENTE));
        valor("Inicio; ingresos", "Q0.00", Sistema.formato(s.totalIngresos()));
        costo("D01; metropolitana, 1 h", "Q150.00", s.orden("D01"), 1);
        costo("D02; periférica, 1.5 h", "Q225.00", s.orden("D02"), 2);
        costo("I01; metro, 2.5 h, 5 puntos", "Q460.00", s.orden("I01"), 3);
        costo("I02; periférica, 1.2 h, 1 punto", "Q355.00", s.orden("I02"), 5);
        costo("E01; metro, 2.5 h, 5 puntos, Q2000", "Q680.00", s.orden("E01"), 5);
        costo("E02; periférica, 2 h, 2 puntos, Q1000", "Q525.00", s.orden("E02"), 7);
        costo("A01; metro, 1.5 h, 9000 BTU", "Q345.00", s.orden("A01"), 2);
        costo("A02; metro, 2 h, 60000 BTU", "Q400.00", s.orden("A02"), 2);
        costo("Instalación; horas dominan, 2.1 h, 1 punto", "Q370.00", new Instalacion("X", "C", "D", 2.1, Zona.METROPOLITANA, 1), 3);
        costo("Instalación; hora exacta, 3 h, 4 puntos", "Q370.00", new Instalacion("X", "C", "D", 3, Zona.METROPOLITANA, 4), 3);
        String antes = foto(s); s.cotizar(new Diagnostico("COT", "C", "D", 1, Zona.METROPOLITANA));
        valor("Cotización; datos e ingresos intactos", true, antes.equals(foto(s)));
        s.registrarOrden(new Diagnostico("N01", "C", "D", 1, Zona.METROPOLITANA));
        valor("Registro N01; cobro único", "Q150.00", Sistema.formato(s.totalRegistro()));
        rechazoIntacto("Registro duplicado N01", s, () -> s.registrarOrden(new Diagnostico("N01", "C", "D", 1, Zona.METROPOLITANA)));
        rechazoIntacto("Código vacío", s, () -> s.registrarOrden(new Diagnostico("  ", "C", "D", 1, Zona.METROPOLITANA)));
        rechazoIntacto("Diagnóstico de 1.6 h", s, () -> s.registrarOrden(new Diagnostico("X", "C", "D", 1.6, Zona.METROPOLITANA)));
        rechazoIntacto("Horas cero", s, () -> s.registrarOrden(new Diagnostico("X", "C", "D", 0, Zona.METROPOLITANA)));
        rechazoIntacto("Horas negativas", s, () -> s.registrarOrden(new Instalacion("X", "C", "D", -1, Zona.METROPOLITANA, 1)));
        rechazoIntacto("Horas NaN", s, () -> s.registrarOrden(new Instalacion("X", "C", "D", Double.NaN, Zona.METROPOLITANA, 1)));
        rechazoIntacto("Horas infinitas", s, () -> s.registrarOrden(new Instalacion("X", "C", "D", Double.POSITIVE_INFINITY, Zona.METROPOLITANA, 1)));
        rechazoIntacto("Puntos cero", s, () -> s.registrarOrden(new Instalacion("X", "C", "D", 1, Zona.METROPOLITANA, 0)));
        rechazoIntacto("Materiales cero", s, () -> s.registrarOrden(new InstalacionElectricaCertificada("X", "C", "D", 1, Zona.METROPOLITANA, 1, BigDecimal.ZERO)));
        rechazoIntacto("BTU menor a 9000", s, () -> s.registrarOrden(new MantenimientoAire("X", "C", "D", 1, Zona.METROPOLITANA, 8999)));
        rechazoIntacto("BTU mayor a 60000", s, () -> s.registrarOrden(new MantenimientoAire("X", "C", "D", 1, Zona.METROPOLITANA, 60001)));
        rechazoIntacto("Aire periférico", s, () -> s.registrarOrden(new MantenimientoAire("X", "C", "D", 1, Zona.PERIFERICA, 9000)));
        rechazoIntacto("Cuadrilla repetida", s, () -> s.registrarCuadrilla(new Cuadrilla("C01", 8, true)));
        rechazoIntacto("Capacidad cero", s, () -> s.registrarCuadrilla(new Cuadrilla("C04", 0, false)));
        s.crearJornada("J1", "C02");
        rechazoIntacto("Segunda jornada abierta C02", s, () -> s.crearJornada("J2", "C02"));
        rechazoIntacto("Iniciar jornada vacía", s, () -> s.iniciar("J1"));
        rechazoIntacto("A01 en cuadrilla sin certificación", s, () -> s.asignar("J1", "A01"));
        s.asignar("J1", "I01");
        valor("Capacidad usa 2.5 h estimadas y no 4 facturables", "3.5", s.jornada("J1").capacidadDisponible().stripTrailingZeros().toPlainString());
        s.crearJornada("J3", "C01");
        rechazoIntacto("Misma orden en otra jornada", s, () -> s.asignar("J3", "I01"));
        s.asignar("J1", "E01");
        rechazoIntacto("Exceder capacidad 6 h; suma 6.5", s, () -> s.asignar("J1", "D02"));
        s.asignar("J1", "D01");
        valor("Capacidad exacta permitida", "0", s.jornada("J1").capacidadDisponible().stripTrailingZeros().toPlainString());
        BigDecimal ingreso = s.totalIngresos(); s.retirar("J1", "D01");
        valor("Retiro devuelve a pendiente", EstadoOrden.PENDIENTE, s.orden("D01").getEstado());
        valor("Retiro no cobra", ingreso, s.totalIngresos());
        rechazoIntacto("Resultado antes de iniciar", s, () -> s.registrarResultado("J1", "I01", true));
        rechazoIntacto("Retiro desde jornada incorrecta", s, () -> s.retirar("J3", "I01"));
        s.iniciar("J1");
        valor("Inicio primera visita; sin recargo", "Q0.00", Sistema.formato(s.totalVisitas()));
        valor("Inicio pasa a atención", EstadoOrden.EN_ATENCION, s.orden("I01").getEstado());
        rechazoIntacto("Inicio repetido", s, () -> s.iniciar("J1"));
        rechazoIntacto("Retiro después de iniciar", s, () -> s.retirar("J1", "I01"));
        rechazoIntacto("Asignación después de iniciar", s, () -> s.asignar("J1", "D01"));
        rechazoIntacto("Resultado de orden ajena", s, () -> s.registrarResultado("J1", "D01", false));
        s.registrarResultado("J1", "I01", true);
        valor("Jornada espera todos los resultados", EstadoJornada.EN_CURSO, s.jornada("J1").getEstado());
        rechazoIntacto("Resultado repetido", s, () -> s.registrarResultado("J1", "I01", false));
        s.registrarResultado("J1", "E01", false);
        valor("Último resultado cierra jornada", EstadoJornada.CERRADA, s.jornada("J1").getEstado());
        valor("Fallo con visitas disponibles", EstadoOrden.PENDIENTE, s.orden("E01").getEstado());
        rechazoIntacto("Asignar orden completada", s, () -> s.asignar("J3", "I01"));
        s.crearJornada("J4", "C02"); s.asignar("J4", "E01");
        valor("Reasignar todavía no cobra", "Q0.00", Sistema.formato(s.totalVisitas()));
        s.retirar("J4", "E01"); s.asignar("J4", "E01"); s.iniciar("J4");
        valor("Segunda visita cobra al iniciar", "Q40.00", Sistema.formato(s.totalVisitas()));
        s.registrarResultado("J4", "E01", false);
        valor("Certificada; dos fallos cancelan", EstadoOrden.CANCELADA, s.orden("E01").getEstado());
        rechazoIntacto("Reasignar cancelada", s, () -> s.asignar("J3", "E01"));
        Sistema visitas = new Sistema(); visitas.cargarDatosIniciales();
        String[] codigos = {"D01", "I01", "E01", "A01"};
        int[] limites = {3, 3, 2, 1};
        int indice = 0;
        for (int k = 0; k < codigos.length; k++) {
            for (int n = 1; n <= limites[k]; n++) {
                String j = "V" + (++indice);
                visitas.crearJornada(j, "C01"); visitas.asignar(j, codigos[k]); visitas.iniciar(j);
                visitas.registrarResultado(j, codigos[k], false);
                valor(codigos[k] + "; fallo " + n + "; estado", n == limites[k] ? EstadoOrden.CANCELADA : EstadoOrden.PENDIENTE, visitas.orden(codigos[k]).getEstado());
                valor(codigos[k] + "; visitas realizadas", n, visitas.orden(codigos[k]).getVisitas());
                valor(j + "; cerrada", EstadoJornada.CERRADA, visitas.jornada(j).getEstado());
            }
        }
        valor("Todas las categorías; recargos 2+2+1+0", "Q200.00", Sistema.formato(visitas.totalVisitas()));
        valor("Datos iniciales no generan ingreso de registro", "Q0.00", Sistema.formato(visitas.totalRegistro()));
        valor("Reporte; ingreso por diagnóstico", true, visitas.reporte().contains("Ingreso registro: Q0.00 | visitas: Q80.00 | total: Q80.00"));
        rechazoIntacto("Jornada repetida", s, () -> s.crearJornada("J1", "C03"));
        rechazoIntacto("Cuadrilla inexistente", s, () -> s.crearJornada("J9", "NO"));
        rechazoIntacto("Orden inexistente", s, () -> s.asignar("J3", "NO"));
        rechazoIntacto("Jornada inexistente", s, () -> s.iniciar("NO"));
        Sistema dec = new Sistema(); dec.registrarCuadrilla(new Cuadrilla("C", 0.3, false));
        dec.registrarOrden(new Instalacion("a", "C", "D", 0.1, Zona.METROPOLITANA, 1));
        dec.registrarOrden(new Instalacion("b", "C", "D", 0.2, Zona.METROPOLITANA, 1));
        dec.crearJornada("J", "C"); dec.asignar("J", "a"); dec.asignar("J", "b");
        valor("Capacidad decimal; 0.1 + 0.2 = 0.3", "0", dec.jornada("J").capacidadDisponible().stripTrailingZeros().toPlainString());
        evidencia.add("TOTAL\t" + numero + " comprobaciones aprobadas");
        Files.write(Path.of("docs", "resultados-pruebas.tsv"), evidencia, StandardCharsets.UTF_8);
        for (String linea : evidencia) System.out.println(linea);
    }
}
