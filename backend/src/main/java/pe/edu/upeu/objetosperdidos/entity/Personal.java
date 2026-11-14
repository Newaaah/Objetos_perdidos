package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;

/** CARGO ampliado a 100 caracteres segun el refinamiento posterior al PDF. */
@Entity
@Table(name = "PERSONAL")
public class Personal {

    @Id
    @Column(name = "ID_PERSONA")
    private Long idPersona;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERSONA", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_AREA", nullable = false)
    private Area area;

    @Column(name = "CARGO", nullable = false, length = 100)
    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoPersonal estado;

    public Personal() {
    }

    public Personal(Persona persona) {
        this.persona = persona;
    }

    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public EstadoPersonal getEstado() {
        return estado;
    }

    public void setEstado(EstadoPersonal estado) {
        this.estado = estado;
    }

}
