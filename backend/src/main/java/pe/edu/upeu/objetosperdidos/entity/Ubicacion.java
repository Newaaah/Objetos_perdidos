package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "UBICACION")
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_UBICACION")
    private Long idUbicacion;

    @Column(name = "NOMBRE", length = 100)
    private String nombre;

    @Column(name = "DETALLE", length = 255)
    private String detalle;

    public Ubicacion() {
    }

    public Ubicacion(Long idUbicacion, String nombre, String detalle) {
        this.idUbicacion = idUbicacion;
        this.nombre = nombre;
        this.detalle = detalle;
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
