package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.objetosperdidos.dto.*;
import pe.edu.upeu.objetosperdidos.services.IReporteEstadisticoService;

@RestController
@RequestMapping("/api/v1/reportes")
@Tag(name = "Reportes estadísticos", description = "Reportes Oracle, vista y procedimiento almacenado")
public class ReporteEstadisticoController {

    private final IReporteEstadisticoService service;

    public ReporteEstadisticoController(IReporteEstadisticoService service) {
        this.service = service;
    }

    @Operation(summary = "Reporte 1A: cantidad de pérdidas por estado")
    @GetMapping("/perdidas/por-estado")
    public ResponseEntity<List<ConteoEstadoDTO>> perdidasPorEstado() {
        return ResponseEntity.ok(service.perdidasPorEstado());
    }

    @Operation(summary = "Reporte 1B: cantidad de hallazgos por estado")
    @GetMapping("/hallazgos/por-estado")
    public ResponseEntity<List<ConteoEstadoDTO>> hallazgosPorEstado() {
        return ResponseEntity.ok(service.hallazgosPorEstado());
    }

    @Operation(summary = "Cantidad de custodias aún disponibles y ya entregadas")
    @GetMapping("/custodias/por-situacion")
    public ResponseEntity<List<ConteoEstadoDTO>> custodiasPorSituacion() {
        return ResponseEntity.ok(service.custodiasPorSituacion());
    }

    @Operation(summary = "Reporte 2: porcentaje de custodias entregadas por categoría")
    @GetMapping("/entregas/por-categoria")
    public ResponseEntity<List<PorcentajeEntregasCategoriaDTO>> entregasPorCategoria() {
        return ResponseEntity.ok(service.entregasPorCategoria());
    }

    @Operation(summary = "Pérdidas registradas por mes")
    @GetMapping("/perdidas/por-mes")
    public ResponseEntity<List<ConteoMesDTO>> perdidasPorMes() {
        return ResponseEntity.ok(service.perdidasPorMes());
    }

    @Operation(summary = "Entregas registradas por mes")
    @GetMapping("/entregas/por-mes")
    public ResponseEntity<List<ConteoMesDTO>> entregasPorMes() {
        return ResponseEntity.ok(service.entregasPorMes());
    }

    @Operation(summary = "Reclamos por estado (sin contar incidencias)")
    @GetMapping("/reclamos/por-estado")
    public ResponseEntity<List<ConteoEstadoDTO>> reclamosPorEstado() {
        return ResponseEntity.ok(service.reclamosPorEstado());
    }

    @Operation(summary = "Ejecuta el procedimiento Oracle SP_RESUMEN_RECLAMOS")
    @GetMapping("/reclamos/resumen")
    public ResponseEntity<ResumenReclamosDTO> resumenReclamos() {
        return ResponseEntity.ok(service.resumenReclamosDesdeProcedimiento());
    }

    @Operation(summary = "Consulta la vista Oracle VW_SEGUIMIENTO_RECLAMOS")
    @GetMapping("/reclamos/seguimiento")
    public ResponseEntity<List<SeguimientoReclamoDTO>> seguimientoReclamos() {
        return ResponseEntity.ok(service.seguimientoDesdeVista());
    }

    // Rutas operativas: cuando integremos SecurityConfig, restringir a PERSONAL/ADMIN
    // y comprobar asignación de personal a la ubicación indicada.
    @Operation(summary = "Hallazgos aprobados todavía pendientes de recepción en una ubicación")
    @GetMapping("/ubicaciones/{idUbicacion}/pendientes-recepcion")
    public ResponseEntity<List<PendienteRecepcionDTO>> pendientesRecepcion(@PathVariable Long idUbicacion) {
        return ResponseEntity.ok(service.pendientesDeRecepcion(idUbicacion));
    }

    @Operation(summary = "Custodias no entregadas en una ubicación")
    @GetMapping("/ubicaciones/{idUbicacion}/custodias-disponibles")
    public ResponseEntity<List<CustodiaDisponibleDTO>> custodiasDisponibles(@PathVariable Long idUbicacion) {
        return ResponseEntity.ok(service.custodiasDisponibles(idUbicacion));
    }

    // La API de MIS reportes no se expone aquí con un idPersona arbitrario:
    // se añadirá cuando JWT permita extraer ID_PERSONA del usuario autenticado.
}
