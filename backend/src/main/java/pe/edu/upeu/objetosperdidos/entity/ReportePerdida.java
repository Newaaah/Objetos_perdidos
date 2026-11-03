package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "REPORTE_PERDIDA")
public class ReportePerdida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_REPORTE_PERDIDA")
    private Long idReportePerdida;

    @ManyToOne
    @JoinColumn(name = "ID_PERSONA")
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "ID_CATEGORIA")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "ID_UBICACION_PERDIDA")
    private Ubicacion ubicacionPerdida;

    @Column(name = "NOMBRE", length = 100)
    private String nombre;

    @Column(name = "MARCA", length = 50)
    private String marca;

    @Column(name = "COLOR", length = 50)
    private String color;

    @Column(name = "DESCRIPCION", length = 255)
    private String descripcion;

    @Column(name = "IMAGEN", length = 255)
    private String imagen;

    @Column(name = "FECHA_PERDIDA")
    private LocalDate fechaPerdida;

    @Column(name = "FECHA_REPORTE", updatable = false)
    private LocalDateTime fechaReporte;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 20)
    private EstadoReportePerdida estado;

    public ReportePerdida() {
    }

    @PrePersist
    public void registrarFecha() {
        if (fechaReporte == null) {
            fechaReporte = LocalDateTime.now();
        }
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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Ubicacion getUbicacionPerdida() {
        return ubicacionPerdida;
    }

    public void setUbicacionPerdida(Ubicacion ubicacionPerdida) {
        this.ubicacionPerdida = ubicacionPerdida;
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

    public EstadoReportePerdida getEstado() {
        return estado;
    }

    public void setEstado(EstadoReportePerdida estado) {
        this.estado = estado;
    }
}
