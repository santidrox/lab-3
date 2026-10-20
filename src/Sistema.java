import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Sistema {
    private final Map<String, OrdenServicio> ordenes = new LinkedHashMap<>();
    private final Map<String, Cuadrilla> cuadrillas = new LinkedHashMap<>();
    private final Map<String, Jornada> jornadas = new LinkedHashMap<>();
    private final Map<String, BigDecimal> ingresosRegistro = new LinkedHashMap<>();
    private final Map<String, BigDecimal> ingresosVisitas = new LinkedHashMap<>();
    public Sistema() {}
    public static String formato(BigDecimal monto) { return "Q" + monto.setScale(2, RoundingMode.HALF_UP).toPlainString(); }
    public String cotizar(OrdenServicio orden) { return orden.categoria() + ": " + formato(orden.calcularCosto()) + " | " + orden.diasAtencion() + " días hábiles"; }
    public void registrarOrden(OrdenServicio orden) { agregarOrden(orden, true); }
    private void agregarOrden(OrdenServicio orden, boolean cobrar) {
        Validacion.exigir(orden != null, "Orden requerida.");
        Validacion.exigir(!ordenes.containsKey(orden.getCodigo()), "Código de orden repetido.");
        Validacion.exigir(orden.getEstado() == EstadoOrden.PENDIENTE && orden.getVisitas() == 0, "Solo se registra una orden nueva.");
        BigDecimal monto = orden.calcularCosto().setScale(2, RoundingMode.HALF_UP);
        ordenes.put(orden.getCodigo(), orden);
        if (cobrar) ingresosRegistro.merge(orden.categoria(), monto, BigDecimal::add);
    }
    public void registrarCuadrilla(Cuadrilla cuadrilla) {
        Validacion.exigir(cuadrilla != null, "Cuadrilla requerida.");
        Validacion.exigir(!cuadrillas.containsKey(cuadrilla.getCodigo()), "Código de cuadrilla repetido.");
        cuadrillas.put(cuadrilla.getCodigo(), cuadrilla);
    }
    public void crearJornada(String codigo, String codigoCuadrilla) {
        codigo = Validacion.texto(codigo, "Código de jornada");
        Validacion.exigir(!jornadas.containsKey(codigo), "Código de jornada repetido.");
        Cuadrilla c = cuadrilla(codigoCuadrilla);
        for (Jornada j : jornadas.values()) Validacion.exigir(j.getCuadrilla() != c || j.getEstado() == EstadoJornada.CERRADA, "La cuadrilla ya tiene una jornada sin cerrar.");
        jornadas.put(codigo, new Jornada(codigo, c));
    }
    public void asignar(String jornada, String orden) { jornada(jornada).asignar(orden(orden)); }
    public void retirar(String jornada, String orden) { jornada(jornada).retirar(orden(orden)); }
    public void iniciar(String codigo) {
        Map<String, BigDecimal> cargos = jornada(codigo).iniciar();
        for (Map.Entry<String, BigDecimal> e : cargos.entrySet()) ingresosVisitas.merge(e.getKey(), e.getValue(), BigDecimal::add);
    }
    public void registrarResultado(String jornada, String orden, boolean completada) { jornada(jornada).registrarResultado(orden(orden), completada); }
    public OrdenServicio orden(String codigo) { return buscar(ordenes, codigo, "Orden"); }
    public Cuadrilla cuadrilla(String codigo) { return buscar(cuadrillas, codigo, "Cuadrilla"); }
    public Jornada jornada(String codigo) { return buscar(jornadas, codigo, "Jornada"); }
    private <T> T buscar(Map<String, T> mapa, String codigo, String tipo) {
        T objeto = mapa.get(Validacion.texto(codigo, "Código"));
        Validacion.exigir(objeto != null, tipo + " no encontrada.");
        return objeto;
    }
    public Map<String, OrdenServicio> getOrdenes() { return Collections.unmodifiableMap(ordenes); }
    public Map<String, Cuadrilla> getCuadrillas() { return Collections.unmodifiableMap(cuadrillas); }
    public Map<String, Jornada> getJornadas() { return Collections.unmodifiableMap(jornadas); }
    public BigDecimal totalRegistro() { return sumar(ingresosRegistro); }
    public BigDecimal totalVisitas() { return sumar(ingresosVisitas); }
    public BigDecimal totalIngresos() { return totalRegistro().add(totalVisitas()); }
    private BigDecimal sumar(Map<String, BigDecimal> mapa) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal monto : mapa.values()) total = total.add(monto);
        return total;
    }
    public String reporte() {
        StringBuilder r = new StringBuilder("ÓRDENES POR CATEGORÍA Y ESTADO\n");
        Map<String, int[]> conteos = new LinkedHashMap<>();
        for (OrdenServicio o : ordenes.values()) conteos.computeIfAbsent(o.categoria(), k -> new int[EstadoOrden.values().length])[o.getEstado().ordinal()]++;
        for (String categoria : conteos.keySet()) {
            r.append(categoria).append(": ");
            for (EstadoOrden e : EstadoOrden.values()) r.append(e).append("=").append(conteos.get(categoria)[e.ordinal()]).append(" ");
            r.append("\n  Ingreso registro: ").append(formato(ingresosRegistro.getOrDefault(categoria, BigDecimal.ZERO)))
             .append(" | visitas: ").append(formato(ingresosVisitas.getOrDefault(categoria, BigDecimal.ZERO)))
             .append(" | total: ").append(formato(ingresosRegistro.getOrDefault(categoria, BigDecimal.ZERO).add(ingresosVisitas.getOrDefault(categoria, BigDecimal.ZERO)))).append("\n");
        }
        r.append("REGISTRO: ").append(formato(totalRegistro())).append(" | VISITAS: ").append(formato(totalVisitas())).append(" | TOTAL: ").append(formato(totalIngresos())).append("\nJORNADAS SIN CERRAR\n");
        boolean alguna = false;
        for (Jornada j : jornadas.values()) if (j.getEstado() != EstadoJornada.CERRADA) { r.append(j).append("\n"); alguna = true; }
        if (!alguna) r.append("Ninguna.\n");
        return r.toString();
    }
    public void cargarDatosIniciales() {
        Validacion.exigir(ordenes.isEmpty() && cuadrillas.isEmpty() && jornadas.isEmpty(), "Los datos iniciales solo se cargan en un sistema vacío.");
        agregarOrden(new Diagnostico("D01", "Ana", "Zona 1", 1, Zona.METROPOLITANA), false);
        agregarOrden(new Diagnostico("D02", "Luis", "Mixco", 1.5, Zona.PERIFERICA), false);
        agregarOrden(new Instalacion("I01", "Marta", "Zona 10", 2.5, Zona.METROPOLITANA, 5), false);
        agregarOrden(new Instalacion("I02", "José", "Villa Nueva", 1.2, Zona.PERIFERICA, 1), false);
        agregarOrden(new InstalacionElectricaCertificada("E01", "Sofía", "Zona 15", 2.5, Zona.METROPOLITANA, 5, new BigDecimal("2000")), false);
        agregarOrden(new InstalacionElectricaCertificada("E02", "Pedro", "San José Pinula", 2, Zona.PERIFERICA, 2, new BigDecimal("1000")), false);
        agregarOrden(new MantenimientoAire("A01", "Carla", "Zona 4", 1.5, Zona.METROPOLITANA, 9000), false);
        agregarOrden(new MantenimientoAire("A02", "Mario", "Zona 14", 2, Zona.METROPOLITANA, 60000), false);
        registrarCuadrilla(new Cuadrilla("C01", 8, true));
        registrarCuadrilla(new Cuadrilla("C02", 6, false));
        registrarCuadrilla(new Cuadrilla("C03", 4, true));
    }
}
