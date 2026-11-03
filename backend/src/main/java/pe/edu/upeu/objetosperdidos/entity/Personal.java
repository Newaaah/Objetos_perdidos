package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PERSONAL")
public class Personal {

    @Id
    @Column(name = "ID_PERSONA")
    private Long idPersona;

    @OneToOne
    @MapsId
    @JoinColumn(name = "ID_PERSONA")
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "ID_AREA")
    private Area area;

    @Column(name = "CARGO", length = 20)
    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 20)
    private EstadoPersonal estado;

    public Personal() {
    }

    public Personal(Persona persona, Area area,
                    String cargo, EstadoPersonal estado) {
        this.persona = persona;
        this.area = area;
        this.cargo = cargo;
        this.estado = estado;
    }

    public Long getIdPersona() {
        return idPersona;
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
