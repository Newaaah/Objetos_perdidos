package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ESTUDIANTE")
public class Estudiante {

    @Id
    @Column(name = "ID_PERSONA")
    private Long idPersona;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERSONA", nullable = false)
    private Persona persona;

    @Column(name = "CODIGO_UNI", nullable = false, length = 9, unique = true, columnDefinition = "CHAR(9)")
    private String codigoUni;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CARRERA", nullable = false)
    private Carrera carrera;

    @Column(name = "CICLO", nullable = true, columnDefinition = "NUMBER(2)")
    private Integer ciclo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoEstudiante estado;

    public Estudiante() {
    }

    public Estudiante(Persona persona) {
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

    public EstadoEstudiante getEstado() {
        return estado;
    }

    public void setEstado(EstadoEstudiante estado) {
        this.estado = estado;
    }

}
