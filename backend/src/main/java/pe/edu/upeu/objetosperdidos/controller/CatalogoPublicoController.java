package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.objetosperdidos.dto.ObjetoEntregadoPublicoDTO;
import pe.edu.upeu.objetosperdidos.dto.ObjetoPublicoDTO;
import pe.edu.upeu.objetosperdidos.services.IFileStorageService;
import pe.edu.upeu.objetosperdidos.services.ICatalogoPublicoService;

@RestController
@RequestMapping("/api/v1/publico/objetos")
@Tag(name = "Catalogo publico", description = "Objetos en custodia y entregados. No requiere login")
public class CatalogoPublicoController {

    private final ICatalogoPublicoService servicio;

    public CatalogoPublicoController(ICatalogoPublicoService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Objetos que siguen bajo custodia de la UPeU")
    @GetMapping("/disponibles")
    public ResponseEntity<List<ObjetoPublicoDTO>> disponibles(
            @Parameter(description = "Filtrar por categoria (opcional)")
            @RequestParam(required = false) Long idCategoria) {
        return ResponseEntity.ok(servicio.disponibles(idCategoria));
    }

    @Operation(summary = "Objetos ya entregados (para reconocer el tuyo y reclamar)")
    @GetMapping("/entregados")
    public ResponseEntity<List<ObjetoEntregadoPublicoDTO>> entregados(
            @Parameter(description = "Filtrar por categoria (opcional)")
            @RequestParam(required = false) Long idCategoria,
            @Parameter(description = "Ultimos N dias (1-365, por defecto 90)")
            @RequestParam(required = false) Integer dias) {
        return ResponseEntity.ok(servicio.entregados(idCategoria, dias));
    }

    @Operation(summary = "Fotografia del objeto (nunca la evidencia de entrega)")
    @GetMapping("/{idCustodia}/imagen")
    public ResponseEntity<byte[]> imagen(@PathVariable Long idCustodia) {
        IFileStorageService.Imagen foto = servicio.imagen(idCustodia);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                .contentType(foto.mediaType())
                .body(foto.contenido());
    }
}
