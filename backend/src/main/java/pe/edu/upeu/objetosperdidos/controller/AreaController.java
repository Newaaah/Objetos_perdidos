package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.objetosperdidos.dto.AreaDTO;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.services.IAreaService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/areas")
@Tag(name = "Areas", description = "Mantenimiento del catalogo de areas UPeU")
public class AreaController {

    private final IAreaService areaService;

    public AreaController(IAreaService areaService) {
        this.areaService = areaService;
    }

    @Operation(summary = "Listar areas")
    @GetMapping
    public ResponseEntity<List<AreaDTO>> listar() {
        return ResponseEntity.ok(areaService.listarTodo());
    }

    @Operation(summary = "Buscar area por ID")
    @GetMapping("/{id}")
    public ResponseEntity<AreaDTO> obtener(@PathVariable Long id) {
        return areaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Area no encontrada con ID: " + id));
    }

    @Operation(summary = "Registrar area")
    @PostMapping
    public ResponseEntity<AreaDTO> crear(@Valid @RequestBody AreaDTO dto) {
        return new ResponseEntity<>(areaService.crear(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar area")
    @PutMapping("/{id}")
    public ResponseEntity<AreaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AreaDTO dto) {
        return areaService.actualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Area no encontrada con ID: " + id));
    }

    @Operation(summary = "Eliminar area si no tiene personal asociado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (areaService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        throw new ResourceNotFoundException("Area no encontrada con ID: " + id);
    }
}
