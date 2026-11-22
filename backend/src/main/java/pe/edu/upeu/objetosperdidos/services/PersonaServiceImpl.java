package pe.edu.upeu.objetosperdidos.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.dto.PersonaResumenDTO;
import pe.edu.upeu.objetosperdidos.entity.Estudiante;
import pe.edu.upeu.objetosperdidos.entity.Persona;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.repository.EstudianteRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonaRepository;
import pe.edu.upeu.objetosperdidos.security.IdentificadorLogin;

@Service
public class PersonaServiceImpl implements IPersonaService {

    private final PersonaRepository personas;
    private final EstudianteRepository estudiantes;

    public PersonaServiceImpl(PersonaRepository personas, EstudianteRepository estudiantes) {
        this.personas = personas;
        this.estudiantes = estudiantes;
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResumenDTO buscar(String ingreso) {
        String valor = IdentificadorLogin.normalizarIngreso(ingreso);

        if (IdentificadorLogin.esCodigoUniversitario(valor)) {
            Estudiante e = estudiantes.findByCodigoUni(valor)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe un estudiante con ese codigo"));
            return convertir(e.getPersona(), e);
        }

        Persona p = personas.findByTipoDocumentoAndDocumentoIdentidad("DNI", valor)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una persona con ese DNI"));
        return convertir(p, estudiantes.findById(p.getIdPersona()).orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResumenDTO perfil(Long idPersona) {
        Persona p = personas.findById(idPersona)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la persona"));
        return convertir(p, estudiantes.findById(idPersona).orElse(null));
    }

    private static PersonaResumenDTO convertir(Persona p, Estudiante e) {
        return new PersonaResumenDTO(
                p.getIdPersona(),
                p.getNombres(),
                p.getApellidos(),
                p.getDocumentoIdentidad(),
                e == null ? null : e.getCodigoUni(),
                e == null || e.getCarrera() == null ? null : e.getCarrera().getNombre());
    }
}