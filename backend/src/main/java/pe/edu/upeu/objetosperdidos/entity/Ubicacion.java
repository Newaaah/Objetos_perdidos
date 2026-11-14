package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "UBICACION")
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_ubicacion")
    @SequenceGenerator(name = "gen_ubicacion", sequenceName = "SEQ_UBICACION", allocationSize = 1)
    @Column(name = "ID_UBICACION", nullable = false)
    private Long idUbicacion;

    @Column(name = "NOMBRE", nullable = false, length = 100, unique = true)
    private String nombre;

    @Column(name = "DETALLE", nullable = true, length = 255)
    private String detalle;

    public Ubicacion() {
    }

    public Long getIdUbicacion() {
        return idUbicacion;
    }

    public void setIdUbicacion(Long idUbicacion) {
        this.idUbicacion = idUbicacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

}
