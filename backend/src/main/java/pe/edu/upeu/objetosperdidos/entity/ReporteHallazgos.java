package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "REPORTE_HALLAZGOS")
public class ReporteHallazgos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HALLAZGO")
    private Long idHallazgo;

    @ManyToOne
    @JoinColumn(name = "ID_PERSONA")
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "ID_CATEGORIA")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "ID_UBICACION_ENCONTRADO")
    private Ubicacion ubicacionEncontrado;

    @ManyToOne
    @JoinColumn(name = "ID_UBICACION_DESTINO")
    private Ubicacion ubicacionDestino;

    @Column(name = "NOMBRE", length = 50)
    private String nombre;

    @Column(name = "MARCA", length = 100)
    private String marca;

    @Column(name = "COLOR", length = 50)
    private String color;

    @Column(name = "DESCRIPCION", length = 255)
    private String descripcion;

    @Column(name = "IMAGEN_REF", length = 255)
    private String imagenRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 20)
    private EstadoReporteHallazgos estado;

    @Column(name = "FECHA_REPORTE", updatable = false)
    private LocalDateTime fechaReporte;

    @ManyToOne
    @JoinColumn(name = "ID_PERSONA_REVISOR")
    private Persona revisor;

    @Column(name = "FECHA_REVISION")
    private LocalDateTime fechaRevision;

    @Column(name = "OBSERVACION", length = 255)
    private String observacion;

    public ReporteHallazgos() {
    }

    public ReporteHallazgos(Persona persona,
                            Categoria categoria,
                            Ubicacion ubicacionEncontrado,
                            Ubicacion ubicacionDestino,
                            String nombre,
                            String marca,
                            String color,
                            String descripcion,
                            String imagenRef,
                            EstadoReporteHallazgos estado) {
        this.persona = persona;
        this.categoria = categoria;
        this.ubicacionEncontrado = ubicacionEncontrado;
        this.ubicacionDestino = ubicacionDestino;
        this.nombre = nombre;
        this.marca = marca;
        this.color = color;
        this.descripcion = descripcion;
        this.imagenRef = imagenRef;
        this.estado = estado;
    }

    @PrePersist
    public void registrarFecha() {
        if (fechaReporte == null) {
            fechaReporte = LocalDateTime.now();
        }
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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenRef() {
        return imagenRef;
    }

    public void setImagenRef(String imagenRef) {
        this.imagenRef = imagenRef;
    }

    public EstadoReporteHallazgos getEstado() {
        return estado;
    }

    public void setEstado(EstadoReporteHallazgos estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public Persona getRevisor() {
        return revisor;
    }

    public void setRevisor(Persona revisor) {
        this.revisor = revisor;
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
