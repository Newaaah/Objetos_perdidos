package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SOLICITUD")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SOLICITUD")
    private Long idSolicitud;

    @ManyToOne
    @JoinColumn(name = "ID_OBJETO")
    private ObjetosCustodiados objeto;

    @ManyToOne
    @JoinColumn(name = "ID_PERSONA")
    private Persona persona;

    @Column(name = "DETALLE_PRUEBA", length = 100)
    private String detallePrueba;

    @Column(name = "ESTADO", length = 20)
    private String estado;

    @Column(name = "FECHA_SOLICITUD", updatable = false)
    private LocalDateTime fechaSolicitud;

    @ManyToOne
    @JoinColumn(name = "ID_PERSONAL")
    private Personal personal;

    @Column(name = "FECHA_REVISION")
    private LocalDateTime fechaRevision;

    @Column(name = "OBSERVACION", length = 255)
    private String observacion;

    public Solicitud() {
    }

    public Solicitud(ObjetosCustodiados objeto,
                     Persona persona,
                     String detallePrueba,
                     String estado) {
        this.objeto = objeto;
        this.persona = persona;
        this.detallePrueba = detallePrueba;
        this.estado = estado;
    }

    @PrePersist
    public void registrarFecha() {
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }
    }

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public ObjetosCustodiados getObjeto() {
        return objeto;
    }

    public void setObjeto(ObjetosCustodiados objeto) {
        this.objeto = objeto;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public String getDetallePrueba() {
        return detallePrueba;
    }

    public void setDetallePrueba(String detallePrueba) {
        this.detallePrueba = detallePrueba;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public Personal getPersonal() {
        return personal;
    }

    public void setPersonal(Personal personal) {
        this.personal = personal;
    }

    public LocalDateTime getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(LocalDateTime fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
