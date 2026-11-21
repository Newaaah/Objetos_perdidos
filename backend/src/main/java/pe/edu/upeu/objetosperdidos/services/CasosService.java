package pe.edu.upeu.objetosperdidos.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.objetosperdidos.dto.*;
import pe.edu.upeu.objetosperdidos.entity.*;
import pe.edu.upeu.objetosperdidos.exception.*;
import pe.edu.upeu.objetosperdidos.repository.*;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;

/**
 * RECLAMO: un estudiante cuestiona una entrega YA registrada
 *          (cree que el objeto entregado era suyo).
 *          PENDIENTE -> EN_REVISION -> APROBADO | RECHAZADO
 * INCIDENCIA: el personal documenta un problema mientras el objeto sigue en custodia.
 *          PENDIENTE -> EN_REVISION -> RESUELTO
 * Ninguna resolucion revierte automaticamente una entrega ni edita pruebas.
 */
@Service
public class CasosService {

    private static final List<EstadoReclamo> ABIERTOS =
            List.of(EstadoReclamo.PENDIENTE, EstadoReclamo.EN_REVISION);

    private final ReclamosRepository casos;
    private final EntregaRepository entregas;
    private final ObjetosCustodiadosRepository custodias;
    private final PersonaRepository personas;
    private final PersonalRepository personales;
    private final AlmacenamientoImagenReclamoService evidencias;
    private final AvisosDeDominioService avisos;

    public CasosService(ReclamosRepository casos, EntregaRepository entregas,
                        ObjetosCustodiadosRepository custodias, PersonaRepository personas,
                        PersonalRepository personales,
                        AlmacenamientoImagenReclamoService evidencias,
                        AvisosDeDominioService avisos) {
        this.casos = casos;
        this.entregas = entregas;
        this.custodias = custodias;
        this.personas = personas;
        this.personales = personales;
        this.evidencias = evidencias;
        this.avisos = avisos;
    }

    // ===================== REGISTRO =====================

    @Transactional
    public CasoRegistradoDTO reclamar(RegistrarReclamoRequest solicitud, MultipartFile evidencia,
                                      UsuarioPrincipal usuario) {
        if (usuario == null) throw new IllegalArgumentException("Sesion invalida");
        Long idPersona = usuario.idPersona();
        validarId(idPersona);
        // Solo estudiantes reclaman. PERSONAL tambien tiene la autoridad ESTUDIANTE,
        // por eso se valida el rol real del token.
        if (usuario.rol() != RolUsuario.ESTUDIANTE) {
            throw new ResourceInUseException("Solo los estudiantes pueden presentar reclamos");
        }
        if (solicitud == null) throw new IllegalArgumentException("Faltan datos del reclamo");
        validarId(solicitud.idEntrega());
        String descripcion = texto(solicitud.descripcion(), "descripcion", 15, 255);

        Entrega entrega = entregas.findById(solicitud.idEntrega())
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada"));
        Persona solicitante = personas.findById(idPersona)
                .orElseThrow(() -> new ResourceNotFoundException("Persona autenticada no encontrada"));

        if (entrega.getPersona() != null
                && idPersona.equals(entrega.getPersona().getIdPersona())) {
            throw new ResourceInUseException("No puedes reclamar un objeto que se te entrego a ti");
        }
        if (casos.existsByTipoReclamoAndPersonaReclamaIdPersonaAndEntregaIdEntregaAndEstadoIn(
                TipoReclamo.RECLAMO, idPersona, solicitud.idEntrega(), ABIERTOS)) {
            throw new ResourceInUseException("Ya tienes un reclamo abierto para esta entrega");
        }

        Reclamos caso = new Reclamos();
        caso.setTipoReclamo(TipoReclamo.RECLAMO);
        caso.setEntrega(entrega);
        caso.setCustodia(entrega.getCustodia());
        caso.setHallazgo(entrega.getCustodia().getHallazgo());
        caso.setPersonaReclama(solicitante);
        caso.setDescripcion(descripcion);
        caso.setEvidencia(guardarEvidencia(evidencia));
        caso.setEstado(EstadoReclamo.PENDIENTE);

        Reclamos guardado = casos.saveAndFlush(caso);
        avisos.reclamoRegistrado(guardado);
        return aDtoPrivado(guardado);
    }

    @Transactional
    public CasoRegistradoDTO incidencia(RegistrarIncidenciaRequest solicitud, MultipartFile evidencia,
                                        Long idPersonal) {
        if (solicitud == null) throw new IllegalArgumentException("Faltan datos de la incidencia");
        validarId(solicitud.idCustodia());
        Personal responsable = personalActivo(idPersonal);
        ObjetosCustodiados custodia = custodias.findById(solicitud.idCustodia())
                .orElseThrow(() -> new ResourceNotFoundException("Custodia no encontrada"));
        if (entregas.existsByCustodiaIdCustodia(solicitud.idCustodia())) {
            throw new ResourceInUseException(
                    "El objeto ya fue entregado; la incidencia solo aplica durante la custodia");
        }
        Reclamos caso = new Reclamos();
        caso.setTipoReclamo(TipoReclamo.INCIDENCIA);
        caso.setCustodia(custodia);
        caso.setHallazgo(custodia.getHallazgo());
        caso.setPersonalRegistra(responsable);
        caso.setDescripcion(texto(solicitud.descripcion(), "descripcion", 15, 255));
        caso.setEvidencia(guardarEvidencia(evidencia));
        caso.setEstado(EstadoReclamo.PENDIENTE);
        return aDto(casos.saveAndFlush(caso));
    }

    // ===================== CONSULTAS =====================

    @Transactional(readOnly = true)
    public List<CasoRegistradoDTO> misReclamos(Long idPersona) {
        validarId(idPersona);
        return casos.findByTipoReclamoAndPersonaReclamaIdPersonaOrderByFechaRegistroDescIdReclamoDesc(
                TipoReclamo.RECLAMO, idPersona).stream().map(CasosService::aDtoPrivado).toList();
    }

    @Transactional(readOnly = true)
    public CasoRegistradoDTO consultarReclamo(Long idCaso, UsuarioPrincipal usuario) {
        Reclamos c = buscar(idCaso, TipoReclamo.RECLAMO);
        if (esDueno(c, usuario)) return aDtoPrivado(c);
        if (!esPersonal(usuario)) throw new ResourceNotFoundException("Reclamo no encontrado");
        personalActivo(usuario.idPersona());
        return aDto(c);
    }

    @Transactional(readOnly = true)
    public CasoRegistradoDTO consultarIncidencia(Long idCaso, Long idPersonal) {
        personalActivo(idPersonal);
        return aDto(buscar(idCaso, TipoReclamo.INCIDENCIA));
    }

    /** El dueno del reclamo o el personal pueden ver la evidencia. */
    @Transactional(readOnly = true)
    public AlmacenamientoImagenReclamoService.Imagen leerEvidencia(Long idCaso, TipoReclamo tipo,
                                                                   UsuarioPrincipal usuario) {
        Reclamos c = buscar(idCaso, tipo);
        if (!esDueno(c, usuario)) {
            if (!esPersonal(usuario)) throw new ResourceNotFoundException("Caso no encontrado");
            personalActivo(usuario.idPersona());
        }
        if (c.getEvidencia() == null || c.getEvidencia().isBlank()) {
            throw new ResourceNotFoundException("Este caso no tiene evidencia adjunta");
        }
        return evidencias.leer(c.getEvidencia());
    }

    @Transactional(readOnly = true)
    public List<CasoRegistradoDTO> pendientes(TipoReclamo tipo, Long idPersonal) {
        personalActivo(idPersonal);
        return casos.findByTipoReclamoAndEstadoOrderByFechaRegistroAscIdReclamoAsc(
                tipo, EstadoReclamo.PENDIENTE).stream().map(CasosService::aDto).toList();
    }

    @Transactional(readOnly = true)
    public List<CasoRegistradoDTO> listar(TipoReclamo tipo, EstadoReclamo estado, Long idPersonal) {
        personalActivo(idPersonal);
        if (estado == null) {
            return casos.findByTipoReclamoOrderByFechaRegistroDescIdReclamoDesc(tipo)
                    .stream().map(CasosService::aDto).toList();
        }
        return casos.findByTipoReclamoAndEstadoOrderByFechaRegistroDescIdReclamoDesc(tipo, estado)
                .stream().map(CasosService::aDto).toList();
    }

    // ===================== REVISION =====================

    @Transactional
    public CasoRegistradoDTO revisar(Long idCaso, TipoReclamo tipo,
                                     RevisarCasoRequest solicitud, Long idPersonal) {
        Personal revisor = personalActivo(idPersonal);
        if (solicitud == null || solicitud.estado() == null) {
            throw new IllegalArgumentException("Debe indicar un estado de revision");
        }
        validarId(idCaso);
        Reclamos caso = casos.bloquearPorId(idCaso)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));
        if (caso.getTipoReclamo() != tipo) {
            throw new ResourceNotFoundException("Caso no encontrado en este modulo");
        }
        if (caso.getEstado().esFinal()) {
            throw new ResourceInUseException("Un caso cerrado no puede modificarse");
        }

        EstadoReclamo nuevo = solicitud.estado();
        if (nuevo == EstadoReclamo.EN_REVISION) {
            if (caso.getEstado() != EstadoReclamo.PENDIENTE) {
                throw new ResourceInUseException("Solo un caso PENDIENTE pasa a EN_REVISION");
            }
            caso.setEstado(EstadoReclamo.EN_REVISION);
            return aDto(casos.saveAndFlush(caso));
        }

        if (!estadoFinalPermitido(tipo, nuevo)) {
            throw new IllegalArgumentException(tipo == TipoReclamo.RECLAMO
                    ? "Un reclamo solo puede pasar a EN_REVISION, APROBADO o RECHAZADO"
                    : "Una incidencia solo puede pasar a EN_REVISION o RESUELTO");
        }
        if (caso.getEstado() != EstadoReclamo.EN_REVISION) {
            throw new ResourceInUseException("Primero debe ponerse EN_REVISION");
        }
        caso.setResolucion(texto(solicitud.resolucion(), "resolucion", 10, 500));
        caso.setFechaResolucion(LocalDateTime.now(ZoneId.of("America/Lima"))
                .truncatedTo(ChronoUnit.SECONDS));
        caso.setEstado(nuevo);
        if (tipo == TipoReclamo.RECLAMO && caso.getPersonalRegistra() == null) {
            // En un reclamo, el personal que lo resolvio queda registrado.
            caso.setPersonalRegistra(revisor);
        }
        // Resolucion humana; NO altera una entrega, custodia ni una perdida.
        Reclamos guardado = casos.saveAndFlush(caso);
        if (tipo == TipoReclamo.RECLAMO) {
            avisos.reclamoResuelto(guardado);
        }
        return aDto(guardado);
    }

    // ===================== AUXILIARES =====================

    private static boolean estadoFinalPermitido(TipoReclamo tipo, EstadoReclamo estado) {
        return tipo == TipoReclamo.RECLAMO
                ? estado == EstadoReclamo.APROBADO || estado == EstadoReclamo.RECHAZADO
                : estado == EstadoReclamo.RESUELTO;
    }

    /** Guarda la evidencia (opcional) y la borra si la transaccion se revierte. */
    private String guardarEvidencia(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) return null;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("No hay una transaccion activa");
        }
        String referencia = evidencias.guardar(archivo);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int estado) {
                        if (estado != TransactionSynchronization.STATUS_COMMITTED) {
                            evidencias.eliminarTrasRollback(referencia);
                        }
                    }
                });
        return referencia;
    }

    private Reclamos buscar(Long id, TipoReclamo tipo) {
        validarId(id);
        Reclamos caso = casos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));
        if (caso.getTipoReclamo() != tipo) throw new ResourceNotFoundException("Caso no encontrado");
        return caso;
    }

    private Personal personalActivo(Long id) {
        validarId(id);
        Personal p = personales.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe personal para esta sesion"));
        if (p.getEstado() != EstadoPersonal.ACTIVO) {
            throw new ResourceInUseException("El personal no esta activo");
        }
        return p;
    }

    private static boolean esDueno(Reclamos c, UsuarioPrincipal u) {
        return u != null && c.getPersonaReclama() != null
                && c.getPersonaReclama().getIdPersona().equals(u.idPersona());
    }

    private static boolean esPersonal(UsuarioPrincipal u) {
        return u != null && (u.rol() == RolUsuario.PERSONAL || u.rol() == RolUsuario.ADMIN);
    }

    private static String texto(String value, String campo, int minimo, int maximo) {
        if (value == null || value.strip().length() < minimo || value.strip().length() > maximo)
            throw new IllegalArgumentException("El campo " + campo + " debe tener entre " + minimo + " y " + maximo + " caracteres");
        return value.strip();
    }

    private static void validarId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Identificador invalido");
    }

    private static CasoRegistradoDTO aDtoPrivado(Reclamos c) {
        // El estudiante no conoce identificadores internos de custodias o hallazgos ajenos.
        return new CasoRegistradoDTO(c.getIdReclamo(), c.getTipoReclamo(), c.getEstado(),
                c.getEntrega() == null ? null : c.getEntrega().getIdEntrega(),
                null, null, c.getDescripcion(), c.getResolucion(),
                tieneEvidencia(c), c.getFechaRegistro(), c.getFechaResolucion());
    }

    private static CasoRegistradoDTO aDto(Reclamos c) {
        return new CasoRegistradoDTO(
                c.getIdReclamo(), c.getTipoReclamo(), c.getEstado(),
                c.getEntrega() == null ? null : c.getEntrega().getIdEntrega(),
                c.getCustodia() == null ? null : c.getCustodia().getIdCustodia(),
                c.getHallazgo() == null ? null : c.getHallazgo().getIdHallazgo(),
                c.getDescripcion(), c.getResolucion(), tieneEvidencia(c),
                c.getFechaRegistro(), c.getFechaResolucion());
    }

    private static boolean tieneEvidencia(Reclamos c) {
        return c.getEvidencia() != null && !c.getEvidencia().isBlank();
    }
}
