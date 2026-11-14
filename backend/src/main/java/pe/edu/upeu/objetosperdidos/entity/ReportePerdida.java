package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "REPORTE_PERDIDA")
public class ReportePerdida {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_reporte_perdida")
    @SequenceGenerator(name = "gen_reporte_perdida", sequenceName = "SEQ_REPORTE_PERDIDA", allocationSize = 1)
    @Column(name = "ID_REPORTE_PERDIDA", nullable = false)
    private Long idReportePerdida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERSONA", nullable = false)
    private Persona persona;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_OBJETO", nullable = false, unique = true)
    private Objeto objeto;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_UBICACION_PERDIDA", nullable = true)
    private Ubicacion ubicacionPerdida;

    @Column(name = "IMAGEN", nullable = true, length = 255)
    private String imagen;

    @Column(name = "FECHA_PERDIDA", nullable = true)
    private LocalDate fechaPerdida;

    @Column(name = "FECHA_REPORTE", nullable = false)
    private LocalDateTime fechaReporte;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoReportePerdida estado = EstadoReportePerdida.REPORTADO;

    @PrePersist
    protected void asignarValoresAutomaticos() {
        if (fechaReporte == null) {
            fechaReporte = LocalDateTime.now(ZoneId.of("America/Lima")).truncatedTo(ChronoUnit.SECONDS);
        }
    }

    public ReportePerdida() {
    }

    public Long getIdReportePerdida() {
        return idReportePerdida;
    }

    public void setIdReportePerdida(Long idReportePerdida) {
        this.idReportePerdida = idReportePerdida;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public Objeto getObjeto() {
        return objeto;
    }

    public void setObjeto(Objeto objeto) {
        this.objeto = objeto;
    }

    public Ubicacion getUbicacionPerdida() {
        return ubicacionPerdida;
    }

    public void setUbicacionPerdida(Ubicacion ubicacionPerdida) {
        this.ubicacionPerdida = ubicacionPerdida;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public LocalDate getFechaPerdida() {
        return fechaPerdida;
    }

    public void setFechaPerdida(LocalDate fechaPerdida) {
        this.fechaPerdida = fechaPerdida;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDateTime fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public EstadoReportePerdida getEstado() {
        return estado;
    }

    public void setEstado(EstadoReportePerdida estado) {
        this.estado = estado;
    }

}
