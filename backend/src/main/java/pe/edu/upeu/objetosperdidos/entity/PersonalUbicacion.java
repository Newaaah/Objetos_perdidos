package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZoneId;

/** FECHA_FIN nula = asignacion vigente. Una misma persona no puede tener dos asignaciones vigentes a una ubicacion. */
@Entity
@Table(name = "PERSONAL_UBICACION")
public class PersonalUbicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_personal_ubicacion")
    @SequenceGenerator(name = "gen_personal_ubicacion", sequenceName = "SEQ_PERSONAL_UBICACION", allocationSize = 1)
    @Column(name = "ID_ASIGNACION", nullable = false)
    private Long idAsignacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERSONA", nullable = false)
    private Personal persona;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_UBICACION", nullable = false)
    private Ubicacion ubicacion;

    @Column(name = "FECHA_INICIO", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "FECHA_FIN", nullable = true)
    private LocalDate fechaFin;

    @PrePersist
    protected void asignarValoresAutomaticos() {
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now(ZoneId.of("America/Lima"));
        }
    }

    public PersonalUbicacion() {
    }

    public Long getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(Long idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public Personal getPersona() {
        return persona;
    }

    public void setPersona(Personal persona) {
        this.persona = persona;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

}
