package pe.edu.upeu.objetosperdidos;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.edu.upeu.objetosperdidos.controller.ReporteEstadisticoController;
import pe.edu.upeu.objetosperdidos.dto.ConteoEstadoDTO;
import pe.edu.upeu.objetosperdidos.dto.ResumenReclamosDTO;
import pe.edu.upeu.objetosperdidos.services.IReporteEstadisticoService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReporteEstadisticoControllerTests {

    private IReporteEstadisticoService service;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        service = mock(IReporteEstadisticoService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReporteEstadisticoController(service))
                .build();
    }

    @Test
    void devuelvePerdidasPorEstado() throws Exception {
        when(service.perdidasPorEstado())
                .thenReturn(List.of(new ConteoEstadoDTO("REPORTADO", 3L)));

        mockMvc.perform(get("/api/v1/reportes/perdidas/por-estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("REPORTADO"))
                .andExpect(jsonPath("$[0].total").value(3));

        verify(service).perdidasPorEstado();
    }

    @Test
    void devuelveResumenDeReclamos() throws Exception {
        when(service.resumenReclamosDesdeProcedimiento())
                .thenReturn(new ResumenReclamosDTO(5L, 2L, 3L));

        mockMvc.perform(get("/api/v1/reportes/reclamos/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.sinRevisar").value(2))
                .andExpect(jsonPath("$.revisados").value(3));

        verify(service).resumenReclamosDesdeProcedimiento();
    }

    @Test
    void exponeLasRutasEstadisticasActuales() throws Exception {
        when(service.perdidasPorEstado()).thenReturn(List.of());
        when(service.hallazgosPorEstado()).thenReturn(List.of());
        when(service.custodiasPorSituacion()).thenReturn(List.of());
        when(service.entregasPorCategoria()).thenReturn(List.of());
        when(service.perdidasPorMes()).thenReturn(List.of());
        when(service.entregasPorMes()).thenReturn(List.of());
        when(service.reclamosPorEstado()).thenReturn(List.of());
        when(service.seguimientoDesdeVista()).thenReturn(List.of());
        when(service.resumenReclamosDesdeProcedimiento())
                .thenReturn(new ResumenReclamosDTO(0L, 0L, 0L));

        String[] rutas = {
            "/api/v1/reportes/perdidas/por-estado",
            "/api/v1/reportes/hallazgos/por-estado",
            "/api/v1/reportes/custodias/por-situacion",
            "/api/v1/reportes/entregas/por-categoria",
            "/api/v1/reportes/perdidas/por-mes",
            "/api/v1/reportes/entregas/por-mes",
            "/api/v1/reportes/reclamos/por-estado",
            "/api/v1/reportes/reclamos/resumen",
            "/api/v1/reportes/reclamos/seguimiento"
        };

        for (String ruta : rutas) {
            mockMvc.perform(get(ruta))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void pasaLaUbicacionCorrectaAlServicio() throws Exception {
        when(service.pendientesDeRecepcion(7L)).thenReturn(List.of());
        when(service.custodiasDisponibles(7L)).thenReturn(List.of());

        mockMvc.perform(get(
                "/api/v1/reportes/ubicaciones/7/pendientes-recepcion"))
                .andExpect(status().isOk());
        mockMvc.perform(get(
                "/api/v1/reportes/ubicaciones/7/custodias-disponibles"))
                .andExpect(status().isOk());

        verify(service).pendientesDeRecepcion(7L);
        verify(service).custodiasDisponibles(7L);
    }
}
