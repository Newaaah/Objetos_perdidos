package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/** La revision se asocia a PERSONAL mediante ID_PERSONA_REVISOR. Custodia anticipada no equivale a aprobar la publicacion. */
@Entity
@Table(name = "REPORTE_HALLAZGOS")
public class ReporteHallazgos {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_reporte_hallazgos")
    @SequenceGenerator(name = "gen_reporte_hallazgos", sequenceName = "SEQ_REPORTE_HALLAZGOS", allocationSize = 1)
    @Column(name = "ID_HALLAZGO", nullable = false)
    private Long idHallazgo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERSONA", nullable = false)
    private Persona persona;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_OBJETO", nullable = false, unique = true)
    private Objeto objeto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_UBICACION_ENCONTRADO", nullable = false)
    private Ubicacion ubicacionEncontrado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_UBICACION_DESTINO", nullable = false)
    private Ubicacion ubicacionDestino;

    @Column(name = "IMAGEN_REF", nullable = false, length = 255)
    private String imagenRef;

    @Column(name = "FECHA_REPORTE", nullable = false)
    private LocalDateTime fechaReporte;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_PERSONA_REVISOR", nullable = true)
    private Personal personaRevisor;

    @Column(name = "FECHA_REVISION", nullable = true)
    private LocalDateTime fechaRevision;

    @Column(name = "OBSERVACION", nullable = true, length = 255)
    private String observacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoReporteHallazgo estado = EstadoReporteHallazgo.PENDIENTE;

    @PrePersist
    protected void asignarValoresAutomaticos() {
        if (fechaReporte == null) {
            fechaReporte = LocalDateTime.now(ZoneId.of("America/Lima")).truncatedTo(ChronoUnit.SECONDS);
        }
    }

    public ReporteHallazgos() {
    }

    public Long getIdHallazgo() {
        return idHallazgo;
    }

    public void setIdHallazgo(Long idHallazgo) {
        this.idHallazgo = idHallazgo;
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

    public Ubicacion getUbicacionEncontrado() {
        return ubicacionEncontrado;
    }

    public void setUbicacionEncontrado(Ubicacion ubicacionEncontrado) {
        this.ubicacionEncontrado = ubicacionEncontrado;
    }

    public Ubicacion getUbicacionDestino() {
        return ubicacionDestino;
    }

    public void setUbicacionDestino(Ubicacion ubicacionDestino) {
        this.ubicacionDestino = ubicacionDestino;
    }

    public String getImagenRef() {
        return imagenRef;
    }

    public void setImagenRef(String imagenRef) {
        this.imagenRef = imagenRef;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDateTime fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public Personal getPersonaRevisor() {
        return personaRevisor;
    }

    public void setPersonaRevisor(Personal personaRevisor) {
        this.personaRevisor = personaRevisor;
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

    public EstadoReporteHallazgo getEstado() {
        return estado;
    }

    public void setEstado(EstadoReporteHallazgo estado) {
        this.estado = estado;
    }

}
