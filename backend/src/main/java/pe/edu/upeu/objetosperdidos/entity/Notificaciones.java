package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/** LEIDA: 0=no leida, 1=leida. El servicio crea un aviso por evento relevante, sin duplicaciones. */
@Entity
@Table(name = "NOTIFICACIONES")
public class Notificaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_notificaciones")
    @SequenceGenerator(name = "gen_notificaciones", sequenceName = "SEQ_NOTIFICACIONES", allocationSize = 1)
    @Column(name = "ID_NOTIFICACION", nullable = false)
    private Long idNotificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERSONA", nullable = false)
    private Usuario persona;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false, length = 40)
    private TipoNotificacion tipo;

    @Column(name = "TITULO", nullable = false, length = 100)
    private String titulo;

    @Column(name = "MENSAJE", nullable = false, length = 255)
    private String mensaje;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "LEIDA", nullable = false, columnDefinition = "NUMBER(1)")
    private Integer leida = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_REPORTE_PERDIDA", nullable = true)
    private ReportePerdida reportePerdida;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_HALLAZGO", nullable = true)
    private ReporteHallazgos hallazgo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_CUSTODIA", nullable = true)
    private ObjetosCustodiados custodia;

    @PrePersist
    protected void asignarValoresAutomaticos() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now(ZoneId.of("America/Lima")).truncatedTo(ChronoUnit.SECONDS);
        }
    }

    public Notificaciones() {
    }

    public Long getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(Long idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public Usuario getPersona() {
        return persona;
    }

    public void setPersona(Usuario persona) {
        this.persona = persona;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacion tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getLeida() {
        return leida;
    }

    public void setLeida(Integer leida) {
        this.leida = leida;
    }

    public ReportePerdida getReportePerdida() {
        return reportePerdida;
    }

    public void setReportePerdida(ReportePerdida reportePerdida) {
        this.reportePerdida = reportePerdida;
    }

    public ReporteHallazgos getHallazgo() {
        return hallazgo;
    }

    public void setHallazgo(ReporteHallazgos hallazgo) {
        this.hallazgo = hallazgo;
    }

    public ObjetosCustodiados getCustodia() {
        return custodia;
    }

    public void setCustodia(ObjetosCustodiados custodia) {
        this.custodia = custodia;
    }

}
