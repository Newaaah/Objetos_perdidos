package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.objetosperdidos.dto.UbicacionDTO;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.services.IUbicacionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ubicaciones")
@Tag(name = "Ubicaciones", description = "Mantenimiento del catalogo de ubicaciones UPeU")
public class UbicacionController {

    private final IUbicacionService ubicacionService;

    public UbicacionController(IUbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @Operation(summary = "Listar ubicaciones")
    @GetMapping
    public ResponseEntity<List<UbicacionDTO>> listar() {
        return ResponseEntity.ok(ubicacionService.listarTodo());
    }

    @Operation(summary = "Buscar ubicacion por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UbicacionDTO> obtener(@PathVariable Long id) {
        return ubicacionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ubicacion no encontrada con ID: " + id));
    }

    @Operation(summary = "Registrar ubicacion")
    @PostMapping
    public ResponseEntity<UbicacionDTO> crear(@Valid @RequestBody UbicacionDTO dto) {
        return new ResponseEntity<>(ubicacionService.crear(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar ubicacion")
    @PutMapping("/{id}")
    public ResponseEntity<UbicacionDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UbicacionDTO dto) {
        return ubicacionService.actualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ubicacion no encontrada con ID: " + id));
    }

    @Operation(summary = "Eliminar ubicacion si no esta protegida ni referenciada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (ubicacionService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        throw new ResourceNotFoundException("Ubicacion no encontrada con ID: " + id);
    }
}
