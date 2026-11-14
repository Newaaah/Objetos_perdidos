package pe.edu.upeu.objetosperdidos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PERSONA", uniqueConstraints = @UniqueConstraint(name = "UK_PERSONA_DOCUMENTO", columnNames = {"TIPO_DOCUMENTO", "DOCUMENTO_IDENTIDAD"}))
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_persona")
    @SequenceGenerator(name = "gen_persona", sequenceName = "SEQ_PERSONA", allocationSize = 1)
    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(name = "TIPO_DOCUMENTO", nullable = false, length = 50)
    private String tipoDocumento;

    @Column(name = "DOCUMENTO_IDENTIDAD", nullable = false, length = 20)
    private String documentoIdentidad;

    @Column(name = "NOMBRES", nullable = false, length = 50)
    private String nombres;

    @Column(name = "APELLIDOS", nullable = false, length = 50)
    private String apellidos;

    @Column(name = "TELEFONO", nullable = true, length = 50)
    private String telefono;

    public Persona() {
    }

    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

}
