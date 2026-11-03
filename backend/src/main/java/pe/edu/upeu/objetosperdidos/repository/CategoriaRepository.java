package pe.edu.upeu.objetosperdidos.repository;

import pe.edu.upeu.objetosperdidos.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository
        extends JpaRepository<Categoria, Long> {

}
