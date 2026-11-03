package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Area;

@Repository
public interface AreaRepository
        extends JpaRepository<Area, Long> {

}

