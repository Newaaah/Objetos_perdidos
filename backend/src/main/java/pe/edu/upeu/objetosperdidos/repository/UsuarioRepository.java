package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Usuario;
import java.util.List;
import java.util.Optional;
import pe.edu.upeu.objetosperdidos.entity.EstadoUsuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByPersonaTipoDocumentoAndPersonaDocumentoIdentidad(String tipoDocumento, String documentoIdentidad);
    List<Usuario> findByEstado(EstadoUsuario estado);
}
