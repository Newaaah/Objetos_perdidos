package pe.edu.upeu.objetosperdidos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "security.jwt.secret-key=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
@AutoConfigureMockMvc
class ReporteOracleIntegrationTests {
    @Autowired
    private ReporteOracleRepository reporteRepository;
    @Autowired
    private ProcedimientoOracleRepository procedimientoRepository;
    @Autowired
    private IReporteOracleService reporteService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void executesReportsViewAndStoredProcedureAgainstOracle() {
        assertNotNull(reporteRepository.findObjetosPorEstado());
        assertNotNull(reporteRepository.findReclamosPorEstado());
        assertNotNull(reporteRepository.findHallazgosPorUbicacion());
        assertNotNull(reporteRepository.findEntregasPorCategoria());
        assertNotNull(reporteRepository.findSeguimientoReclamos());
        assertNotNull(reporteRepository.findVistaSeguimiento());
        assertNotNull(reporteRepository.findVistaSinRevisar());
        assertNotNull(reporteRepository.findResumenVista());
        assertNotNull(reporteService.objetosPorEstado());
        assertNotNull(reporteService.reclamosPorEstado());
        assertNotNull(reporteService.hallazgosPorUbicacion());
        assertNotNull(reporteService.entregasPorCategoria());
        assertNotNull(reporteService.seguimientoReclamos());
        assertNotNull(reporteService.vistaSeguimiento());
        assertNotNull(reporteService.vistaSinRevisar());
        assertNotNull(reporteService.resumenVista());

        ResumenReclamosDTO summary = assertDoesNotThrow(procedimientoRepository::ejecutarResumenReclamos);
        assertNotNull(summary.total());
        assertNotNull(summary.sinRevisar());
        assertNotNull(summary.revisados());
    }

    @Test
    void restrictsAdministrativeReportsToExactAdminAuthority() throws Exception {
        String path = "/api/v1/admin/reportes/objetos-por-estado";

        mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(path).with(user("estudiante")
                        .authorities(new SimpleGrantedAuthority("ESTUDIANTE"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(path).with(user("admin")
                        .authorities(new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void servesEveryReportAndValidatesLoginRequest() throws Exception {
        var administrator = user("admin").authorities(new SimpleGrantedAuthority("ADMIN"));
        for (String endpoint : new String[]{
                "objetos-por-estado", "reclamos-por-estado", "hallazgos-por-ubicacion",
                "entregas-por-categoria", "seguimiento-reclamos", "resumen-reclamos",
                "vista-seguimiento", "vista-sin-revisar", "vista-resumen"
        }) {
            mockMvc.perform(get("/api/v1/admin/reportes/" + endpoint).with(administrator))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"documentoIdentidad":"","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void exposesAllReportsAndBearerAuthenticationInOpenApi() throws Exception {
        String specification = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(specification.contains("Reportes Administrativos UPeU"));
        assertTrue(specification.contains("BearerAuth"));
        for (String endpoint : new String[]{
                "objetos-por-estado", "reclamos-por-estado", "hallazgos-por-ubicacion",
                "entregas-por-categoria", "seguimiento-reclamos", "resumen-reclamos",
                "vista-seguimiento", "vista-sin-revisar", "vista-resumen"
        }) {
            assertTrue(specification.contains(endpoint), "Falta documentar " + endpoint);
        }
    }
}
