package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "COINCIDENCIA", uniqueConstraints = @UniqueConstraint(name = "UK_COINCIDENCIA_PAR", columnNames = {"ID_HALLAZGO", "ID_REPORTE_PERDIDA"}))
public class Coincidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_coincidencia")
    @SequenceGenerator(name = "gen_coincidencia", sequenceName = "SEQ_COINCIDENCIA", allocationSize = 1)
    @Column(name = "ID_COINCIDENCIA", nullable = false)
    private Long idCoincidencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_HALLAZGO", nullable = false)
    private ReporteHallazgos hallazgo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_REPORTE_PERDIDA", nullable = false)
    private ReportePerdida reportePerdida;

    @Column(name = "PUNTAJE", nullable = false, columnDefinition = "NUMBER(3)")
    private Integer puntaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoCoincidencia estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERSONAL_REVISOR", nullable = false)
    private Personal personalRevisor;

    @Column(name = "FECHA_REVISION", nullable = false)
    private LocalDateTime fechaRevision;

    @Column(name = "OBSERVACION", nullable = false, length = 255)
    private String observacion;

    @PrePersist
    protected void asignarValoresAutomaticos() {
        if (fechaRevision == null) {
            fechaRevision = LocalDateTime.now(ZoneId.of("America/Lima")).truncatedTo(ChronoUnit.SECONDS);
        }
    }

    public Coincidencia() {
    }

    public Long getIdCoincidencia() {
        return idCoincidencia;
    }

    public void setIdCoincidencia(Long idCoincidencia) {
        this.idCoincidencia = idCoincidencia;
    }

    public ReporteHallazgos getHallazgo() {
        return hallazgo;
    }

    public void setHallazgo(ReporteHallazgos hallazgo) {
        this.hallazgo = hallazgo;
    }

    public ReportePerdida getReportePerdida() {
        return reportePerdida;
    }

    public void setReportePerdida(ReportePerdida reportePerdida) {
        this.reportePerdida = reportePerdida;
    }

    public Integer getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Integer puntaje) {
        this.puntaje = puntaje;
    }

    public EstadoCoincidencia getEstado() {
        return estado;
    }

    public void setEstado(EstadoCoincidencia estado) {
        this.estado = estado;
    }

    public Personal getPersonalRevisor() {
        return personalRevisor;
    }

    public void setPersonalRevisor(Personal personalRevisor) {
        this.personalRevisor = personalRevisor;
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
