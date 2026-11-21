package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.entity.*;
import pe.edu.upeu.objetosperdidos.repository.*;

/**
 * Avisos internos en la misma transaccion que el evento de negocio.
 * No se envia correo/WhatsApp, no se consultan camaras, ni se declara propietario a nadie.
 * El control de duplicados es por persona+tipo+reporte/hallazgo/custodia.
 */
@Service
public class AvisosDeDominioService {
    private final NotificacionesRepository repo;
    private final ReportePerdidaRepository perdidas;
    private final ComparadorObjetosService comparador;
    private final ISugerenciasCoincidenciaService sugerencias;
    private final PersonalUbicacionRepository asignaciones;
    private final UsuarioRepository usuarios;

    public AvisosDeDominioService(NotificacionesRepository repo, ReportePerdidaRepository perdidas,
            ComparadorObjetosService comparador, ISugerenciasCoincidenciaService sugerencias,
            PersonalUbicacionRepository asignaciones, UsuarioRepository usuarios) {
        this.asignaciones = asignaciones;
        this.usuarios = usuarios;
        this.repo = repo;
        this.perdidas = perdidas;
        this.comparador = comparador;
        this.sugerencias = sugerencias;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void perdidaRegistrada(ReportePerdida perdida) {
        // La recomendacion existente ya filtra los candidatos aptos y evita mostrar datos privados.
        var resultado = sugerencias.sugerir(perdida.getIdReportePerdida(), perdida.getPersona().getIdPersona());
        if (!resultado.sugerencias().isEmpty()) avisarPosible(perdida);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void hallazgoRevisado(ReporteHallazgos hallazgo) {
        boolean aprobada = hallazgo.getEstado() == EstadoReporteHallazgo.APROBADO;
        emitir(hallazgo.getPersona(),
                aprobada ? TipoNotificacion.HALLAZGO_APROBADO : TipoNotificacion.HALLAZGO_RECHAZADO,
                aprobada ? "Reporte de hallazgo aprobado" : "Reporte de hallazgo rechazado",
                aprobada ? "Tu reporte fue aprobado. La recepcion fisica se registra por separado."
                         : "Tu reporte fue rechazado. Revisa el estado de tu hallazgo.",
                null, hallazgo, null);
        if (aprobada) {
            avisarPersonalDestino(hallazgo);
            posiblesParaHallazgo(hallazgo);
        }
    }

    /** Ej.: el personal de garita sabe que alguien llevara ese objeto. */
    private void avisarPersonalDestino(ReporteHallazgos h) {
        if (h.getUbicacionDestino() == null) return;
        String objeto = h.getObjeto() == null ? "un objeto" : h.getObjeto().getNombre();
        String mensaje = recortar("Un estudiante llevara '" + objeto + "' a "
                + h.getUbicacionDestino().getNombre()
                + ". Compara el objeto con la foto del hallazgo al recibirlo.", 255);
        for (PersonalUbicacion asignacion : asignaciones
                .findByUbicacionIdUbicacionAndFechaFinIsNull(
                        h.getUbicacionDestino().getIdUbicacion())) {
            Personal p = asignacion.getPersona();
            if (p == null || p.getEstado() != EstadoPersonal.ACTIVO) continue;
            emitir(p.getPersona(), TipoNotificacion.HALLAZGO_PENDIENTE_RECEPCION,
                    "Hallazgo por recibir", mensaje, null, h, null);
        }
    }

    /** Aviso a los administradores activos de que hay un reclamo nuevo. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void reclamoRegistrado(Reclamos caso) {
        String mensaje = "Se registro el reclamo #" + caso.getIdReclamo()
                + " sobre la entrega #" + caso.getEntrega().getIdEntrega()
                + ". Revisalo en la bandeja de reclamos.";
        for (Usuario admin : usuarios.findByRolAndEstado(RolUsuario.ADMIN, EstadoUsuario.ACTIVO)) {
            // Sin control de duplicados: cada reclamo es un caso distinto.
            guardar(admin.getPersona(), TipoNotificacion.RECLAMO_REGISTRADO,
                    "Nuevo reclamo de devolucion", mensaje, null, null, caso.getCustodia());
        }
    }

    /** Aviso al estudiante de que su reclamo fue aprobado o rechazado. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void reclamoResuelto(Reclamos caso) {
        boolean aprobado = caso.getEstado() == EstadoReclamo.APROBADO;
        guardar(caso.getPersonaReclama(), TipoNotificacion.RECLAMO_RESUELTO,
                aprobado ? "Tu reclamo fue aprobado" : "Tu reclamo fue rechazado",
                "Tu reclamo #" + caso.getIdReclamo() + " fue "
                        + (aprobado ? "APROBADO" : "RECHAZADO")
                        + ". Revisa la resolucion en Mis reclamos.",
                null, null, caso.getCustodia());
    }

    private static String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 3) + "...";
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void custodiaRecibida(ObjetosCustodiados c) {
        Persona personaAvisada = c.getHallazgo() != null ? c.getHallazgo().getPersona() : c.getPersonaEntrega();
        if (personaAvisada != null) {
            emitir(personaAvisada, TipoNotificacion.OBJETO_EN_CUSTODIA,
                    "Objeto recibido en custodia",
                    "Se registro una recepcion fisica. La custodia no significa entrega ni prueba propiedad.",
                    null, c.getHallazgo(), c);
        }
        if (c.getHallazgo() == null) posiblesParaCustodia(c);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void coincidenciaConfirmada(Coincidencia coincidencia) {
        if (coincidencia.getEstado() == EstadoCoincidencia.CONFIRMADA) {
            avisarPosible(coincidencia.getReportePerdida());
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void entregaRegistrada(Entrega e) {
        emitir(e.getPersona(), TipoNotificacion.ENTREGA_REGISTRADA,
                "Devolucion registrada",
                "El personal registro una entrega fisica con verificacion y evidencia.",
                e.getReportePerdida(), null, e.getCustodia());
    }

    private void posiblesParaHallazgo(ReporteHallazgos h) {
        if (h.getObjeto() == null || h.getObjeto().getCategoria() == null) return;
        for (ReportePerdida p : candidatas(h.getObjeto().getCategoria().getIdCategoria())) {
            var r = comparador.comparar(p, h);
            if (apto(r)) avisarPosible(p);
        }
    }

    private void posiblesParaCustodia(ObjetosCustodiados c) {
        if (c.getObjeto() == null || c.getObjeto().getCategoria() == null) return;
        for (ReportePerdida p : candidatas(c.getObjeto().getCategoria().getIdCategoria())) {
            var r = comparador.comparar(p, c);
            if (apto(r)) avisarPosible(p);
        }
    }

    private List<ReportePerdida> candidatas(Long idCategoria) {
        // Limite para que el evento no recorra indefinidamente todos los reportes.
        return perdidas.buscarActivasPorCategoria(EstadoReportePerdida.REPORTADO,
                idCategoria, PageRequest.of(0, 200));
    }

    private boolean apto(ComparadorObjetosService.ResultadoComparacion r) {
        return r.cobertura() >= 45 && r.similitud() >= 45 && r.prioridad() >= 40;
    }

    private void avisarPosible(ReportePerdida p) {
        if (p.getEstado() != EstadoReportePerdida.REPORTADO) return;
        emitir(p.getPersona(), TipoNotificacion.POSIBLE_COINCIDENCIA,
                "Posible coincidencia para tu reporte",
                "Hay un candidato que puedes revisar. No confirma propiedad ni autoriza una entrega.",
                p, null, null);
    }

    private void emitir(Persona destino, TipoNotificacion tipo, String titulo, String mensaje,
                        ReportePerdida perdida, ReporteHallazgos hallazgo, ObjetosCustodiados custodia) {
        if (destino == null || destino.getIdPersona() == null) return;
        Long id = destino.getIdPersona();
        if (perdida != null && repo.existsByPersonaIdPersonaAndTipoAndReportePerdidaIdReportePerdida(
                id, tipo, perdida.getIdReportePerdida())) return;
        if (hallazgo != null && repo.existsByPersonaIdPersonaAndTipoAndHallazgoIdHallazgo(
                id, tipo, hallazgo.getIdHallazgo())) return;
        if (custodia != null && repo.existsByPersonaIdPersonaAndTipoAndCustodiaIdCustodia(
                id, tipo, custodia.getIdCustodia())) return;
        guardar(destino, tipo, titulo, mensaje, perdida, hallazgo, custodia);
    }

    private void guardar(Persona destino, TipoNotificacion tipo, String titulo, String mensaje,
                         ReportePerdida perdida, ReporteHallazgos hallazgo, ObjetosCustodiados custodia) {
        if (destino == null || destino.getIdPersona() == null) return;
        Notificaciones n = new Notificaciones();
        n.setPersona(destino);
        n.setTipo(tipo);
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setReportePerdida(perdida);
        n.setHallazgo(hallazgo);
        n.setCustodia(custodia);
        n.setLeida(0);
        repo.saveAndFlush(n);
    }
}
