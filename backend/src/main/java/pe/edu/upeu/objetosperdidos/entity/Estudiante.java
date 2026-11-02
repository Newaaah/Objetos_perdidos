package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ESTUDIANTE")
public class Estudiante {

    @Id
    @Column(name = "ID_PERSONA")
    private Long idPersona;

    @OneToOne
    @MapsId
    @JoinColumn(name = "ID_PERSONA")
    private Persona persona;

    @Column(name = "CODIGO_UNI", length = 9)
    private String codigoUni;

    @ManyToOne
    @JoinColumn(name = "ID_CARRERA")
    private Carrera carrera;

    @Column(name = "CICLO")
    private Integer ciclo;

    @Column(name = "ESTADO", length = 20)
    private String estado;

    public Estudiante() {
    }

    public Estudiante(Persona persona, String codigoUni,
                      Carrera carrera, Integer ciclo, String estado) {
        this.persona = persona;
        this.codigoUni = codigoUni;
        this.carrera = carrera;
        this.ciclo = ciclo;
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

    public String getCodigoUni() {
        return codigoUni;
    }

    public void setCodigoUni(String codigoUni) {
        this.codigoUni = codigoUni;
    }

    public Carrera getCarrera() {
        return carrera;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }

    public Integer getCiclo() {
        return ciclo;
    }

    public void setCiclo(Integer ciclo) {
        this.ciclo = ciclo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
