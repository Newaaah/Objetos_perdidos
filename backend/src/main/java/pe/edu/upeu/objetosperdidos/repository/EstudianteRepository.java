
package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.edu.upeu.objetosperdidos.entity.Estudiante;

import java.util.Optional;

@Repository
public interface EstudianteRepository
        extends JpaRepository<Estudiante, Long> {

    // Buscar estudiante por codigo universitario
    Optional<Estudiante> findByCodigoUni(String codigoUni);

    // Verificar si un codigo universitario existe
    boolean existsByCodigoUni(String codigoUni);

    // Buscar estudiante por tipo y numero de documento
    Optional<Estudiante>
    findByPersonaTipoDocumentoAndPersonaDocumentoIdentidad(
            String tipoDocumento,
            String documentoIdentidad
    );
}
