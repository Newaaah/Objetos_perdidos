package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CATEGORIA")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_categoria")
    @SequenceGenerator(name = "gen_categoria", sequenceName = "SEQ_CATEGORIA", allocationSize = 1)
    @Column(name = "ID_CATEGORIA", nullable = false)
    private Long idCategoria;

    @Column(name = "NOMBRE", nullable = false, length = 50, unique = true)
    private String nombre;

    public Categoria() {
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}
