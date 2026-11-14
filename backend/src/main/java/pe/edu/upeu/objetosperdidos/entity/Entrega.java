package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/** FOTO_EVIDENCIA y OBSERVACION_VERIFICACION son obligatorias. La entrega solo se confirma cuando la evidencia se almacena. */
@Entity
@Table(name = "ENTREGA")
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_entrega")
    @SequenceGenerator(name = "gen_entrega", sequenceName = "SEQ_ENTREGA", allocationSize = 1)
    @Column(name = "ID_ENTREGA", nullable = false)
    private Long idEntrega;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_COINCIDENCIA", nullable = true, unique = true)
    private Coincidencia coincidencia;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CUSTODIA", nullable = false, unique = true)
    private ObjetosCustodiados custodia;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_REPORTE_PERDIDA", nullable = true, unique = true)
    private ReportePerdida reportePerdida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERSONAL", nullable = false)
    private Personal personal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERSONA", nullable = false)
    private Persona persona;

    @Column(name = "FOTO_EVIDENCIA", nullable = false, length = 255)
    private String fotoEvidencia;

    @Column(name = "OBSERVACION_VERIFICACION", nullable = false, length = 255)
    private String observacionVerificacion;

    @Column(name = "FECHA_ENTREGA", nullable = false)
    private LocalDateTime fechaEntrega;

    @PrePersist
    protected void asignarValoresAutomaticos() {
        if (fechaEntrega == null) {
            fechaEntrega = LocalDateTime.now(ZoneId.of("America/Lima")).truncatedTo(ChronoUnit.SECONDS);
        }
    }

    public Entrega() {
    }

    public Long getIdEntrega() {
        return idEntrega;
    }

    public void setIdEntrega(Long idEntrega) {
        this.idEntrega = idEntrega;
    }

    public Coincidencia getCoincidencia() {
        return coincidencia;
    }

    public void setCoincidencia(Coincidencia coincidencia) {
        this.coincidencia = coincidencia;
    }

    public ObjetosCustodiados getCustodia() {
        return custodia;
    }

    public void setCustodia(ObjetosCustodiados custodia) {
        this.custodia = custodia;
    }

    public ReportePerdida getReportePerdida() {
        return reportePerdida;
    }

    public void setReportePerdida(ReportePerdida reportePerdida) {
        this.reportePerdida = reportePerdida;
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

    public String getFotoEvidencia() {
        return fotoEvidencia;
    }

    public void setFotoEvidencia(String fotoEvidencia) {
        this.fotoEvidencia = fotoEvidencia;
    }

    public String getObservacionVerificacion() {
        return observacionVerificacion;
    }

    public void setObservacionVerificacion(String observacionVerificacion) {
        this.observacionVerificacion = observacionVerificacion;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

}
