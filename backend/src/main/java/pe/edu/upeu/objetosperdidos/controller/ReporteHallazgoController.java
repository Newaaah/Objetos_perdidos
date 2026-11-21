package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.objetosperdidos.dto.CrearReporteHallazgoRequest;
import pe.edu.upeu.objetosperdidos.dto.MiHallazgoDTO;
import pe.edu.upeu.objetosperdidos.dto.ReporteHallazgoCreadoDTO;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.IReporteHallazgoService;

@RestController
@RequestMapping("/api/v1/hallazgos")
@Tag(name = "Reportes de hallazgo",
        description = "Registro de objetos encontrados y consulta de mis reportes")
public class ReporteHallazgoController {

    private final IReporteHallazgoService servicio;

    public ReporteHallazgoController(IReporteHallazgoService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Registrar un hallazgo con imagen (no confirma custodia)")
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ReporteHallazgoCreadoDTO> crear(
            @ModelAttribute @ParameterObject @Valid CrearReporteHallazgoRequest dto,
            @Parameter(description = "Foto del objeto JPG, PNG o WebP, hasta 5 MB")
            @RequestPart("imagen") MultipartFile imagen,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return new ResponseEntity<>(
                servicio.crear(dto, imagen, usuario.idPersona()), HttpStatus.CREATED);
    }

    @Operation(summary = "Consultar solamente mis reportes de hallazgos")
    @GetMapping("/mios")
    public ResponseEntity<List<MiHallazgoDTO>> misHallazgos(
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok(servicio.misHallazgos(usuario.idPersona()));
    }
}
