package pe.edu.upeu.objetosperdidos.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.objetosperdidos.entity.*;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.repository.CandidatosCoincidenciaRepository;

@ExtendWith(MockitoExtension.class)
class SugerenciasCoincidenciaServiceTest {
    @Mock CandidatosCoincidenciaRepository repositorio;
    SugerenciasCoincidenciaService servicio;

    @BeforeEach void configurar() {
        servicio = new SugerenciasCoincidenciaService(
                repositorio, new ComparadorObjetosService());
    }

    private Objeto objeto(String nombre, Long idCategoria, String nombreCategoria,
                          String marca, String color) {
        Categoria c = new Categoria();
        c.setIdCategoria(idCategoria);
        c.setNombre(nombreCategoria);
        Objeto o = new Objeto();
        o.setCategoria(c);
        o.setNombre(nombre);
        o.setMarca(marca);
        o.setColor(color);
        return o;
    }

    private ReportePerdida perdida() {
        ReportePerdida p = new ReportePerdida();
        p.setIdReportePerdida(21L);
        p.setEstado(EstadoReportePerdida.REPORTADO);
        p.setObjeto(objeto("Audifonos Bluetooth", 1L, "Electronica", "JBL", "Negro"));
        p.setFechaPerdida(LocalDate.of(2026, 11, 19));
        return p;
    }

    private ReporteHallazgos hallazgo(Long id, String nombre, String marca, String color) {
        ReporteHallazgos h = new ReporteHallazgos();
        h.setIdHallazgo(id);
        h.setEstado(EstadoReporteHallazgo.APROBADO);
        h.setObjeto(objeto(nombre, 1L, "Electronica", marca, color));
        h.setFechaReporte(LocalDateTime.of(2026, 11, 20, 10, 0));
        return h;
    }

    private ObjetosCustodiados custodia(Long id, ReporteHallazgos hallazgo, Objeto objeto) {
        ObjetosCustodiados c = new ObjetosCustodiados();
        c.setIdCustodia(id);
        c.setHallazgo(hallazgo);
        c.setObjeto(objeto);
        c.setFechaRegistro(LocalDateTime.of(2026, 11, 20, 11, 0));
        return c;
    }

    @Test void noDevuelveReportesDeOtraPersona() {
        when(repositorio.buscarPerdidaPropia(21L, 2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> servicio.sugerir(21L, 2L));
        verify(repositorio, never()).custodiasDisponibles();
    }

    @Test void noSugiereSiPerdidaEstaRecuperada() {
        var p = perdida(); p.setEstado(EstadoReportePerdida.RECUPERADO);
        when(repositorio.buscarPerdidaPropia(21L, 2L)).thenReturn(Optional.of(p));
        var r = servicio.sugerir(21L, 2L);
        assertTrue(r.sugerencias().isEmpty());
        verify(repositorio, never()).hallazgosSinCustodia();
    }

    @Test void incorporaCustodiaDirecta() {
        var p = perdida();
        var c = custodia(23L, null, objeto("Auriculares Bluetooth", 1L,
                "Electronica", "JBL", "Negro"));
        when(repositorio.buscarPerdidaPropia(21L, 2L)).thenReturn(Optional.of(p));
        when(repositorio.custodiasDisponibles()).thenReturn(List.of(c));
        var r = servicio.sugerir(21L, 2L);
        assertEquals(1, r.sugerencias().size());
        assertEquals("CUSTODIA", r.sugerencias().get(0).origen());
    }

    @Test void evitaDuplicarHallazgoCuandoYaEstaCustodiado() {
        var p = perdida();
        var h = hallazgo(40L, "Auriculares Bluetooth", "JBL", "Negro");
        var c = custodia(50L, h, h.getObjeto());
        when(repositorio.buscarPerdidaPropia(21L, 2L)).thenReturn(Optional.of(p));
        when(repositorio.custodiasDisponibles()).thenReturn(List.of(c));
        when(repositorio.hallazgosSinCustodia()).thenReturn(List.of(h));
        var r = servicio.sugerir(21L, 2L);
        assertEquals(1, r.sugerencias().size());
        assertEquals(Long.valueOf(50L), r.sugerencias().get(0).idReferencia());
    }

    @Test void protegeCaracteristicasPrivadasEnRespuesta() {
        var p = perdida();
        var h = hallazgo(40L, "Auriculares Bluetooth", "JBL", "Negro");
        when(repositorio.buscarPerdidaPropia(21L, 2L)).thenReturn(Optional.of(p));
        when(repositorio.hallazgosSinCustodia()).thenReturn(List.of(h));
        var r = servicio.sugerir(21L, 2L);
        assertEquals(1, r.sugerencias().size());
        assertEquals("Electronica", r.sugerencias().get(0).categoria());
        assertFalse(r.sugerencias().get(0).toString().contains("JBL"));
        assertFalse(r.sugerencias().get(0).toString().contains("Negro"));
    }

    @Test void limitaResultadosACinco() {
        var p = perdida();
        var hallazgos = java.util.stream.LongStream.rangeClosed(1, 9)
                .mapToObj(id -> hallazgo(id, "Audifonos Bluetooth", "JBL", "Negro"))
                .toList();
        when(repositorio.buscarPerdidaPropia(21L, 2L)).thenReturn(Optional.of(p));
        when(repositorio.hallazgosSinCustodia()).thenReturn(hallazgos);
        var r = servicio.sugerir(21L, 2L);
        assertEquals(5, r.sugerencias().size());
    }
}
