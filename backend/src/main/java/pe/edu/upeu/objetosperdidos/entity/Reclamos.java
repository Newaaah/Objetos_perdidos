package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RECLAMOS")
public class Reclamos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RECLAMO")
    private Long idReclamo;

    @ManyToOne
    @JoinColumn(name = "ID_ENTREGA")
    private Entrega entrega;

    @ManyToOne
    @JoinColumn(name = "ID_PERSONA_RECLAMA")
    private Persona personaReclama;

    @Column(name = "DETALLE_ARGUMENTO", length = 255)
    private String detalleArgumento;

    @Column(name = "ESTADO", length = 20)
    private String estado;

    @Column(name = "FECHA_RECLAMO", updatable = false)
    private LocalDateTime fechaReclamo;

    public Reclamos() {
    }

    public Reclamos(Entrega entrega,
                    Persona personaReclama,
                    String detalleArgumento,
                    String estado) {
        this.entrega = entrega;
        this.personaReclama = personaReclama;
        this.detalleArgumento = detalleArgumento;
        this.estado = estado;
    }

    @PrePersist
    public void registrarFecha() {
        if (fechaReclamo == null) {
            fechaReclamo = LocalDateTime.now();
        }
    }

    public Long getIdReclamo() {
        return idReclamo;
    }

    public void setIdReclamo(Long idReclamo) {
        this.idReclamo = idReclamo;
    }

    public Entrega getEntrega() {
        return entrega;
    }

    public void setEntrega(Entrega entrega) {
        this.entrega = entrega;
    }

    public Persona getPersonaReclama() {
        return personaReclama;
    }

    public void setPersonaReclama(Persona personaReclama) {
        this.personaReclama = personaReclama;
    }

    public String getDetalleArgumento() {
        return detalleArgumento;
    }

    public void setDetalleArgumento(String detalleArgumento) {
        this.detalleArgumento = detalleArgumento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaReclamo() {
        return fechaReclamo;
    }
}
