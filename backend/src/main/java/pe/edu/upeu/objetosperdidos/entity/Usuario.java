package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "USUARIO")
public class Usuario {

    @Id
    @Column(name = "ID_PERSONA")
    private Long idPersona;

    @OneToOne
    @MapsId
    @JoinColumn(name = "ID_PERSONA")
    private Persona persona;

    @Column(name = "PASSWORD", length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROL", length = 50)
    private RolUsuario rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 20)
    private EstadoUsuario estado;

    public Usuario() {
    }

    public Usuario(Persona persona, String password,
                   RolUsuario rol, EstadoUsuario estado) {
        this.persona = persona;
        this.password = password;
        this.rol = rol;
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