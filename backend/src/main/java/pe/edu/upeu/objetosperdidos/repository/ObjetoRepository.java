package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Objeto;
import java.util.List;

@Repository
public interface ObjetoRepository extends JpaRepository<Objeto, Long> {
    List<Objeto> findByCategoriaIdCategoria(Long idCategoria);
}
