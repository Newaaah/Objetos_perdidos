package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;

/** Una persona tiene como maximo una cuenta. PASSWORD almacena solamente el hash. */
@Entity
@Table(name = "USUARIO")
public class Usuario {

    @Id
    @Column(name = "ID_PERSONA")
    private Long idPersona;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERSONA", nullable = false)
    private Persona persona;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROL", nullable = false, length = 50)
    private RolUsuario rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoUsuario estado;

    public Usuario() {
    }

    public Usuario(Persona persona) {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

}
