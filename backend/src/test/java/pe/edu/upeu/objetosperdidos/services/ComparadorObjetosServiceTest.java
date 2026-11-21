package pe.edu.upeu.objetosperdidos.services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.objetosperdidos.entity.Categoria;
import pe.edu.upeu.objetosperdidos.entity.Objeto;
import pe.edu.upeu.objetosperdidos.entity.ObjetosCustodiados;
import pe.edu.upeu.objetosperdidos.entity.ReporteHallazgos;
import pe.edu.upeu.objetosperdidos.entity.ReportePerdida;
import pe.edu.upeu.objetosperdidos.entity.Ubicacion;
import pe.edu.upeu.objetosperdidos.services.ComparadorObjetosService.ResultadoComparacion;

class ComparadorObjetosServiceTest {
    private final ComparadorObjetosService comparador = new ComparadorObjetosService();

    @Test
    void noDependeDeLaDescripcion() {
        Objeto a = objeto(1L, "Guitarra acustica", "Yamaha", "Negro", "Pegatina en la parte trasera");
        Objeto b = objeto(1L, "Guitarra acustica", "Yamaha", "Negro", "No se sabe nada mas");
        ResultadoComparacion r = comparador.comparar(a, b);
        assertEquals(100, r.similitud());
        assertEquals(85, r.cobertura()); // Sin fecha ni ubicacion no llegamos a 100 de cobertura.
    }

    @Test
    void reconoceSinonimosTildesYColores() {
        Objeto a = objeto(1L, "Audífonos inalámbricos", "Sony", "Negro", "");
        Objeto b = objeto(1L, "Auriculares Bluetooth", "SONY", "negra", "");
        assertEquals(100, comparador.comparar(a, b).similitud());
    }

    @Test
    void informacionAusenteNoEquivaleAContradiccion() {
        Objeto a = objeto(1L, "Mochila", "Nike", "Negro", "");
        Objeto b = objeto(1L, "Mochila", "Sin marca", null, "");
        ResultadoComparacion r = comparador.comparar(a, b);
        assertEquals(100, r.similitud());
        assertEquals(55, r.cobertura());
        assertTrue(r.prioridad() < 100);
    }

    @Test
    void marcaDiferenteReduceSimilitud() {
        Objeto a = objeto(1L, "Mochila", "Nike", "Negro", "");
        Objeto b = objeto(1L, "Mochila", "Adidas", "Negro", "");
        assertTrue(comparador.comparar(a, b).similitud() < 100);
    }

    @Test
    void lugarDiferenteNoDescartaUnaCoincidencia() {
        ReportePerdida p = perdida(objeto(1L, "Guitarra", "Yamaha", "Negro", ""),
                LocalDate.of(2026, 11, 5), ubicacion(1L));
        ReporteHallazgos h = hallazgo(objeto(1L, "Guitarra", "Yamaha", "Negro", ""),
                LocalDateTime.of(2026, 11, 8, 10, 0), ubicacion(2L));
        ResultadoComparacion r = comparador.comparar(p, h);
        assertTrue(r.prioridad() >= 80);
        assertEquals("DISTINTO", r.criterios().get(5).evaluacion());
    }

    @Test
    void fechaLejanaNoDescartaObjeto() {
        ReportePerdida p = perdida(objeto(1L, "Guitarra", "Yamaha", "Negro", ""),
                LocalDate.of(2026, 7, 1), ubicacion(1L));
        ReporteHallazgos h = hallazgo(objeto(1L, "Guitarra", "Yamaha", "Negro", ""),
                LocalDateTime.of(2026, 11, 10, 10, 0), ubicacion(1L));
        assertTrue(comparador.comparar(p, h).similitud() >= 90);
    }

    @Test
    void perdidaPosteriorARegistroDelHallazgoReduceCompatibilidadTemporal() {
        ReportePerdida p = perdida(objeto(1L, "Guitarra", "Yamaha", "Negro", ""),
                LocalDate.of(2026, 11, 20), ubicacion(1L));
        ReporteHallazgos h = hallazgo(objeto(1L, "Guitarra", "Yamaha", "Negro", ""),
                LocalDateTime.of(2026, 11, 10, 10, 0), ubicacion(1L));
        assertEquals("DISTINTO", comparador.comparar(p, h).criterios().get(4).evaluacion());
    }

    @Test
    void custodiaDirectaNoUsaUbicacionActualComoOrigen() {
        ReportePerdida p = perdida(objeto(1L, "Bufanda", null, "Rojo", ""),
                LocalDate.of(2026, 11, 15), ubicacion(2L));
        ObjetosCustodiados c = new ObjetosCustodiados();
        c.setObjeto(objeto(1L, "Bufanda", null, "Rojo", ""));
        c.setFechaRegistro(LocalDateTime.of(2026, 11, 18, 10, 0));
        c.setUbicacionActual(ubicacion(2L)); // NO ES lugar de hallazgo.
        ResultadoComparacion r = comparador.comparar(p, c);
        assertEquals("SIN_DATOS", r.criterios().get(5).evaluacion());
        assertEquals(80, r.cobertura()); // nombre 30 + categoria 25 + color 15 + tiempo 10
    }

    @Test
    void custodiaConHallazgoUsaUbicacionDelHallazgoComoOrigen() {
        ReportePerdida p = perdida(objeto(1L, "Mochila", "Nike", "Negro", ""),
                LocalDate.of(2026, 11, 5), ubicacion(1L));
        ReporteHallazgos h = hallazgo(objeto(1L, "Mochila", "Nike", "Negro", ""),
                LocalDateTime.of(2026, 11, 6, 10, 0), ubicacion(1L));
        ObjetosCustodiados c = new ObjetosCustodiados();
        c.setObjeto(h.getObjeto());
        c.setHallazgo(h);
        c.setUbicacionActual(ubicacion(2L));
        assertEquals("COMPATIBLE", comparador.comparar(p, c).criterios().get(5).evaluacion());
    }

    @Test
    void categoriaSolaNoDaPrioridadMaxima() {
        Objeto a = objeto(1L, null, null, null, "");
        Objeto b = objeto(1L, null, null, null, "");
        ResultadoComparacion r = comparador.comparar(a, b);
        assertEquals(100, r.similitud());
        assertEquals(25, r.cobertura());
        assertTrue(r.prioridad() < 70);
    }

    @Test
    void ausenciaTotalDaCero() {
        assertEquals(0, comparador.comparar(new Objeto(), new Objeto()).prioridad());
    }

    @Test
    void requiereObjetosValidos() {
        assertThrows(NullPointerException.class,
                () -> comparador.comparar((Objeto) null, objeto(1L, "Mochila", null, null, "")));
    }

    private Objeto objeto(Long idCategoria, String nombre, String marca,
                           String color, String descripcion) {
        Objeto o = new Objeto();
        if (idCategoria != null) {
            Categoria c = new Categoria();
            c.setIdCategoria(idCategoria);
            o.setCategoria(c);
        }
        o.setNombre(nombre);
        o.setMarca(marca);
        o.setColor(color);
        o.setDescripcion(descripcion);
        return o;
    }

    private Ubicacion ubicacion(Long id) {
        Ubicacion u = new Ubicacion();
        u.setIdUbicacion(id);
        return u;
    }

    private ReportePerdida perdida(Objeto o, LocalDate fecha, Ubicacion u) {
        ReportePerdida p = new ReportePerdida();
        p.setObjeto(o);
        p.setFechaPerdida(fecha);
        p.setUbicacionPerdida(u);
        return p;
    }

    private ReporteHallazgos hallazgo(Objeto o, LocalDateTime fecha, Ubicacion u) {
        ReporteHallazgos h = new ReporteHallazgos();
        h.setObjeto(o);
        h.setFechaReporte(fecha);
        h.setUbicacionEncontrado(u);
        return h;
    }
}
