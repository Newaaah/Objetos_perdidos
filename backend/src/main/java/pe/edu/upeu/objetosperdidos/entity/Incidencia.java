package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "INCIDENCIA")
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INCIDENCIA")
    private Long idIncidencia;

    @ManyToOne
    @JoinColumn(name = "ID_RECLAMO")
    private Reclamos reclamo;

    @ManyToOne
    @JoinColumn(name = "ID_PERSONAL")
    private Personal personal;

    @Column(name = "DESCRIPCION", length = 255)
    private String descripcion;

    @Column(name = "EVIDENCIAS", length = 255)
    private String evidencias;

    @Column(name = "FECHA_INCIDENCIA", updatable = false)
    private LocalDateTime fechaIncidencia;

    public Incidencia() {
    }

    public Incidencia(Reclamos reclamo,
                      Personal personal,
                      String descripcion,
                      String evidencias) {
        this.reclamo = reclamo;
        this.personal = personal;
        this.descripcion = descripcion;
        this.evidencias = evidencias;
    }

    @PrePersist
    public void registrarFecha() {
        if (fechaIncidencia == null) {
            fechaIncidencia = LocalDateTime.now();
        }
    }

    public Long getIdIncidencia() {
        return idIncidencia;
    }

    public void setIdIncidencia(Long idIncidencia) {
        this.idIncidencia = idIncidencia;
    }

    public Reclamos getReclamo() {
        return reclamo;
    }

    public void setReclamo(Reclamos reclamo) {
        this.reclamo = reclamo;
    }

    public Personal getPersonal() {
        return personal;
    }

    public void setPersonal(Personal personal) {
        this.personal = personal;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEvidencias() {
        return evidencias;
    }

    public void setEvidencias(String evidencias) {
        this.evidencias = evidencias;
    }

    public LocalDateTime getFechaIncidencia() {
        return fechaIncidencia;
    }
}
