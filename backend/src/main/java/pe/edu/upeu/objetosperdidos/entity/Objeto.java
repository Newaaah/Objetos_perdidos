package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;

/** Ficha descriptiva; dos reportes independientes pueden describir el mismo objeto fisico con fichas distintas. */
@Entity
@Table(name = "OBJETO")
public class Objeto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_objeto")
    @SequenceGenerator(name = "gen_objeto", sequenceName = "SEQ_OBJETO", allocationSize = 1)
    @Column(name = "ID_OBJETO", nullable = false)
    private Long idObjeto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CATEGORIA", nullable = false)
    private Categoria categoria;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "MARCA", nullable = true, length = 100)
    private String marca;

    @Column(name = "COLOR", nullable = true, length = 50)
    private String color;

    @Column(name = "DESCRIPCION", nullable = false, length = 255)
    private String descripcion;

    public Objeto() {
    }

    public Long getIdObjeto() {
        return idObjeto;
    }

    public void setIdObjeto(Long idObjeto) {
        this.idObjeto = idObjeto;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
