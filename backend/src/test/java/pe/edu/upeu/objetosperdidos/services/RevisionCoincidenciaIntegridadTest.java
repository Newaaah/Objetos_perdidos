package pe.edu.upeu.objetosperdidos.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import pe.edu.upeu.objetosperdidos.dto.RevisarCoincidenciaRequest;
import pe.edu.upeu.objetosperdidos.entity.Categoria;
import pe.edu.upeu.objetosperdidos.entity.Coincidencia;
import pe.edu.upeu.objetosperdidos.entity.EstadoCoincidencia;
import pe.edu.upeu.objetosperdidos.entity.EstadoPersonal;
import pe.edu.upeu.objetosperdidos.entity.EstadoReporteHallazgo;
import pe.edu.upeu.objetosperdidos.entity.EstadoReportePerdida;
import pe.edu.upeu.objetosperdidos.entity.Objeto;
import pe.edu.upeu.objetosperdidos.entity.Personal;
import pe.edu.upeu.objetosperdidos.entity.ReporteHallazgos;
import pe.edu.upeu.objetosperdidos.entity.ReportePerdida;
import pe.edu.upeu.objetosperdidos.repository.CoincidenciaRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonalRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica la propagacion de ORA-00001 simulada y el orden de bloqueos.
 * No ejecuta transacciones Oracle reales ni escribe en la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class RevisionCoincidenciaIntegridadTest {

    @Mock CoincidenciaRepository coincidencias;
    @Mock PersonalRepository personal;
    @Mock EntityManager em;
    @Mock TypedQuery<Long> consultaEntregas;

    @Test
    void preservaErrorDeIntegridadParaQueElManejadorHttpLoConviertaEn409() {
        Personal revisor = new Personal();
        revisor.setIdPersona(4L);
        revisor.setEstado(EstadoPersonal.ACTIVO);
        when(personal.findById(4L)).thenReturn(Optional.of(revisor));

        ReportePerdida perdida = new ReportePerdida();
        perdida.setIdReportePerdida(21L);
        perdida.setEstado(EstadoReportePerdida.REPORTADO);
        perdida.setObjeto(objeto("Guitarra", "Yamaha"));
        perdida.setFechaPerdida(LocalDate.of(2026, 11, 20));
        when(em.find(ReportePerdida.class, 21L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(perdida);

        ReporteHallazgos hallazgo = new ReporteHallazgos();
        hallazgo.setIdHallazgo(22L);
        hallazgo.setEstado(EstadoReporteHallazgo.APROBADO);
        hallazgo.setObjeto(objeto("Guitarra acustica", "Yamaha"));
        hallazgo.setFechaReporte(LocalDateTime.of(2026, 11, 20, 14, 0));
        when(em.find(ReporteHallazgos.class, 22L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(hallazgo);

        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(consultaEntregas);
        when(consultaEntregas.setParameter("idHallazgo", 22L)).thenReturn(consultaEntregas);
        when(consultaEntregas.getSingleResult()).thenReturn(0L);

        DataIntegrityViolationException colision = new DataIntegrityViolationException(
                "ORA-00001: unique constraint (UPEU_DATOS.UX_COINC_CONF_HALL) violated");
        when(coincidencias.saveAndFlush(any(Coincidencia.class))).thenThrow(colision);

        RevisionCoincidenciaServiceImpl servicio = new RevisionCoincidenciaServiceImpl(
                coincidencias, personal, em, new ComparadorObjetosService());
        RevisarCoincidenciaRequest solicitud = new RevisarCoincidenciaRequest(
                21L, 22L, EstadoCoincidencia.CONFIRMADA, "Revision humana de prueba");

        assertThrows(DataIntegrityViolationException.class,
                () -> servicio.registrar(solicitud, 4L));

        InOrder orden = inOrder(em);
        orden.verify(em).find(ReportePerdida.class, 21L, LockModeType.PESSIMISTIC_WRITE);
        orden.verify(em).find(ReporteHallazgos.class, 22L, LockModeType.PESSIMISTIC_WRITE);
        verify(coincidencias).saveAndFlush(any(Coincidencia.class));
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
}
