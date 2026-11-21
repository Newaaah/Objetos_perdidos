package pe.edu.upeu.objetosperdidos.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.objetosperdidos.dto.RevisarCoincidenciaRequest;
import pe.edu.upeu.objetosperdidos.entity.*;
import pe.edu.upeu.objetosperdidos.exception.ResourceInUseException;
import pe.edu.upeu.objetosperdidos.repository.CoincidenciaRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonalRepository;

@ExtendWith(MockitoExtension.class)
class RevisionCoincidenciaServiceImplTest {

    @Mock CoincidenciaRepository coincidencias;
    @Mock PersonalRepository personal;
    @Mock EntityManager em;
    @Mock TypedQuery<Long> consultaEntregas;
    RevisionCoincidenciaServiceImpl servicio;

    @BeforeEach void preparar() {
        servicio = new RevisionCoincidenciaServiceImpl(
                coincidencias, personal, em, new ComparadorObjetosService());
    }

    private static Objeto objeto(String nombre, String marca) {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(3L);
        categoria.setNombre("Instrumentos musicales");
        Objeto objeto = new Objeto();
        objeto.setNombre(nombre);
        objeto.setMarca(marca);
        objeto.setColor("Negro");
        objeto.setDescripcion("Objeto de prueba");
        objeto.setCategoria(categoria);
        return objeto;
    }

    private static ReportePerdida perdida() {
        ReportePerdida p = new ReportePerdida();
        p.setIdReportePerdida(21L);
        p.setEstado(EstadoReportePerdida.REPORTADO);
        p.setObjeto(objeto("Guitarra de prueba", "Yamaha"));
        p.setFechaPerdida(LocalDate.of(2026, 11, 20));
        return p;
    }

    private static ReporteHallazgos hallazgo() {
        ReporteHallazgos h = new ReporteHallazgos();
        h.setIdHallazgo(22L);
        h.setEstado(EstadoReporteHallazgo.APROBADO);
        h.setObjeto(objeto("Guitarra acustica Yamaha", "Yamaha"));
        h.setFechaReporte(LocalDateTime.of(2026, 11, 20, 14, 0));
        return h;
    }

    private static Personal revisor(EstadoPersonal estado) {
        Personal p = new Personal();
        p.setIdPersona(4L);
        p.setEstado(estado);
        return p;
    }

    private void autenticarRevisorActivo() {
        when(personal.findById(4L)).thenReturn(Optional.of(revisor(EstadoPersonal.ACTIVO)));
    }

    private void cargarParejaBloqueada(ReportePerdida p, ReporteHallazgos h) {
        when(em.find(ReportePerdida.class, 21L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(p);
        if (p.getEstado() == EstadoReportePerdida.REPORTADO) {
            when(em.find(ReporteHallazgos.class, 22L, LockModeType.PESSIMISTIC_WRITE))
                    .thenReturn(h);
        }
    }

    private void simularEntregas(long total) {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(consultaEntregas);
        when(consultaEntregas.setParameter("idHallazgo", 22L)).thenReturn(consultaEntregas);
        when(consultaEntregas.getSingleResult()).thenReturn(total);
    }

    private static RevisarCoincidenciaRequest peticion(EstadoCoincidencia decision) {
        return new RevisarCoincidenciaRequest(21L, 22L, decision,
                "Revision ficticia del personal: se compararon los datos registrados");
    }

    @Test void registraConfirmadaConRevisorYSimilitudCalculada() {
        autenticarRevisorActivo();
        cargarParejaBloqueada(perdida(), hallazgo());
        simularEntregas(0);
        when(coincidencias.saveAndFlush(any(Coincidencia.class))).thenAnswer(inv -> {
            Coincidencia c = inv.getArgument(0);
            c.setIdCoincidencia(35L);
            c.setFechaRevision(LocalDateTime.of(2026, 11, 20, 15, 0));
            return c;
        });

        var resultado = servicio.registrar(peticion(EstadoCoincidencia.CONFIRMADA), 4L);
        assertEquals(35L, resultado.idCoincidencia());
        assertEquals(4L, resultado.idPersonalRevisor());
        assertEquals("CONFIRMADA", resultado.estado());
        assertTrue(resultado.puntaje() >= 0 && resultado.puntaje() <= 100);
        ArgumentCaptor<Coincidencia> captor = ArgumentCaptor.forClass(Coincidencia.class);
        verify(coincidencias).saveAndFlush(captor.capture());
        assertEquals(22L, captor.getValue().getHallazgo().getIdHallazgo());
    }

    @Test void permiteDescartarAunqueLosObjetosSeanDiferentes() {
        autenticarRevisorActivo();
        ReporteHallazgos h = hallazgo();
        h.setObjeto(objeto("Billetera deportiva", "Sin marca"));
        cargarParejaBloqueada(perdida(), h);
        simularEntregas(0);
        when(coincidencias.saveAndFlush(any(Coincidencia.class))).thenAnswer(inv -> {
            Coincidencia c = inv.getArgument(0);
            c.setIdCoincidencia(36L);
            return c;
        });
        var resultado = servicio.registrar(peticion(EstadoCoincidencia.DESCARTADA), 4L);
        assertEquals("DESCARTADA", resultado.estado());
    }

    @Test void rechazaPersonalInactivo() {
        when(personal.findById(4L)).thenReturn(Optional.of(revisor(EstadoPersonal.INACTIVO)));
        assertThrows(ResourceInUseException.class,
                () -> servicio.registrar(peticion(EstadoCoincidencia.CONFIRMADA), 4L));
        verifyNoInteractions(em);
    }

    @Test void rechazaPerdidaCancelada() {
        autenticarRevisorActivo();
        ReportePerdida p = perdida();
        p.setEstado(EstadoReportePerdida.CANCELADO);
        cargarParejaBloqueada(p, hallazgo());
        assertThrows(ResourceInUseException.class,
                () -> servicio.registrar(peticion(EstadoCoincidencia.CONFIRMADA), 4L));
        verify(coincidencias, never()).saveAndFlush(any());
    }

    @Test void rechazaHallazgoPendiente() {
        autenticarRevisorActivo();
        ReporteHallazgos h = hallazgo();
        h.setEstado(EstadoReporteHallazgo.PENDIENTE);
        cargarParejaBloqueada(perdida(), h);
        assertThrows(ResourceInUseException.class,
                () -> servicio.registrar(peticion(EstadoCoincidencia.CONFIRMADA), 4L));
        verify(coincidencias, never()).saveAndFlush(any());
    }

    @Test void evitaRegistrarDosVecesLaMismaPareja() {
        autenticarRevisorActivo();
        cargarParejaBloqueada(perdida(), hallazgo());
        when(coincidencias.findByReportePerdidaIdReportePerdidaAndHallazgoIdHallazgo(21L, 22L))
                .thenReturn(Optional.of(new Coincidencia()));
        assertThrows(ResourceInUseException.class,
                () -> servicio.registrar(peticion(EstadoCoincidencia.CONFIRMADA), 4L));
        verify(coincidencias, never()).saveAndFlush(any());
    }

    @Test void evitaRevisarUnHallazgoYaEntregado() {
        autenticarRevisorActivo();
        cargarParejaBloqueada(perdida(), hallazgo());
        simularEntregas(1);
        assertThrows(ResourceInUseException.class,
                () -> servicio.registrar(peticion(EstadoCoincidencia.CONFIRMADA), 4L));
        verify(coincidencias, never()).saveAndFlush(any());
    }

    @Test void impideConfirmarHallazgoConfirmadoParaOtraPerdida() {
        autenticarRevisorActivo();
        cargarParejaBloqueada(perdida(), hallazgo());
        simularEntregas(0);
        when(coincidencias.existsByHallazgoIdHallazgoAndEstado(22L, EstadoCoincidencia.CONFIRMADA))
                .thenReturn(true);
        assertThrows(ResourceInUseException.class,
                () -> servicio.registrar(peticion(EstadoCoincidencia.CONFIRMADA), 4L));
    }

    @Test void impideConfirmarPerdidaConfirmadaConOtroHallazgo() {
        autenticarRevisorActivo();
        cargarParejaBloqueada(perdida(), hallazgo());
        simularEntregas(0);
        when(coincidencias.existsByReportePerdidaIdReportePerdidaAndEstado(
                21L, EstadoCoincidencia.CONFIRMADA)).thenReturn(true);
        assertThrows(ResourceInUseException.class,
                () -> servicio.registrar(peticion(EstadoCoincidencia.CONFIRMADA), 4L));
    }

    @Test void evaluacionNoInsertaCoincidencias() {
        when(em.find(ReportePerdida.class, 21L)).thenReturn(perdida());
        when(em.find(ReporteHallazgos.class, 22L)).thenReturn(hallazgo());
        simularEntregas(0);
        var evaluacion = servicio.evaluar(21L, 22L);
        assertEquals(21L, evaluacion.idReportePerdida());
        assertTrue(evaluacion.similitud() > 0);
        assertFalse(evaluacion.objetoEntregado());
        verify(coincidencias, never()).saveAndFlush(any());
    }

    @Test void rechazaObservacionEnBlanco() {
        var solicitud = new RevisarCoincidenciaRequest(21L, 22L,
                EstadoCoincidencia.CONFIRMADA, "   ");
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrar(solicitud, 4L));
        verifyNoInteractions(em);
    }
}
