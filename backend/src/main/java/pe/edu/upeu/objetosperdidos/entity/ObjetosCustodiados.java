package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "OBJETOS_CUSTODIADOS")
public class ObjetosCustodiados {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_OBJETO")
    private Long idObjeto;

    @OneToOne
    @JoinColumn(name = "ID_HALLAZGO")
    private ReporteHallazgos hallazgo;

    @ManyToOne
    @JoinColumn(name = "ID_CATEGORIA")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "ID_UBICACION_ENCONTRADO")
    private Ubicacion ubicacionEncontrado;

    @ManyToOne
    @JoinColumn(name = "ID_UBICACION_ACTUAL")
    private Ubicacion ubicacionActual;

    @ManyToOne
    @JoinColumn(name = "ID_PERSONAL_REGISTRA")
    private Personal personalRegistra;

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

    @Column(name = "FECHA_REGISTRO", updatable = false)
    private LocalDateTime fechaRegistro;

    public ObjetosCustodiados() {
    }

    public ObjetosCustodiados(
            ReporteHallazgos hallazgo,
            Categoria categoria,
            Ubicacion ubicacionEncontrado,
            Ubicacion ubicacionActual,
            Personal personalRegistra,
            String nombre,
            String marca,
            String color,
            String descripcion,
            String imagen) {

        this.hallazgo = hallazgo;
        this.categoria = categoria;
        this.ubicacionEncontrado = ubicacionEncontrado;
        this.ubicacionActual = ubicacionActual;
        this.personalRegistra = personalRegistra;
        this.nombre = nombre;
        this.marca = marca;
        this.color = color;
        this.descripcion = descripcion;
        this.imagen = imagen;
    }

    @PrePersist
    public void registrarFecha() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    public Long getIdObjeto() {
        return idObjeto;
    }

    public void setIdObjeto(Long idObjeto) {
        this.idObjeto = idObjeto;
    }

    public ReporteHallazgos getHallazgo() {
        return hallazgo;
    }

    public void setHallazgo(ReporteHallazgos hallazgo) {
        this.hallazgo = hallazgo;
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
