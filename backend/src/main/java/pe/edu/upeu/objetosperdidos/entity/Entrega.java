package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ENTREGA")
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ENTREGA")
    private Long idEntrega;

    @ManyToOne
    @JoinColumn(name = "ID_OBJETO")
    private ObjetosCustodiados objeto;

    @ManyToOne
    @JoinColumn(name = "ID_SOLICITUD")
    private Solicitud solicitud;

    @ManyToOne
    @JoinColumn(name = "ID_PERSONAL")
    private Personal personal;

    @ManyToOne
    @JoinColumn(name = "ID_PERSONA")
    private Persona persona;

    @Column(name = "IMAGEN", length = 255)
    private String imagen;

    @Column(name = "FECHA_ENTREGA", updatable = false)
    private LocalDateTime fechaEntrega;

    public Entrega() {
    }

    public Entrega(ObjetosCustodiados objeto,
                   Solicitud solicitud,
                   Personal personal,
                   Persona persona,
                   String imagen) {
        this.objeto = objeto;
        this.solicitud = solicitud;
        this.personal = personal;
        this.persona = persona;
        this.imagen = imagen;
    }

    @PrePersist
    public void registrarFecha() {
        if (fechaEntrega == null) {
            fechaEntrega = LocalDateTime.now();
        }
    }

    public Long getIdEntrega() {
        return idEntrega;
    }

    public void setIdEntrega(Long idEntrega) {
        this.idEntrega = idEntrega;
    }

    public ObjetosCustodiados getObjeto() {
        return objeto;
    }

    public void setObjeto(ObjetosCustodiados objeto) {
        this.objeto = objeto;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
    }

    public Personal getPersonal() {
        return personal;
    }

    public void setPersonal(Personal personal) {
        this.personal = personal;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }
}