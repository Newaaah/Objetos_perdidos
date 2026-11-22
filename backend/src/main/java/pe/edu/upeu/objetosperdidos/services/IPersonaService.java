package pe.edu.upeu.objetosperdidos.services;

import pe.edu.upeu.objetosperdidos.dto.PersonaResumenDTO;

public interface IPersonaService {

    PersonaResumenDTO buscar(String ingreso);
}