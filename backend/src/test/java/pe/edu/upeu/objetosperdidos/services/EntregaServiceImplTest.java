package pe.edu.upeu.objetosperdidos.services;

import java.util.Optional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import pe.edu.upeu.objetosperdidos.dto.RegistrarEntregaRequest;
import pe.edu.upeu.objetosperdidos.entity.Coincidencia;
import pe.edu.upeu.objetosperdidos.entity.Entrega;
import pe.edu.upeu.objetosperdidos.entity.EstadoCoincidencia;
import pe.edu.upeu.objetosperdidos.entity.EstadoReportePerdida;
import pe.edu.upeu.objetosperdidos.entity.ReporteHallazgos;
import pe.edu.upeu.objetosperdidos.entity.ReportePerdida;
import pe.edu.upeu.objetosperdidos.entity.EstadoPersonal;
import pe.edu.upeu.objetosperdidos.entity.Objeto;
import pe.edu.upeu.objetosperdidos.entity.ObjetosCustodiados;
import pe.edu.upeu.objetosperdidos.entity.Persona;
import pe.edu.upeu.objetosperdidos.entity.Personal;
import pe.edu.upeu.objetosperdidos.exception.ResourceInUseException;
import pe.edu.upeu.objetosperdidos.repository.CoincidenciaRepository;
import pe.edu.upeu.objetosperdidos.repository.EntregaRepository;
import pe.edu.upeu.objetosperdidos.repository.ObjetosCustodiadosRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonaRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonalRepository;
import pe.edu.upeu.objetosperdidos.repository.ReportePerdidaRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Sin Spring context ni Oracle: repositorios e imagenes son simulados (Mockito). */
@ExtendWith(MockitoExtension.class)
class EntregaServiceImplTest {

    @Mock private EntregaRepository entregas;
    @Mock private CoincidenciaRepository coincidencias;
    @Mock private ReportePerdidaRepository perdidas;
    @Mock private ObjetosCustodiadosRepository custodias;
    @Mock private PersonaRepository personas;
    @Mock private PersonalRepository personales;
    @Mock private AlmacenamientoImagenEntregaService imagenes;

    private EntregaServiceImpl servicio;

    @BeforeEach
    void configurar() {
        servicio = new EntregaServiceImpl(
                entregas, coincidencias, perdidas, custodias, personas, personales, imagenes);
    }

    private static RegistrarEntregaRequest solicitud(boolean verifica, boolean entrega) {
        return new RegistrarEntregaRequest(
                21L, 2L, "PRUEBA: constancia simulada", verifica, entrega);
    }

    private static MockMultipartFile archivoPrueba() {
        return new MockMultipartFile(
                "fotografia", "foto.png", "image/png", new byte[] {1, 2, 3});
    }

    @Test
    void sinVerificacionPresencialNoAccedeANingunRepositorio() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrar(solicitud(false, true), archivoPrueba(), 4L));
        verifyNoInteractions(entregas, coincidencias, perdidas,
                custodias, personas, personales, imagenes);
    }

    @Test
    void sinEntregaFisicaNoAccedeANingunRepositorio() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrar(solicitud(true, false), archivoPrueba(), 4L));
        verifyNoInteractions(entregas, coincidencias, perdidas,
                custodias, personas, personales, imagenes);
    }

    @Test
    void custodiaYaEntregadaSeRechazaAntesDeGuardarFotografia() {
        var personal = new Personal();
        personal.setEstado(EstadoPersonal.ACTIVO);
        var receptor = new Persona();
        receptor.setIdPersona(2L);
        var custodia = new ObjetosCustodiados();
        custodia.setIdCustodia(21L);

        when(personales.findById(4L)).thenReturn(Optional.of(personal));
        when(personas.findById(2L)).thenReturn(Optional.of(receptor));
        when(custodias.bloquearParaEntrega(21L)).thenReturn(Optional.of(custodia));
        when(entregas.existsByCustodiaIdCustodia(21L)).thenReturn(true);

        var error = assertThrows(ResourceInUseException.class,
                () -> servicio.registrar(solicitud(true, true), archivoPrueba(), 4L));

        assertTrue(error.getMessage().contains("ya fue entregada"));
        verifyNoInteractions(imagenes);
        verify(entregas, never()).saveAndFlush(any(Entrega.class));
    }

    @Test
    void estadoSinEntregaApareceDisponible() {
        var objeto = new Objeto();
        objeto.setIdObjeto(22L);
        objeto.setNombre("Mochila de prueba");
        var custodia = new ObjetosCustodiados();
        custodia.setIdCustodia(21L);
        custodia.setObjeto(objeto);

        when(custodias.findById(21L)).thenReturn(Optional.of(custodia));
        when(entregas.findByCustodiaIdCustodia(21L)).thenReturn(Optional.empty());

        var estado = servicio.estadoCustodia(21L);

        assertTrue(estado.disponibleParaEntrega());
        assertNull(estado.idEntrega());
        assertEquals(22L, estado.idObjeto());
    }

    @Test
    void estadoConEntregaApareceNoDisponible() {
        var objeto = new Objeto();
        objeto.setIdObjeto(23L);
        objeto.setNombre("Bufanda de prueba");
        var custodia = new ObjetosCustodiados();
        custodia.setIdCustodia(22L);
        custodia.setObjeto(objeto);
        var entrega = new Entrega();
        entrega.setIdEntrega(21L);

        when(custodias.findById(22L)).thenReturn(Optional.of(custodia));
        when(entregas.findByCustodiaIdCustodia(22L))
                .thenReturn(Optional.of(entrega));

        var estado = servicio.estadoCustodia(22L);

        assertFalse(estado.disponibleParaEntrega());
        assertEquals(21L, estado.idEntrega());
    }

    @Test
    void coincidenciaDescartadaNoPermiteEntregaVinculada() {
        var personal = new Personal();
        personal.setEstado(EstadoPersonal.ACTIVO);
        var receptor = new Persona();
        receptor.setIdPersona(2L);
        var hallazgo = new ReporteHallazgos();
        hallazgo.setIdHallazgo(33L);
        var custodia = new ObjetosCustodiados();
        custodia.setIdCustodia(21L);
        custodia.setHallazgo(hallazgo);
        var coincidencia = new Coincidencia();
        coincidencia.setEstado(EstadoCoincidencia.DESCARTADA);

        when(personales.findById(4L)).thenReturn(Optional.of(personal));
        when(personas.findById(2L)).thenReturn(Optional.of(receptor));
        when(custodias.bloquearParaEntrega(21L)).thenReturn(Optional.of(custodia));
        when(coincidencias.findById(44L)).thenReturn(Optional.of(coincidencia));

        var datos = new RegistrarEntregaRequest(21L, 2L, "Verificacion manual",
                true, true, 44L);
        assertThrows(ResourceInUseException.class,
                () -> servicio.registrar(datos, archivoPrueba(), 4L));
        verifyNoInteractions(imagenes);
        verify(entregas, never()).saveAndFlush(any(Entrega.class));
    }

    @Test
    void hallazgoConCoincidenciaConfirmadaExigeSeleccionExplicita() {
        var personal = new Personal();
        personal.setEstado(EstadoPersonal.ACTIVO);
        var receptor = new Persona();
        receptor.setIdPersona(2L);
        var hallazgo = new ReporteHallazgos();
        hallazgo.setIdHallazgo(33L);
        var custodia = new ObjetosCustodiados();
        custodia.setIdCustodia(21L);
        custodia.setHallazgo(hallazgo);

        when(personales.findById(4L)).thenReturn(Optional.of(personal));
        when(personas.findById(2L)).thenReturn(Optional.of(receptor));
        when(custodias.bloquearParaEntrega(21L)).thenReturn(Optional.of(custodia));
        when(coincidencias.existsByHallazgoIdHallazgoAndEstado(
                33L, EstadoCoincidencia.CONFIRMADA)).thenReturn(true);

        var error = assertThrows(ResourceInUseException.class,
                () -> servicio.registrar(solicitud(true, true), archivoPrueba(), 4L));
        assertTrue(error.getMessage().contains("idCoincidencia"));
        verifyNoInteractions(imagenes);
        verify(entregas, never()).saveAndFlush(any(Entrega.class));
    }

    @Test
    void coincidenciaDeOtroHallazgoSeRechazaSinFoto() {
        var personal = new Personal();
        personal.setEstado(EstadoPersonal.ACTIVO);
        var receptor = new Persona();
        receptor.setIdPersona(2L);
        var hallazgo = new ReporteHallazgos();
        hallazgo.setIdHallazgo(33L);
        var hallazgoAjeno = new ReporteHallazgos();
        hallazgoAjeno.setIdHallazgo(55L);
        var custodia = new ObjetosCustodiados();
        custodia.setIdCustodia(21L);
        custodia.setHallazgo(hallazgo);
        var coincidencia = new Coincidencia();
        coincidencia.setEstado(EstadoCoincidencia.CONFIRMADA);
        coincidencia.setHallazgo(hallazgoAjeno);

        when(personales.findById(4L)).thenReturn(Optional.of(personal));
        when(personas.findById(2L)).thenReturn(Optional.of(receptor));
        when(custodias.bloquearParaEntrega(21L)).thenReturn(Optional.of(custodia));
        when(coincidencias.findById(44L)).thenReturn(Optional.of(coincidencia));

        var datos = new RegistrarEntregaRequest(21L, 2L, "Verificacion manual",
                true, true, 44L);
        assertThrows(ResourceInUseException.class,
                () -> servicio.registrar(datos, archivoPrueba(), 4L));
        verifyNoInteractions(imagenes);
    }

    @Test
    void receptorDistintoAlReportanteNoSeMarcaRecuperado() {
        var personal = new Personal();
        personal.setEstado(EstadoPersonal.ACTIVO);
        var receptor = new Persona();
        receptor.setIdPersona(2L);
        var reportante = new Persona();
        reportante.setIdPersona(9L);
        var hallazgo = new ReporteHallazgos();
        hallazgo.setIdHallazgo(33L);
        var custodia = new ObjetosCustodiados();
        custodia.setIdCustodia(21L);
        custodia.setHallazgo(hallazgo);
        var perdida = new ReportePerdida();
        perdida.setIdReportePerdida(66L);
        perdida.setPersona(reportante);
        perdida.setEstado(EstadoReportePerdida.REPORTADO);
        var coincidencia = new Coincidencia();
        coincidencia.setEstado(EstadoCoincidencia.CONFIRMADA);
        coincidencia.setHallazgo(hallazgo);
        coincidencia.setReportePerdida(perdida);

        when(personales.findById(4L)).thenReturn(Optional.of(personal));
        when(personas.findById(2L)).thenReturn(Optional.of(receptor));
        when(custodias.bloquearParaEntrega(21L)).thenReturn(Optional.of(custodia));
        when(coincidencias.findById(44L)).thenReturn(Optional.of(coincidencia));
        when(perdidas.bloquearParaEntrega(66L)).thenReturn(Optional.of(perdida));

        var datos = new RegistrarEntregaRequest(21L, 2L, "Verificacion manual",
                true, true, 44L);
        assertThrows(ResourceInUseException.class,
                () -> servicio.registrar(datos, archivoPrueba(), 4L));
        assertEquals(EstadoReportePerdida.REPORTADO, perdida.getEstado());
        verifyNoInteractions(imagenes);
        verify(perdidas, never()).saveAndFlush(any(ReportePerdida.class));
    }

    @Test
    void entregaConCoincidenciaConfirmadaMarcaPerdidaRecuperada() {
        var personal = new Personal();
        personal.setEstado(EstadoPersonal.ACTIVO);
        var receptor = new Persona();
        receptor.setIdPersona(2L);
        var objeto = new Objeto();
        objeto.setIdObjeto(8L);
        objeto.setNombre("Guitarra de prueba");
        var hallazgo = new ReporteHallazgos();
        hallazgo.setIdHallazgo(33L);
        var custodia = new ObjetosCustodiados();
        custodia.setIdCustodia(21L);
        custodia.setObjeto(objeto);
        custodia.setHallazgo(hallazgo);
        var perdida = new ReportePerdida();
        perdida.setIdReportePerdida(66L);
        perdida.setPersona(receptor);
        perdida.setEstado(EstadoReportePerdida.REPORTADO);
        var coincidencia = new Coincidencia();
        coincidencia.setIdCoincidencia(44L);
        coincidencia.setEstado(EstadoCoincidencia.CONFIRMADA);
        coincidencia.setHallazgo(hallazgo);
        coincidencia.setReportePerdida(perdida);

        when(personales.findById(4L)).thenReturn(Optional.of(personal));
        when(personas.findById(2L)).thenReturn(Optional.of(receptor));
        when(custodias.bloquearParaEntrega(21L)).thenReturn(Optional.of(custodia));
        when(coincidencias.findById(44L)).thenReturn(Optional.of(coincidencia));
        when(perdidas.bloquearParaEntrega(66L)).thenReturn(Optional.of(perdida));
        when(imagenes.guardar(any())).thenReturn("evidencia-ficticia");
        when(entregas.saveAndFlush(any(Entrega.class))).thenAnswer(inv -> {
            Entrega e = inv.getArgument(0);
            e.setIdEntrega(99L);
            return e;
        });
        when(perdidas.saveAndFlush(any(ReportePerdida.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionSynchronizationManager.initSynchronization();
        try {
            var datos = new RegistrarEntregaRequest(21L, 2L,
                    "Entrega fisica verificada", true, true, 44L);
            var respuesta = servicio.registrar(datos, archivoPrueba(), 4L);

            assertEquals(EstadoReportePerdida.RECUPERADO, perdida.getEstado());
            assertEquals(44L, respuesta.idCoincidencia());
            assertEquals(66L, respuesta.idReportePerdida());
            verify(perdidas).saveAndFlush(perdida);
            verify(entregas).saveAndFlush(argThat(e ->
                    e.getCoincidencia() == coincidencia && e.getReportePerdida() == perdida));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void entregaDirectaSinCoincidenciaNoModificaPerdidas() {
        var personal = new Personal();
        personal.setEstado(EstadoPersonal.ACTIVO);
        var receptor = new Persona();
        receptor.setIdPersona(2L);
        var objeto = new Objeto();
        objeto.setIdObjeto(8L);
        objeto.setNombre("Guitarra de prueba");
        var custodia = new ObjetosCustodiados();
        custodia.setIdCustodia(21L);
        custodia.setObjeto(objeto);

        when(personales.findById(4L)).thenReturn(Optional.of(personal));
        when(personas.findById(2L)).thenReturn(Optional.of(receptor));
        when(custodias.bloquearParaEntrega(21L)).thenReturn(Optional.of(custodia));
        when(imagenes.guardar(any())).thenReturn("evidencia-ficticia");
        when(entregas.saveAndFlush(any(Entrega.class))).thenAnswer(inv -> {
            Entrega e = inv.getArgument(0);
            e.setIdEntrega(99L);
            return e;
        });

        TransactionSynchronizationManager.initSynchronization();
        try {
            var respuesta = servicio.registrar(solicitud(true, true), archivoPrueba(), 4L);
            assertNull(respuesta.idCoincidencia());
            assertNull(respuesta.idReportePerdida());
            verifyNoInteractions(perdidas, coincidencias);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
