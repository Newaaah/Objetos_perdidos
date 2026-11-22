package pe.edu.upeu.objetosperdidos.services;

import pe.edu.upeu.objetosperdidos.dto.PersonaResumenDTO;

public interface IPersonaService {

    PersonaResumenDTO buscar(String ingreso);

    // Datos de la persona conectada (el ID sale del token, no del cliente)
    PersonaResumenDTO perfil(Long idPersona);
}