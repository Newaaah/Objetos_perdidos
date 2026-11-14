package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "AREA")
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_area")
    @SequenceGenerator(name = "gen_area", sequenceName = "SEQ_AREA", allocationSize = 1)
    @Column(name = "ID_AREA", nullable = false)
    private Long idArea;

    @Column(name = "NOMBRE", nullable = false, length = 50, unique = true)
    private String nombre;

    public Area() {
    }

    public Long getIdArea() {
        return idArea;
    }

    public void setIdArea(Long idArea) {
        this.idArea = idArea;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}
