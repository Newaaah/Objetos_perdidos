package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.objetosperdidos.dto.CarreraDTO;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.services.ICarreraService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carreras")
@Tag(name = "Carreras", description = "Mantenimiento del catalogo de carreras UPeU")
public class CarreraController {

    private final ICarreraService carreraService;

    public CarreraController(ICarreraService carreraService) {
        this.carreraService = carreraService;
    }

    @Operation(summary = "Listar carreras")
    @GetMapping
    public ResponseEntity<List<CarreraDTO>> listar() {
        return ResponseEntity.ok(carreraService.listarTodo());
    }

    @Operation(summary = "Buscar carrera por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CarreraDTO> obtener(@PathVariable Long id) {
        return carreraService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Carrera no encontrada con ID: " + id));
    }

    @Operation(summary = "Registrar carrera")
    @PostMapping
    public ResponseEntity<CarreraDTO> crear(@Valid @RequestBody CarreraDTO dto) {
        return new ResponseEntity<>(carreraService.crear(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar carrera")
    @PutMapping("/{id}")
    public ResponseEntity<CarreraDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CarreraDTO dto) {
        return carreraService.actualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Carrera no encontrada con ID: " + id));
    }

    @Operation(summary = "Eliminar carrera si no tiene estudiantes asociados")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (carreraService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        throw new ResourceNotFoundException("Carrera no encontrada con ID: " + id);
    }
}
