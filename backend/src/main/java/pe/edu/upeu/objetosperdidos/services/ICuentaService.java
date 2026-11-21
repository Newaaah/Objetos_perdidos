package pe.edu.upeu.objetosperdidos.services;

/** Contrasenas: el ADMIN habilita la de un estudiante y cada uno cambia la suya. */
public interface ICuentaService {

    void aprovisionarEstudiante(Long idPersona, String clave, Long idAdmin);

    void cambiarMiClave(Long idPersona, String actual, String nueva);
}
