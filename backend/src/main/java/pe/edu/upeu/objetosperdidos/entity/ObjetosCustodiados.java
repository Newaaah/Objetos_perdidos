package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/** IMAGEN es opcional solo si existe un hallazgo con IMAGEN_REF; sin hallazgo, el servicio debe exigir una fotografia. */
@Entity
@Table(name = "OBJETOS_CUSTODIADOS")
public class ObjetosCustodiados {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_objetos_custodiados")
    @SequenceGenerator(name = "gen_objetos_custodiados", sequenceName = "SEQ_OBJETOS_CUSTODIADOS", allocationSize = 1)
    @Column(name = "ID_CUSTODIA", nullable = false)
    private Long idCustodia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_OBJETO", nullable = false)
    private Objeto objeto;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_HALLAZGO", nullable = true, unique = true)
    private ReporteHallazgos hallazgo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_UBICACION_ENCONTRADO", nullable = true)
    private Ubicacion ubicacionEncontrado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_UBICACION_ACTUAL", nullable = false)
    private Ubicacion ubicacionActual;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERSONAL_REGISTRA", nullable = false)
    private Personal personalRegistra;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_PERSONA_ENTREGA", nullable = true)
    private Persona personaEntrega;

    @Column(name = "IMAGEN", nullable = true, length = 255)
    private String imagen;

    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "OBSERVACION_RECEPCION", nullable = true, length = 255)
    private String observacionRecepcion;

    @PrePersist
    protected void asignarValoresAutomaticos() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now(ZoneId.of("America/Lima")).truncatedTo(ChronoUnit.SECONDS);
        }
    }

    public ObjetosCustodiados() {
    }

    public Long getIdCustodia() {
        return idCustodia;
    }

    public void setIdCustodia(Long idCustodia) {
        this.idCustodia = idCustodia;
    }

    public Objeto getObjeto() {
        return objeto;
    }

    public void setObjeto(Objeto objeto) {
        this.objeto = objeto;
    }

    public ReporteHallazgos getHallazgo() {
        return hallazgo;
    }

    public void setHallazgo(ReporteHallazgos hallazgo) {
        this.hallazgo = hallazgo;
    }

    public Ubicacion getUbicacionEncontrado() {
        return ubicacionEncontrado;
    }

    public void setUbicacionEncontrado(Ubicacion ubicacionEncontrado) {
        this.ubicacionEncontrado = ubicacionEncontrado;
    }

    public Ubicacion getUbicacionActual() {
        return ubicacionActual;
    }

    public void setUbicacionActual(Ubicacion ubicacionActual) {
        this.ubicacionActual = ubicacionActual;
    }

    public Personal getPersonalRegistra() {
        return personalRegistra;
    }

    public void setPersonalRegistra(Personal personalRegistra) {
        this.personalRegistra = personalRegistra;
    }

    public Persona getPersonaEntrega() {
        return personaEntrega;
    }

    public void setPersonaEntrega(Persona personaEntrega) {
        this.personaEntrega = personaEntrega;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getObservacionRecepcion() {
        return observacionRecepcion;
    }

    public void setObservacionRecepcion(String observacionRecepcion) {
        this.observacionRecepcion = observacionRecepcion;
    }

}
