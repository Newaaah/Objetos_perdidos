package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.objetosperdidos.dto.CrearReportePerdidaRequest;
import pe.edu.upeu.objetosperdidos.dto.MiPerdidaDTO;
import pe.edu.upeu.objetosperdidos.dto.ReportePerdidaCreadoDTO;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.IReportePerdidaService;

@RestController
@RequestMapping("/api/v1/perdidas")
@Tag(name = "Reportes de perdida", description = "Registro y consulta de mis objetos perdidos")
public class ReportePerdidaController {

    private final IReportePerdidaService servicio;

    public ReportePerdidaController(IReportePerdidaService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Registrar un objeto perdido a nombre del usuario autenticado")
    @PostMapping
    public ResponseEntity<ReportePerdidaCreadoDTO> crear(
            @Valid @RequestBody CrearReportePerdidaRequest solicitud,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        ReportePerdidaCreadoDTO resultado = servicio.crear(solicitud, usuario.idPersona());
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @Operation(summary = "Consultar solamente mis reportes de perdida")
    @GetMapping("/mias")
    public ResponseEntity<List<MiPerdidaDTO>> misPerdidas(
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok(servicio.misPerdidas(usuario.idPersona()));
    }
}
