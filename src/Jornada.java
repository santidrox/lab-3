import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public class Jornada {
    private final String codigo;
    private final Cuadrilla cuadrilla;
    private final List<OrdenServicio> ordenes = new ArrayList<>();
    private final Map<String, Boolean> resultados = new LinkedHashMap<>();
    private EstadoJornada estado = EstadoJornada.PREPARACION;
    public Jornada(String codigo, Cuadrilla cuadrilla) {
        this.codigo = Validacion.texto(codigo, "Código de jornada");
        Validacion.exigir(cuadrilla != null, "Cuadrilla requerida.");
        this.cuadrilla = cuadrilla;
    }
    public String getCodigo() { return codigo; }
    public Cuadrilla getCuadrilla() { return cuadrilla; }
    public EstadoJornada getEstado() { return estado; }
    public List<OrdenServicio> getOrdenes() { return Collections.unmodifiableList(ordenes); }
    public Map<String, Boolean> getResultados() { return Collections.unmodifiableMap(resultados); }
    public BigDecimal horasAsignadas() {
        BigDecimal suma = BigDecimal.ZERO;
        for (OrdenServicio o : ordenes) suma = suma.add(BigDecimal.valueOf(o.getHorasEstimadas()));
        return suma;
    }
    public BigDecimal capacidadDisponible() { return BigDecimal.valueOf(cuadrilla.getCapacidad()).subtract(horasAsignadas()); }
    void asignar(OrdenServicio orden) {
        Validacion.exigir(estado == EstadoJornada.PREPARACION, "Solo se asigna en preparación.");
        Validacion.exigir(orden.getEstado() == EstadoOrden.PENDIENTE && orden.getVisitas() < orden.maxVisitas(), "La orden no está disponible.");
        Validacion.exigir(orden.compatible(cuadrilla), "La orden requiere una cuadrilla certificada en refrigeración.");
        Validacion.exigir(BigDecimal.valueOf(orden.getHorasEstimadas()).compareTo(capacidadDisponible()) <= 0, "La asignación excede la capacidad.");
        orden.asignar();
        ordenes.add(orden);
    }
    void retirar(OrdenServicio orden) {
        Validacion.exigir(estado == EstadoJornada.PREPARACION && ordenes.contains(orden), "Solo se retiran órdenes de esta jornada en preparación.");
        orden.retirar();
        ordenes.remove(orden);
    }
    Map<String, BigDecimal> iniciar() {
        Validacion.exigir(estado == EstadoJornada.PREPARACION && !ordenes.isEmpty(), "La jornada debe estar en preparación y tener órdenes.");
        for (OrdenServicio o : ordenes) Validacion.exigir(o.getEstado() == EstadoOrden.ASIGNADA && o.getVisitas() < o.maxVisitas(), "Una orden no permite iniciar.");
        Map<String, BigDecimal> cargos = new LinkedHashMap<>();
        for (OrdenServicio o : ordenes) cargos.merge(o.categoria(), o.iniciarVisita(), BigDecimal::add);
        estado = EstadoJornada.EN_CURSO;
        return cargos;
    }
    void registrarResultado(OrdenServicio orden, boolean completada) {
        Validacion.exigir(estado == EstadoJornada.EN_CURSO && ordenes.contains(orden) && !resultados.containsKey(orden.getCodigo()), "Resultado no permitido o ya registrado.");
        orden.finalizarVisita(completada);
        resultados.put(orden.getCodigo(), completada);
        if (resultados.size() == ordenes.size()) estado = EstadoJornada.CERRADA;
    }
    @Override public String toString() { return codigo + " | cuadrilla " + cuadrilla.getCodigo() + " | " + estado + " | horas " + horasAsignadas() + " | disponibles " + capacidadDisponible(); }
}
