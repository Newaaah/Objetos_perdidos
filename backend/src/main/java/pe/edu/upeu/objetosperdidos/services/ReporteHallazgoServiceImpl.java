package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.objetosperdidos.dto.CrearReporteHallazgoRequest;
import pe.edu.upeu.objetosperdidos.dto.MiHallazgoDTO;
import pe.edu.upeu.objetosperdidos.dto.ReporteHallazgoCreadoDTO;
import pe.edu.upeu.objetosperdidos.entity.Categoria;
import pe.edu.upeu.objetosperdidos.entity.EstadoReporteHallazgo;
import pe.edu.upeu.objetosperdidos.entity.Objeto;
import pe.edu.upeu.objetosperdidos.entity.Persona;
import pe.edu.upeu.objetosperdidos.entity.ReporteHallazgos;
import pe.edu.upeu.objetosperdidos.entity.Ubicacion;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.repository.CategoriaRepository;
import pe.edu.upeu.objetosperdidos.repository.ObjetoRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonaRepository;
import pe.edu.upeu.objetosperdidos.repository.ReporteHallazgosRepository;
import pe.edu.upeu.objetosperdidos.repository.UbicacionRepository;

@Service
public class ReporteHallazgoServiceImpl implements IReporteHallazgoService {

    private final PersonaRepository personas;
    private final CategoriaRepository categorias;
    private final UbicacionRepository ubicaciones;
    private final ObjetoRepository objetos;
    private final ReporteHallazgosRepository hallazgos;
    private final IReporteEstadisticoService consultas;
    private final AlmacenamientoImagenHallazgoService imagenes;

    public ReporteHallazgoServiceImpl(
            PersonaRepository personas,
            CategoriaRepository categorias,
            UbicacionRepository ubicaciones,
            ObjetoRepository objetos,
            ReporteHallazgosRepository hallazgos,
            IReporteEstadisticoService consultas,
            AlmacenamientoImagenHallazgoService imagenes) {
        this.personas = personas;
        this.categorias = categorias;
        this.ubicaciones = ubicaciones;
        this.objetos = objetos;
        this.hallazgos = hallazgos;
        this.consultas = consultas;
        this.imagenes = imagenes;
    }

    @Override
    @Transactional
    public ReporteHallazgoCreadoDTO crear(
            CrearReporteHallazgoRequest datos,
            MultipartFile imagen,
            Long idPersona) {

        // La identidad proviene de la sesion autenticada, nunca del JSON.
        Persona persona = personas.findById(idPersona)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la persona autenticada"));

        Categoria categoria = categorias.findById(datos.idCategoria())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria no encontrada con ID: " + datos.idCategoria()));

        Ubicacion encontrado = ubicaciones.findById(datos.idUbicacionEncontrado())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ubicacion de hallazgo no encontrada con ID: "
                                + datos.idUbicacionEncontrado()));

        Ubicacion destino = ubicaciones.findById(datos.idUbicacionDestino())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ubicacion de destino no encontrada con ID: "
                                + datos.idUbicacionDestino()));

        String nombre = normalizarObligatorio(datos.nombre(), "nombre");
        String descripcion = normalizarObligatorio(datos.descripcion(), "descripcion");

        // El archivo se guarda ANTES del INSERT. Se borra si la transaccion
        // de Oracle falla o es revertida, para no dejar imagenes huerfanas.
        String imagenRef = imagenes.guardar(imagen);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            imagenes.eliminarTrasRollback(imagenRef);
            throw new IllegalStateException("No hay una transaccion activa");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int estado) {
                if (estado != TransactionSynchronization.STATUS_COMMITTED) {
                    imagenes.eliminarTrasRollback(imagenRef);
                }
            }
        });

        Objeto objeto = new Objeto();
        objeto.setCategoria(categoria);
        objeto.setNombre(nombre);
        objeto.setMarca(normalizarOpcional(datos.marca()));
        objeto.setColor(normalizarOpcional(datos.color()));
        objeto.setDescripcion(descripcion);
        objeto = objetos.saveAndFlush(objeto);

        ReporteHallazgos reporte = new ReporteHallazgos();
        reporte.setPersona(persona);
        reporte.setObjeto(objeto);
        reporte.setUbicacionEncontrado(encontrado);
        reporte.setUbicacionDestino(destino);
        reporte.setImagenRef(imagenRef);
        reporte.setEstado(EstadoReporteHallazgo.PENDIENTE);
        // No se registra automaticamente custodia ni aprobacion.
        reporte = hallazgos.saveAndFlush(reporte);

        return new ReporteHallazgoCreadoDTO(
                reporte.getIdHallazgo(),
                objeto.getIdObjeto(),
                persona.getIdPersona(),
                objeto.getNombre(),
                categoria.getIdCategoria(),
                encontrado.getIdUbicacion(),
                destino.getIdUbicacion(),
                reporte.getImagenRef(),
                reporte.getFechaReporte(),
                reporte.getEstado());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiHallazgoDTO> misHallazgos(Long idPersona) {
        return consultas.misHallazgos(idPersona);
    }

    private static String normalizarObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        return valor.strip();
    }

    private static String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }
}
