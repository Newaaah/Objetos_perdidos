package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.objetosperdidos.dto.ResultadoSugerenciasDTO;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.ISugerenciasCoincidenciaService;

@RestController
@RequestMapping("/api/v1/coincidencias/sugerencias")
@Tag(name = "Sugerencias de coincidencias", description = "Candidatos para mis perdidas")
public class SugerenciasCoincidenciaController {

    private final ISugerenciasCoincidenciaService servicio;

    public SugerenciasCoincidenciaController(ISugerenciasCoincidenciaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/mias/{idReportePerdida}")
    @Operation(summary = "Sugerir coincidencias para uno de mis reportes de perdida")
    public ResponseEntity<ResultadoSugerenciasDTO> sugerir(
            @PathVariable Long idReportePerdida,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok(servicio.sugerir(idReportePerdida, usuario.idPersona()));
    }
}
