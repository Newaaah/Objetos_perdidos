package pe.edu.upeu.objetosperdidos.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.objetosperdidos.dto.EntregaRegistradaDTO;
import pe.edu.upeu.objetosperdidos.dto.EstadoCustodiaEntregaDTO;
import pe.edu.upeu.objetosperdidos.dto.RegistrarEntregaRequest;
import pe.edu.upeu.objetosperdidos.entity.Coincidencia;
import pe.edu.upeu.objetosperdidos.entity.Entrega;
import pe.edu.upeu.objetosperdidos.entity.EstadoCoincidencia;
import pe.edu.upeu.objetosperdidos.entity.EstadoReportePerdida;
import pe.edu.upeu.objetosperdidos.entity.ReportePerdida;
import pe.edu.upeu.objetosperdidos.entity.EstadoPersonal;
import pe.edu.upeu.objetosperdidos.entity.ObjetosCustodiados;
import pe.edu.upeu.objetosperdidos.entity.Persona;
import pe.edu.upeu.objetosperdidos.entity.Personal;
import pe.edu.upeu.objetosperdidos.exception.ResourceInUseException;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.repository.CoincidenciaRepository;
import pe.edu.upeu.objetosperdidos.repository.EntregaRepository;
import pe.edu.upeu.objetosperdidos.repository.ObjetosCustodiadosRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonaRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonalRepository;
import pe.edu.upeu.objetosperdidos.repository.ReportePerdidaRepository;

@Service
public class EntregaServiceImpl implements IEntregaService {

    private final EntregaRepository entregas;
    private final CoincidenciaRepository coincidencias;
    private final ReportePerdidaRepository perdidas;
    private final ObjetosCustodiadosRepository custodias;
    private final PersonaRepository personas;
    private final PersonalRepository personales;
    private final IFileStorageService imagenes;
    private final AvisosDeDominioService avisos;

    public EntregaServiceImpl(EntregaRepository entregas,
                              CoincidenciaRepository coincidencias,
                              ReportePerdidaRepository perdidas,
                              ObjetosCustodiadosRepository custodias,
                              PersonaRepository personas,
                              PersonalRepository personales,
                              IFileStorageService imagenes,
                              AvisosDeDominioService avisos) {
        this.entregas = entregas;
        this.coincidencias = coincidencias;
        this.perdidas = perdidas;
        this.custodias = custodias;
        this.personas = personas;
        this.personales = personales;
        this.imagenes = imagenes;
        this.avisos = avisos;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<pe.edu.upeu.objetosperdidos.dto.MiEntregaDTO> misEntregas(Long idPersona) {
        validarId(idPersona, "persona");
        return entregas.findByPersonaIdPersonaOrderByFechaEntregaDescIdEntregaDesc(idPersona)
                .stream()
                .map(e -> new pe.edu.upeu.objetosperdidos.dto.MiEntregaDTO(
                        e.getIdEntrega(), e.getCustodia().getIdCustodia(),
                        e.getReportePerdida() == null ? null : e.getReportePerdida().getIdReportePerdida(),
                        e.getCustodia().getObjeto().getNombre(), e.getFechaEntrega()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoCustodiaEntregaDTO estadoCustodia(Long idCustodia) {
        validarId(idCustodia, "custodia");
        ObjetosCustodiados custodia = buscarCustodia(idCustodia);
        Long idEntrega = entregas.findByCustodiaIdCustodia(idCustodia)
                .map(Entrega::getIdEntrega).orElse(null);
        return new EstadoCustodiaEntregaDTO(
                idCustodia,
                custodia.getObjeto().getIdObjeto(),
                custodia.getObjeto().getNombre(),
                idEntrega == null,
                idEntrega);
    }

    @Override
    @Transactional
    public EntregaRegistradaDTO registrar(RegistrarEntregaRequest datos,
                                          MultipartFile fotografia,
                                          Long idPersonaPersonal) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos de entrega son obligatorios");
        }
        validarId(datos.idCustodia(), "custodia");
        validarId(datos.idPersonaRecibe(), "persona que recibe");
        if (datos.idCoincidencia() != null) {
            validarId(datos.idCoincidencia(), "coincidencia");
        }
        if (datos.idReportePerdida() != null) {
            validarId(datos.idReportePerdida(), "reporte de perdida");
        }
        if (!Boolean.TRUE.equals(datos.confirmoVerificacionPresencial())) {
            throw new IllegalArgumentException(
                    "Debes confirmar la verificacion presencial de identidad y propiedad");
        }
        if (!Boolean.TRUE.equals(datos.confirmoEntregaFisica())) {
            throw new IllegalArgumentException("Debes confirmar la entrega fisica");
        }
        String observacion = datos.observacionVerificacion();
        if (observacion == null || observacion.isBlank()
                || observacion.strip().length() > 255) {
            throw new IllegalArgumentException(
                    "La observacion de verificacion es obligatoria (maximo 255 caracteres)");
        }

        Personal responsable = personales.findById(idPersonaPersonal)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe personal asociado a la sesion"));
        if (responsable.getEstado() != EstadoPersonal.ACTIVO) {
            throw new ResourceInUseException("El personal no esta activo");
        }
        Persona destinatario = personas.findById(datos.idPersonaRecibe())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Persona receptora no encontrada con ID: " + datos.idPersonaRecibe()));

        // Bloqueo en Oracle de la fila de custodia durante la comprobacion e INSERT.
        ObjetosCustodiados custodia = custodias.bloquearParaEntrega(datos.idCustodia())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custodia no encontrada con ID: " + datos.idCustodia()));
        if (entregas.existsByCustodiaIdCustodia(datos.idCustodia())) {
            throw new ResourceInUseException("Esta custodia ya fue entregada");
        }

        // El personal debe seleccionar expresamente la coincidencia.
        // Una coincidencia CONFIRMADA no implica entrega ni cambia la perdida.
        Coincidencia coincidencia = null;
        ReportePerdida reporte = null;
        if (datos.idCoincidencia() != null) {
            coincidencia = coincidencias.findById(datos.idCoincidencia())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Coincidencia no encontrada: " + datos.idCoincidencia()));
            if (coincidencia.getEstado() != EstadoCoincidencia.CONFIRMADA) {
                throw new ResourceInUseException("Solo se puede vincular una coincidencia CONFIRMADA");
            }
            if (custodia.getHallazgo() == null
                    || !custodia.getHallazgo().getIdHallazgo()
                            .equals(coincidencia.getHallazgo().getIdHallazgo())) {
                throw new ResourceInUseException(
                        "La coincidencia no corresponde al hallazgo de esta custodia");
            }
            Long idReporte = coincidencia.getReportePerdida().getIdReportePerdida();
            if (datos.idReportePerdida() != null && !datos.idReportePerdida().equals(idReporte)) {
                throw new ResourceInUseException("El reporte solicitado no pertenece a la coincidencia");
            }
            reporte = perdidas.bloquearParaEntrega(idReporte)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Reporte de perdida no encontrado: " + idReporte));
            if (reporte.getEstado() != EstadoReportePerdida.REPORTADO) {
                throw new ResourceInUseException("La perdida ya no esta en estado REPORTADO");
            }
            if (!destinatario.getIdPersona().equals(reporte.getPersona().getIdPersona())) {
                throw new ResourceInUseException(
                        "La persona que recibe no coincide con el titular del reporte de perdida");
            }
            if (entregas.findByReportePerdidaIdReportePerdida(idReporte).isPresent()) {
                throw new ResourceInUseException("Este reporte de perdida ya tiene entrega registrada");
            }
        } else if (custodia.getHallazgo() != null
                && coincidencias.existsByHallazgoIdHallazgoAndEstado(
                        custodia.getHallazgo().getIdHallazgo(), EstadoCoincidencia.CONFIRMADA)) {
            throw new ResourceInUseException(
                    "Este hallazgo tiene coincidencia CONFIRMADA; indica su idCoincidencia para entregar");
        }

        // Entrega directa con reporte previo: el personal debe vincularlo explicitamente.
        // Nunca se vincula automaticamente por similitud heuristica.
        if (coincidencia == null && datos.idReportePerdida() != null) {
            reporte = perdidas.bloquearParaEntrega(datos.idReportePerdida())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Reporte de perdida no encontrado: " + datos.idReportePerdida()));
            if (reporte.getEstado() != EstadoReportePerdida.REPORTADO) {
                throw new ResourceInUseException("Solo un reporte REPORTADO puede recuperarse");
            }
            if (!destinatario.getIdPersona().equals(reporte.getPersona().getIdPersona())) {
                throw new ResourceInUseException("El receptor no es el titular del reporte vinculado");
            }
            if (entregas.findByReportePerdidaIdReportePerdida(datos.idReportePerdida()).isPresent()) {
                throw new ResourceInUseException("El reporte ya tiene una entrega registrada");
            }
        }

        // Foto de evidencia obligatoria. El archivo se elimina si la transaccion falla.
        String referencia = guardarConLimpieza(fotografia);
        Entrega entrega = new Entrega();
        entrega.setCustodia(custodia);
        entrega.setPersona(destinatario);
        entrega.setPersonal(responsable);
        entrega.setObservacionVerificacion(observacion.strip());
        entrega.setFotoEvidencia(referencia);
        entrega.setCoincidencia(coincidencia);
        entrega.setReportePerdida(reporte);

        // No marcamos RECUPERADO hasta registrar la entrega fisica y su evidencia.
        Entrega guardada = entregas.saveAndFlush(entrega);
        if (reporte != null) {
            reporte.setEstado(EstadoReportePerdida.RECUPERADO);
            perdidas.saveAndFlush(reporte);
        }
        avisos.entregaRegistrada(guardada);
        return convertir(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public EntregaRegistradaDTO consultar(Long idEntrega) {
        validarId(idEntrega, "entrega");
        Entrega entrega = entregas.findById(idEntrega)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Entrega no encontrada con ID: " + idEntrega));
        return convertir(entrega);
    }

    @Override
    @Transactional(readOnly = true)
    public IFileStorageService.Imagen leerEvidencia(Long idEntrega) {
        validarId(idEntrega, "entrega");
        Entrega entrega = entregas.findById(idEntrega)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Entrega no encontrada con ID: " + idEntrega));
        return imagenes.load(entrega.getFotoEvidencia());
    }

    private ObjetosCustodiados buscarCustodia(Long idCustodia) {
        return custodias.findById(idCustodia)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custodia no encontrada con ID: " + idCustodia));
    }

    private String guardarConLimpieza(MultipartFile fotografia) {
        // Se borra sola si la transaccion falla.
        return imagenes.storeEnTransaccion(fotografia, IFileStorageService.ENTREGAS);
    }

    private static EntregaRegistradaDTO convertir(Entrega e) {
        return new EntregaRegistradaDTO(
                e.getIdEntrega(),
                e.getCustodia().getIdCustodia(),
                e.getCustodia().getObjeto().getIdObjeto(),
                e.getCustodia().getObjeto().getNombre(),
                e.getPersona().getIdPersona(),
                e.getPersonal().getIdPersona(),
                e.getFechaEntrega(),
                e.getObservacionVerificacion(),
                e.getFotoEvidencia(),
                e.getCoincidencia() == null ? null : e.getCoincidencia().getIdCoincidencia(),
                e.getReportePerdida() == null ? null : e.getReportePerdida().getIdReportePerdida());
    }

    private static void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de " + campo + " debe ser positivo");
        }
    }
}
