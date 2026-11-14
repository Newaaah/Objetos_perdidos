package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CARRERA")
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_carrera")
    @SequenceGenerator(name = "gen_carrera", sequenceName = "SEQ_CARRERA", allocationSize = 1)
    @Column(name = "ID_CARRERA", nullable = false)
    private Long idCarrera;

    @Column(name = "NOMBRE", nullable = false, length = 50, unique = true)
    private String nombre;

    public Carrera() {
    }

    public Long getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(Long idCarrera) {
        this.idCarrera = idCarrera;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}
