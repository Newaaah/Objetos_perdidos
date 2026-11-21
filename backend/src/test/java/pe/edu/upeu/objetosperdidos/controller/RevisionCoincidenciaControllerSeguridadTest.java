package pe.edu.upeu.objetosperdidos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import pe.edu.upeu.objetosperdidos.config.SecurityConfig;
import pe.edu.upeu.objetosperdidos.dto.CoincidenciaRevisadaDTO;
import pe.edu.upeu.objetosperdidos.dto.RevisarCoincidenciaRequest;
import pe.edu.upeu.objetosperdidos.entity.RolUsuario;
import pe.edu.upeu.objetosperdidos.exception.CustomAccessDeniedHandler;
import pe.edu.upeu.objetosperdidos.exception.CustomAuthenticationEntryPoint;
import pe.edu.upeu.objetosperdidos.security.JwtAuthenticationFilter;
import pe.edu.upeu.objetosperdidos.security.JwtService;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.IRevisionCoincidenciaService;

/** Prueba las reglas HTTP reales sin conectarse a Oracle. */
@WebMvcTest(controllers = RevisionCoincidenciaController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
class RevisionCoincidenciaControllerSeguridadTest {

    @Autowired MockMvc mvc;
    @MockitoBean IRevisionCoincidenciaService servicio;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean AuthenticationProvider authenticationProvider;

    private static UsuarioPrincipal usuario(RolUsuario rol) {
        long id = switch (rol) {
            case ESTUDIANTE -> 2L;
            case PERSONAL -> 3L;
            case ADMIN -> 4L;
        };
        return new UsuarioPrincipal(id, "prueba-" + rol, "hash-ficticio", rol, true);
    }

    private static String jsonValido() {
        return """
            {
              "idReportePerdida": 21,
              "idHallazgo": 22,
              "decision": "CONFIRMADA",
              "observacion": "SIMULACION: revision formal de coincidencia"
            }
            """;
    }

    private static CoincidenciaRevisadaDTO resultado(long idRevisor) {
        return new CoincidenciaRevisadaDTO(50L, 21L, 22L, "CONFIRMADA",
                84, idRevisor, LocalDateTime.of(2026, 11, 20, 15, 0), "SIMULACION");
    }

    @Test void anonimoNoPuedeEvaluar() throws Exception {
        mvc.perform(get("/api/v1/coincidencias/revision/evaluar")
                    .param("idReportePerdida", "21").param("idHallazgo", "22"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(servicio);
    }

    @Test void estudianteNoPuedeEvaluar() throws Exception {
        mvc.perform(get("/api/v1/coincidencias/revision/evaluar")
                    .param("idReportePerdida", "21").param("idHallazgo", "22")
                    .with(user(usuario(RolUsuario.ESTUDIANTE))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(servicio);
    }

    @Test void estudianteNoPuedeRegistrarDecision() throws Exception {
        mvc.perform(post("/api/v1/coincidencias/revision")
                    .contentType(MediaType.APPLICATION_JSON).content(jsonValido())
                    .with(user(usuario(RolUsuario.ESTUDIANTE))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(servicio);
    }

    @Test void personalPuedeEvaluar() throws Exception {
        mvc.perform(get("/api/v1/coincidencias/revision/evaluar")
                    .param("idReportePerdida", "21").param("idHallazgo", "22")
                    .with(user(usuario(RolUsuario.PERSONAL))))
                .andExpect(status().isOk());
        verify(servicio).evaluar(21L, 22L);
    }

    @Test void adminPuedeEvaluar() throws Exception {
        mvc.perform(get("/api/v1/coincidencias/revision/evaluar")
                    .param("idReportePerdida", "21").param("idHallazgo", "22")
                    .with(user(usuario(RolUsuario.ADMIN))))
                .andExpect(status().isOk());
        verify(servicio).evaluar(21L, 22L);
    }

    @Test void personalRegistraUsandoIdDelJwt() throws Exception {
        when(servicio.registrar(any(RevisarCoincidenciaRequest.class), eq(3L)))
                .thenReturn(resultado(3L));
        mvc.perform(post("/api/v1/coincidencias/revision")
                    .contentType(MediaType.APPLICATION_JSON).content(jsonValido())
                    .with(user(usuario(RolUsuario.PERSONAL))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPersonalRevisor").value(3));
        verify(servicio).registrar(any(RevisarCoincidenciaRequest.class), eq(3L));
    }

    @Test void adminRegistraUsandoIdDelJwt() throws Exception {
        when(servicio.registrar(any(RevisarCoincidenciaRequest.class), eq(4L)))
                .thenReturn(resultado(4L));
        mvc.perform(post("/api/v1/coincidencias/revision")
                    .contentType(MediaType.APPLICATION_JSON).content(jsonValido())
                    .with(user(usuario(RolUsuario.ADMIN))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPersonalRevisor").value(4));
        verify(servicio).registrar(any(RevisarCoincidenciaRequest.class), eq(4L));
    }

    @Test void solicitudSinObservacionEsInvalida() throws Exception {
        String json = """
                {"idReportePerdida":21,"idHallazgo":22,
                 "decision":"CONFIRMADA","observacion":""}
                """;
        mvc.perform(post("/api/v1/coincidencias/revision")
                    .contentType(MediaType.APPLICATION_JSON).content(json)
                    .with(user(usuario(RolUsuario.ADMIN))))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(servicio);
    }
}
