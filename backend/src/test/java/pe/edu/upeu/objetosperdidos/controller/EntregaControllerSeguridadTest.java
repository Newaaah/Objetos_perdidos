package pe.edu.upeu.objetosperdidos.controller;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import pe.edu.upeu.objetosperdidos.config.SecurityConfig;
import pe.edu.upeu.objetosperdidos.dto.EntregaRegistradaDTO;
import pe.edu.upeu.objetosperdidos.dto.EstadoCustodiaEntregaDTO;
import pe.edu.upeu.objetosperdidos.dto.RegistrarEntregaRequest;
import pe.edu.upeu.objetosperdidos.entity.RolUsuario;
import pe.edu.upeu.objetosperdidos.exception.CustomAccessDeniedHandler;
import pe.edu.upeu.objetosperdidos.exception.CustomAuthenticationEntryPoint;
import pe.edu.upeu.objetosperdidos.security.JwtAuthenticationFilter;
import pe.edu.upeu.objetosperdidos.security.JwtService;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.AlmacenamientoImagenEntregaService;
import pe.edu.upeu.objetosperdidos.services.IEntregaService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas HTTP sobre SecurityConfig + EntregaController, SIN Oracle ni archivos reales.
 * Los usuarios son ficticios y se insertan solo en el contexto de prueba.
 */
@WebMvcTest(controllers = EntregaController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
class EntregaControllerSeguridadTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private IEntregaService servicio;

    // La cadena JWT existe, pero no consultamos usuarios ni generamos tokens reales.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    private static UsuarioPrincipal usuario(RolUsuario rol) {
        long id = switch (rol) {
            case ESTUDIANTE -> 1L;
            case PERSONAL -> 3L;
            case ADMIN -> 4L;
        };
        return new UsuarioPrincipal(id, "prueba-" + rol, "hash-ficticio", rol, true);
    }

    private static MockMultipartFile datosValidos() {
        String json = """
                {
                  "idCustodia": 21,
                  "idPersonaRecibe": 2,
                  "observacionVerificacion": "SIMULACION DE PRUEBA AUTOMATIZADA",
                  "confirmoVerificacionPresencial": true,
                  "confirmoEntregaFisica": true
                }
                """;
        return new MockMultipartFile("datos", "", "text/plain", json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static MockMultipartFile fotoFicticia() {
        return new MockMultipartFile("fotografia", "prueba.png", "image/png", new byte[] {1, 2, 3});
    }

    @Test
    void sinAutenticacionNoPuedeConsultarEstadoDeCustodia() throws Exception {
        mvc.perform(get("/api/v1/entregas/custodias/21/estado"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(servicio);
    }

    @Test
    void estudianteNoPuedeConsultarEstadoDeCustodia() throws Exception {
        mvc.perform(get("/api/v1/entregas/custodias/21/estado")
                        .with(user(usuario(RolUsuario.ESTUDIANTE))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(servicio);
    }

    @Test
    void estudianteNoPuedeConsultarEntregaInterna() throws Exception {
        mvc.perform(get("/api/v1/entregas/21")
                        .with(user(usuario(RolUsuario.ESTUDIANTE))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(servicio);
    }

    @Test
    void estudianteNoPuedeVerEvidenciaPrivada() throws Exception {
        mvc.perform(get("/api/v1/entregas/21/evidencia")
                        .with(user(usuario(RolUsuario.ESTUDIANTE))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(servicio);
    }

    @Test
    void estudianteNoPuedeRegistrarEntrega() throws Exception {
        mvc.perform(multipart("/api/v1/entregas")
                        .file(datosValidos()).file(fotoFicticia())
                        .with(user(usuario(RolUsuario.ESTUDIANTE))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(servicio);
    }

    @Test
    void personalPuedeConsultarEstadoDeCustodia() throws Exception {
        when(servicio.estadoCustodia(21L))
                .thenReturn(new EstadoCustodiaEntregaDTO(21L, 22L, "Mochila de prueba", true, null));
        mvc.perform(get("/api/v1/entregas/custodias/21/estado")
                        .with(user(usuario(RolUsuario.PERSONAL))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponibleParaEntrega").value(true));
        verify(servicio).estadoCustodia(21L);
    }

    @Test
    void adminPuedeConsultarEstadoDeCustodia() throws Exception {
        when(servicio.estadoCustodia(21L))
                .thenReturn(new EstadoCustodiaEntregaDTO(21L, 22L, "Mochila de prueba", true, null));
        mvc.perform(get("/api/v1/entregas/custodias/21/estado")
                        .with(user(usuario(RolUsuario.ADMIN))))
                .andExpect(status().isOk());
        verify(servicio).estadoCustodia(21L);
    }

    @Test
    void personalPuedeRegistrarYSuIdentidadSeTomaDeLaSesion() throws Exception {
        when(servicio.registrar(any(RegistrarEntregaRequest.class), any(), eq(3L)))
                .thenReturn(entregaFicticia(3L));
        mvc.perform(multipart("/api/v1/entregas")
                        .file(datosValidos()).file(fotoFicticia())
                        .with(user(usuario(RolUsuario.PERSONAL))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPersonalEntrega").value(3));
        verify(servicio).registrar(any(RegistrarEntregaRequest.class), any(), eq(3L));
    }

    @Test
    void adminPuedeRegistrarYSuIdentidadSeTomaDeLaSesion() throws Exception {
        when(servicio.registrar(any(RegistrarEntregaRequest.class), any(), eq(4L)))
                .thenReturn(entregaFicticia(4L));
        mvc.perform(multipart("/api/v1/entregas")
                        .file(datosValidos()).file(fotoFicticia())
                        .with(user(usuario(RolUsuario.ADMIN))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPersonalEntrega").value(4));
        verify(servicio).registrar(any(RegistrarEntregaRequest.class), any(), eq(4L));
    }

    @Test
    void personalPuedeConsultarFotoDeEvidencia() throws Exception {
        byte[] imagen = new byte[] {1, 2, 3, 4};
        when(servicio.leerEvidencia(21L))
                .thenReturn(new AlmacenamientoImagenEntregaService.Imagen(imagen, MediaType.IMAGE_PNG));
        mvc.perform(get("/api/v1/entregas/21/evidencia")
                        .with(user(usuario(RolUsuario.PERSONAL))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(imagen));
        verify(servicio).leerEvidencia(21L);
    }

    private static EntregaRegistradaDTO entregaFicticia(long idPersonal) {
        return new EntregaRegistradaDTO(99L, 21L, 22L, "Mochila de prueba", 2L,
                idPersonal, LocalDateTime.of(2026, 11, 20, 11, 30),
                "SIMULACION DE PRUEBA AUTOMATIZADA", "entregas/imagen-ficticia.png");
    }
}
