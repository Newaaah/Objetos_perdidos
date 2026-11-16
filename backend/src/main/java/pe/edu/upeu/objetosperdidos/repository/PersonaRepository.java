package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Persona;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByTipoDocumentoAndDocumentoIdentidad(String tipoDocumento, String documentoIdentidad);
    boolean existsByTipoDocumentoAndDocumentoIdentidad(String tipoDocumento, String documentoIdentidad);
}
