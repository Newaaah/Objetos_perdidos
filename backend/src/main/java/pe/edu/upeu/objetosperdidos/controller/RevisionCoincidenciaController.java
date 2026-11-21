package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.objetosperdidos.dto.CoincidenciaRevisadaDTO;
import pe.edu.upeu.objetosperdidos.dto.EvaluacionCoincidenciaDTO;
import pe.edu.upeu.objetosperdidos.dto.RevisarCoincidenciaRequest;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.IRevisionCoincidenciaService;

@RestController
@RequestMapping("/api/v1/coincidencias/revision")
@Tag(name = "Revision formal de coincidencias", description = "Solo PERSONAL/ADMIN. No autoriza entregas.")
public class RevisionCoincidenciaController {

    private final IRevisionCoincidenciaService servicio;

    public RevisionCoincidenciaController(IRevisionCoincidenciaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/evaluar")
    @Operation(summary = "Comparar una perdida y un hallazgo para revision de personal")
    public ResponseEntity<EvaluacionCoincidenciaDTO> evaluar(
            @RequestParam Long idReportePerdida,
            @RequestParam Long idHallazgo) {
        return ResponseEntity.ok(servicio.evaluar(idReportePerdida, idHallazgo));
    }

    @PostMapping
    @Operation(summary = "Confirmar o descartar una pareja perdida-hallazgo (decision definitiva)")
    public ResponseEntity<CoincidenciaRevisadaDTO> registrar(
            @Valid @RequestBody RevisarCoincidenciaRequest solicitud,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicio.registrar(solicitud, usuario.idPersona()));
    }

    @GetMapping("/reporte/{idReportePerdida}")
    @Operation(summary = "Consultar revisiones registradas para un reporte de perdida")
    public ResponseEntity<List<CoincidenciaRevisadaDTO>> porReporte(
            @PathVariable Long idReportePerdida) {
        return ResponseEntity.ok(servicio.porReporte(idReportePerdida));
    }

    @GetMapping("/{idCoincidencia}")
    @Operation(summary = "Consultar una revision registrada")
    public ResponseEntity<CoincidenciaRevisadaDTO> consultar(@PathVariable Long idCoincidencia) {
        return ResponseEntity.ok(servicio.consultar(idCoincidencia));
    }
}
