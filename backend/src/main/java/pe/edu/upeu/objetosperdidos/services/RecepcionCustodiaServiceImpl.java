package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.objetosperdidos.dto.CustodiaRegistradaDTO;
import pe.edu.upeu.objetosperdidos.dto.PendienteRecepcionCustodiaDTO;
import pe.edu.upeu.objetosperdidos.dto.RecepcionCustodiaRequest;
import pe.edu.upeu.objetosperdidos.dto.RecepcionDirectaCustodiaRequest;
import pe.edu.upeu.objetosperdidos.entity.Categoria;
import pe.edu.upeu.objetosperdidos.entity.EstadoPersonal;
import pe.edu.upeu.objetosperdidos.entity.EstadoReporteHallazgo;
import pe.edu.upeu.objetosperdidos.entity.Objeto;
import pe.edu.upeu.objetosperdidos.entity.ObjetosCustodiados;
import pe.edu.upeu.objetosperdidos.entity.Persona;
import pe.edu.upeu.objetosperdidos.entity.Personal;
import pe.edu.upeu.objetosperdidos.entity.ReporteHallazgos;
import pe.edu.upeu.objetosperdidos.entity.Ubicacion;
import pe.edu.upeu.objetosperdidos.exception.ResourceInUseException;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.repository.CategoriaRepository;
import pe.edu.upeu.objetosperdidos.repository.ObjetoRepository;
import pe.edu.upeu.objetosperdidos.repository.ObjetosCustodiadosRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonaRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonalRepository;
import pe.edu.upeu.objetosperdidos.repository.ReporteHallazgosRepository;
import pe.edu.upeu.objetosperdidos.repository.UbicacionRepository;

@Service
public class RecepcionCustodiaServiceImpl implements IRecepcionCustodiaService {

    private final ReporteHallazgosRepository hallazgos;
    private final ObjetosCustodiadosRepository custodias;
    private final PersonalRepository personal;
    private final PersonaRepository personas;
    private final UbicacionRepository ubicaciones;
    private final CategoriaRepository categorias;
    private final ObjetoRepository objetos;
    private final IFileStorageService imagenes;
    private final AvisosDeDominioService avisos;

    public RecepcionCustodiaServiceImpl(
            ReporteHallazgosRepository hallazgos,
            ObjetosCustodiadosRepository custodias,
            PersonalRepository personal,
            PersonaRepository personas,
            UbicacionRepository ubicaciones,
            CategoriaRepository categorias,
            ObjetoRepository objetos,
            IFileStorageService imagenes,
            AvisosDeDominioService avisos) {
        this.hallazgos = hallazgos;
        this.custodias = custodias;
        this.personal = personal;
        this.personas = personas;
        this.ubicaciones = ubicaciones;
        this.categorias = categorias;
        this.objetos = objetos;
        this.imagenes = imagenes;
        this.avisos = avisos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendienteRecepcionCustodiaDTO> pendientes() {
        return hallazgos.buscarSinCustodiaPorEstado(EstadoReporteHallazgo.APROBADO)
                .stream()
                .map(h -> new PendienteRecepcionCustodiaDTO(
                        h.getIdHallazgo(), h.getObjeto().getIdObjeto(),
                        h.getObjeto().getNombre(),
                        h.getUbicacionDestino().getIdUbicacion(),
                        h.getUbicacionDestino().getNombre(), h.getFechaReporte()))
                .toList();
    }

    @Override
    @Transactional
    public CustodiaRegistradaDTO recibir(
            RecepcionCustodiaRequest solicitud, Long idPersonaPersonal) {
        return recibirConImagen(solicitud, null, idPersonaPersonal);
    }

    @Override
    @Transactional
    public CustodiaRegistradaDTO recibirConImagen(
            RecepcionCustodiaRequest solicitud, MultipartFile imagen,
            Long idPersonaPersonal) {
        if (solicitud == null) {
            throw new IllegalArgumentException("Los datos de recepcion son obligatorios");
        }
        validarConfirmacion(solicitud.confirmoRecepcionFisica());
        validarId(solicitud.idHallazgo(), "hallazgo");
        validarId(solicitud.idUbicacionActual(), "ubicacion actual");
        Personal receptor = obtenerPersonal(idPersonaPersonal);

        // Esta misma fila se bloquea durante la comprobacion y el INSERT.
        ReporteHallazgos hallazgo = hallazgos
                .bloquearParaRecepcion(solicitud.idHallazgo())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hallazgo no encontrado con ID: " + solicitud.idHallazgo()));
        if (hallazgo.getEstado() != EstadoReporteHallazgo.APROBADO) {
            throw new ResourceInUseException(
                    "Solo se puede recibir un hallazgo APROBADO; estado: " + hallazgo.getEstado());
        }
        if (custodias.findByHallazgoIdHallazgo(hallazgo.getIdHallazgo()).isPresent()) {
            throw new ResourceInUseException("El hallazgo ya tiene una custodia registrada");
        }

        Ubicacion ubicacion = buscarUbicacion(solicitud.idUbicacionActual());
        Persona entrega = buscarEntrega(solicitud.idPersonaEntrega());
        String observacion = normalizarOpcional(solicitud.observacionRecepcion());
        validarLongitud(observacion, 255, "observacionRecepcion");

        // Una imagen nueva de recepcion es opcional si la original es accesible.
        boolean imagenNueva = imagen != null && !imagen.isEmpty();
        if (!imagenNueva) {
            try {
                imagenes.load(hallazgo.getImagenRef());
            } catch (ResourceNotFoundException ex) {
                throw new IllegalArgumentException(
                        "El hallazgo no tiene una fotografia accesible; adjunta una nueva");
            }
        }
        String refNueva = imagenNueva ? guardarYAsociarLimpieza(imagen) : null;

        ObjetosCustodiados custodia = new ObjetosCustodiados();
        custodia.setHallazgo(hallazgo);
        custodia.setObjeto(hallazgo.getObjeto());
        custodia.setUbicacionEncontrado(hallazgo.getUbicacionEncontrado());
        custodia.setUbicacionActual(ubicacion);
        custodia.setPersonalRegistra(receptor);
        custodia.setPersonaEntrega(entrega);
        custodia.setObservacionRecepcion(observacion);
        custodia.setImagen(refNueva); // sin foto nueva, se consulta la del hallazgo.
        ObjetosCustodiados guardada = custodias.saveAndFlush(custodia);
        avisos.custodiaRecibida(guardada);
        return aRespuesta(guardada);
    }

    @Override
    @Transactional
    public CustodiaRegistradaDTO recibirDirectamente(
            RecepcionDirectaCustodiaRequest solicitud, MultipartFile imagen,
            Long idPersonaPersonal) {
        if (solicitud == null) {
            throw new IllegalArgumentException("Los datos de recepcion son obligatorios");
        }
        validarConfirmacion(solicitud.confirmoRecepcionFisica());
        validarId(solicitud.idCategoria(), "categoria");
        validarId(solicitud.idUbicacionActual(), "ubicacion actual");

        Personal receptor = obtenerPersonal(idPersonaPersonal);
        Categoria categoria = categorias.findById(solicitud.idCategoria())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria no encontrada con ID: " + solicitud.idCategoria()));
        Ubicacion ubicacion = buscarUbicacion(solicitud.idUbicacionActual());
        Ubicacion encontrado = null;
        if (solicitud.idUbicacionEncontrado() != null) {
            validarId(solicitud.idUbicacionEncontrado(), "ubicacion encontrada");
            encontrado = buscarUbicacion(solicitud.idUbicacionEncontrado());
        }
        Persona entrega = buscarEntrega(solicitud.idPersonaEntrega());
        String nombre = normalizarObligatorio(solicitud.nombre(), "nombre");
        String descripcion = normalizarObligatorio(solicitud.descripcion(), "descripcion");
        String marca = normalizarOpcional(solicitud.marca());
        String color = normalizarOpcional(solicitud.color());
        String observacion = normalizarOpcional(solicitud.observacionRecepcion());
        validarLongitud(nombre, 100, "nombre");
        validarLongitud(descripcion, 255, "descripcion");
        validarLongitud(marca, 100, "marca");
        validarLongitud(color, 50, "color");
        validarLongitud(observacion, 255, "observacionRecepcion");

        // Primero se valida/guarda la imagen; si Oracle revierte, se elimina.
        String referencia = guardarYAsociarLimpieza(imagen);

        Objeto objeto = new Objeto();
        objeto.setCategoria(categoria);
        objeto.setNombre(nombre);
        objeto.setMarca(marca);
        objeto.setColor(color);
        objeto.setDescripcion(descripcion);
        objeto = objetos.saveAndFlush(objeto);

        ObjetosCustodiados custodia = new ObjetosCustodiados();
        custodia.setHallazgo(null); // Recepcion DIRECTA, sin reporte previo.
        custodia.setObjeto(objeto);
        custodia.setUbicacionEncontrado(encontrado);
        custodia.setUbicacionActual(ubicacion);
        custodia.setPersonalRegistra(receptor);
        custodia.setPersonaEntrega(entrega);
        custodia.setObservacionRecepcion(observacion);
        custodia.setImagen(referencia);
        ObjetosCustodiados guardada = custodias.saveAndFlush(custodia);
        avisos.custodiaRecibida(guardada);
        return aRespuesta(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public CustodiaRegistradaDTO consultar(Long idCustodia) {
        validarId(idCustodia, "custodia");
        ObjetosCustodiados custodia = custodias.findById(idCustodia)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custodia no encontrada con ID: " + idCustodia));
        return aRespuesta(custodia);
    }

    @Override
    @Transactional(readOnly = true)
    public IFileStorageService.Imagen leerImagen(Long idCustodia) {
        validarId(idCustodia, "custodia");
        ObjetosCustodiados c = custodias.findById(idCustodia)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custodia no encontrada con ID: " + idCustodia));
        if (c.getImagen() != null && !c.getImagen().isBlank()) {
            return imagenes.load(c.getImagen());
        }
        if (c.getHallazgo() != null) {
            return imagenes.load(c.getHallazgo().getImagenRef());
        }
        throw new ResourceNotFoundException("Esta custodia no tiene fotografia disponible");
    }

    private String guardarYAsociarLimpieza(MultipartFile imagen) {
        // Se borra sola si la transaccion falla.
        return imagenes.storeEnTransaccion(imagen, IFileStorageService.CUSTODIAS);
    }

    private Personal obtenerPersonal(Long idPersona) {
        Personal receptor = personal.findById(idPersona)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe PERSONAL asociado a la sesion"));
        if (receptor.getEstado() != EstadoPersonal.ACTIVO) {
            throw new ResourceInUseException("El personal no esta activo");
        }
        return receptor;
    }

    private Ubicacion buscarUbicacion(Long id) {
        return ubicaciones.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ubicacion no encontrada con ID: " + id));
    }

    private Persona buscarEntrega(Long id) {
        if (id == null) return null;
        validarId(id, "persona que entrega");
        return personas.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Persona que entrega no encontrada con ID: " + id));
    }

    private CustodiaRegistradaDTO aRespuesta(ObjetosCustodiados c) {
        ReporteHallazgos h = c.getHallazgo();
        String referencia = c.getImagen() != null ? c.getImagen()
                : h == null ? null : h.getImagenRef();
        return new CustodiaRegistradaDTO(
                c.getIdCustodia(), h == null ? null : h.getIdHallazgo(),
                c.getObjeto().getIdObjeto(), c.getObjeto().getNombre(),
                c.getPersonalRegistra().getIdPersona(),
                c.getUbicacionActual().getIdUbicacion(),
                c.getUbicacionActual().getNombre(),
                c.getPersonaEntrega() == null ? null : c.getPersonaEntrega().getIdPersona(),
                c.getFechaRegistro(), c.getObservacionRecepcion(),
                h == null ? "DIRECTA" : "CON_HALLAZGO", referencia);
    }

    private static void validarConfirmacion(Boolean confirmado) {
        if (!Boolean.TRUE.equals(confirmado)) {
            throw new IllegalArgumentException("Debes confirmar la recepcion fisica");
        }
    }

    private static void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de " + campo + " debe ser positivo");
        }
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

    private static void validarLongitud(String valor, int max, String campo) {
        if (valor != null && valor.length() > max) {
            throw new IllegalArgumentException(
                    "El campo " + campo + " supera los " + max + " caracteres");
        }
    }
}
