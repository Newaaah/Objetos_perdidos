package pe.edu.upeu.objetosperdidos.services;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.dto.CrearReportePerdidaRequest;
import pe.edu.upeu.objetosperdidos.dto.MiPerdidaDTO;
import pe.edu.upeu.objetosperdidos.dto.ReportePerdidaCreadoDTO;
import pe.edu.upeu.objetosperdidos.entity.Categoria;
import pe.edu.upeu.objetosperdidos.entity.Objeto;
import pe.edu.upeu.objetosperdidos.entity.Persona;
import pe.edu.upeu.objetosperdidos.entity.ReportePerdida;
import pe.edu.upeu.objetosperdidos.entity.Ubicacion;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.repository.CategoriaRepository;
import pe.edu.upeu.objetosperdidos.repository.ObjetoRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonaRepository;
import pe.edu.upeu.objetosperdidos.repository.ReportePerdidaRepository;
import pe.edu.upeu.objetosperdidos.repository.UbicacionRepository;

@Service
public class ReportePerdidaServiceImpl implements IReportePerdidaService {

    private final PersonaRepository personas;
    private final CategoriaRepository categorias;
    private final UbicacionRepository ubicaciones;
    private final ObjetoRepository objetos;
    private final ReportePerdidaRepository perdidas;
    private final IReporteEstadisticoService consultas;
    private final AvisosDeDominioService avisos;

    public ReportePerdidaServiceImpl(
            PersonaRepository personas,
            CategoriaRepository categorias,
            UbicacionRepository ubicaciones,
            ObjetoRepository objetos,
            ReportePerdidaRepository perdidas,
            IReporteEstadisticoService consultas,
            AvisosDeDominioService avisos) {
        this.personas = personas;
        this.categorias = categorias;
        this.ubicaciones = ubicaciones;
        this.objetos = objetos;
        this.perdidas = perdidas;
        this.consultas = consultas;
        this.avisos = avisos;
    }

    @Override
    @Transactional
    public ReportePerdidaCreadoDTO crear(CrearReportePerdidaRequest solicitud, Long idPersona) {
        // El identificador de persona proviene del JWT y nunca del JSON.
        Persona persona = personas.findById(idPersona)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la persona autenticada"));

        Categoria categoria = categorias.findById(solicitud.idCategoria())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria no encontrada con ID: " + solicitud.idCategoria()));

        Ubicacion ubicacion = null;
        if (solicitud.idUbicacionPerdida() != null) {
            ubicacion = ubicaciones.findById(solicitud.idUbicacionPerdida())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Ubicacion no encontrada con ID: " + solicitud.idUbicacionPerdida()));
        }

        LocalDate fechaPerdida = solicitud.fechaPerdida();
        if (fechaPerdida != null
                && fechaPerdida.isAfter(LocalDate.now(ZoneId.of("America/Lima")))) {
            throw new IllegalArgumentException("La fecha de perdida no puede ser futura");
        }

        Objeto objeto = new Objeto();
        objeto.setCategoria(categoria);
        objeto.setNombre(normalizarObligatorio(solicitud.nombre(), "nombre"));
        objeto.setMarca(normalizarOpcional(solicitud.marca()));
        objeto.setColor(normalizarOpcional(solicitud.color()));
        objeto.setDescripcion(normalizarObligatorio(solicitud.descripcion(), "descripcion"));
        objeto = objetos.saveAndFlush(objeto);

        ReportePerdida reporte = new ReportePerdida();
        reporte.setPersona(persona);
        reporte.setObjeto(objeto);
        reporte.setUbicacionPerdida(ubicacion);
        reporte.setFechaPerdida(fechaPerdida);
        // El @PrePersist de ReportePerdida asigna fechaReporte;
        // la entidad ya inicializa el estado REPORTADO.
        reporte = perdidas.saveAndFlush(reporte);

        avisos.perdidaRegistrada(reporte);

        return new ReportePerdidaCreadoDTO(
                reporte.getIdReportePerdida(),
                objeto.getIdObjeto(),
                persona.getIdPersona(),
                objeto.getNombre(),
                categoria.getIdCategoria(),
                ubicacion == null ? null : ubicacion.getIdUbicacion(),
                reporte.getFechaPerdida(),
                reporte.getFechaReporte(),
                reporte.getEstado()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiPerdidaDTO> misPerdidas(Long idPersona) {
        // Reutiliza el metodo existente de consulta por propietario.
        return consultas.misPerdidas(idPersona);
    }

    private String normalizarObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        return valor.strip();
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }
}
