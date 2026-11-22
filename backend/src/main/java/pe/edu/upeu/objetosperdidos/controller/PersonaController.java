package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.objetosperdidos.dto.PersonaResumenDTO;
import pe.edu.upeu.objetosperdidos.services.IPersonaService;

@RestController
@RequestMapping("/api/v1/personas")
@Tag(name = "Personas", description = "Busqueda por DNI o codigo. Solo PERSONAL y ADMIN")
public class PersonaController {

    private final IPersonaService servicio;

    public PersonaController(IPersonaService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Buscar persona por DNI o codigo universitario")
    @GetMapping("/buscar")
    public ResponseEntity<PersonaResumenDTO> buscar(@RequestParam String ingreso) {
        return ResponseEntity.ok(servicio.buscar(ingreso));
    }
}