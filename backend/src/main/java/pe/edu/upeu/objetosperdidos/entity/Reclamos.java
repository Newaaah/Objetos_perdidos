package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/** Reclamo y incidencia comparten tabla. Requiere al menos una referencia a hallazgo, custodia o entrega. */
@Entity
@Table(name = "RECLAMOS")
public class Reclamos {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_reclamos")
    @SequenceGenerator(name = "gen_reclamos", sequenceName = "SEQ_RECLAMOS", allocationSize = 1)
    @Column(name = "ID_RECLAMO", nullable = false)
    private Long idReclamo;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_RECLAMO", nullable = false, length = 20)
    private TipoReclamo tipoReclamo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_ENTREGA", nullable = true)
    private Entrega entrega;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_CUSTODIA", nullable = true)
    private ObjetosCustodiados custodia;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_PERSONA_RECLAMA", nullable = true)
    private Persona personaReclama;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_PERSONAL_REGISTRA", nullable = true)
    private Personal personalRegistra;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_HALLAZGO", nullable = true)
    private ReporteHallazgos hallazgo;

    @Column(name = "EVIDENCIA", nullable = true, length = 255)
    private String evidencia;

    @Column(name = "DESCRIPCION", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "RESOLUCION", nullable = true, length = 500)
    private String resolucion;

    @Column(name = "FECHA_RESOLUCION", nullable = true)
    private LocalDateTime fechaResolucion;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoReclamo estado = EstadoReclamo.PENDIENTE;

    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void asignarValoresAutomaticos() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now(ZoneId.of("America/Lima")).truncatedTo(ChronoUnit.SECONDS);
        }
    }

    public Reclamos() {
    }

    public Long getIdReclamo() {
        return idReclamo;
    }

    public void setIdReclamo(Long idReclamo) {
        this.idReclamo = idReclamo;
    }

    public TipoReclamo getTipoReclamo() {
        return tipoReclamo;
    }

    public void setTipoReclamo(TipoReclamo tipoReclamo) {
        this.tipoReclamo = tipoReclamo;
    }

    public Entrega getEntrega() {
        return entrega;
    }

    public void setEntrega(Entrega entrega) {
        this.entrega = entrega;
    }

    public ObjetosCustodiados getCustodia() {
        return custodia;
    }

    public void setCustodia(ObjetosCustodiados custodia) {
        this.custodia = custodia;
    }

    public Persona getPersonaReclama() {
        return personaReclama;
    }

    public void setPersonaReclama(Persona personaReclama) {
        this.personaReclama = personaReclama;
    }

    public Personal getPersonalRegistra() {
        return personalRegistra;
    }

    public void setPersonalRegistra(Personal personalRegistra) {
        this.personalRegistra = personalRegistra;
    }

    public ReporteHallazgos getHallazgo() {
        return hallazgo;
    }

    public void setHallazgo(ReporteHallazgos hallazgo) {
        this.hallazgo = hallazgo;
    }

    public String getEvidencia() {
        return evidencia;
    }

    public void setEvidencia(String evidencia) {
        this.evidencia = evidencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDateTime fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public EstadoReclamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoReclamo estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

}
