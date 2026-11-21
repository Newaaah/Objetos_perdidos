package pe.edu.upeu.objetosperdidos.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas HTTP simuladas: no conectan con Oracle ni alteran datos reales.
 */
class GlobalExceptionHandlerTest {

    @RestController
    static class ControladorDePrueba {

        @PostMapping("/pruebas/coincidencia/hallazgo-repetido")
        public void hallazgoRepetido() {
            throw new DataIntegrityViolationException(
                    "ORA-00001: unique constraint (UPEU_DATOS.UX_COINC_CONF_HALL) violated");
        }

        @PostMapping("/pruebas/coincidencia/perdida-repetida")
        public void perdidaRepetida() {
            throw new DataIntegrityViolationException(
                    "ORA-00001: unique constraint (UPEU_DATOS.UX_COINC_CONF_PERD) violated");
        }

        @PostMapping("/pruebas/coincidencia/conflicto-negocio")
        public void conflictoNegocio() {
            throw new ResourceInUseException("Este hallazgo ya tiene una coincidencia CONFIRMADA");
        }

        @PostMapping("/pruebas/coincidencia/error-interno")
        public void errorInterno() {
            throw new IllegalStateException("Detalle interno que no debe mostrarse");
        }
    }

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new ControladorDePrueba())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void indiceUnicoDeHallazgoDevuelve409SinExponerOracle() throws Exception {
        verificarConflictoIntegridad("/pruebas/coincidencia/hallazgo-repetido");
    }

    @Test
    void indiceUnicoDePerdidaDevuelve409SinExponerOracle() throws Exception {
        verificarConflictoIntegridad("/pruebas/coincidencia/perdida-repetida");
    }

    @Test
    void conflictoDeReglaDeNegocioDevuelve409() throws Exception {
        mvc.perform(post("/pruebas/coincidencia/conflicto-negocio"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Este hallazgo ya tiene una coincidencia CONFIRMADA"));
    }

    @Test
    void falloInesperadoDevuelve500SinExponerDetalles() throws Exception {
        mvc.perform(post("/pruebas/coincidencia/error-interno"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message")
                        .value("Ocurrio un error interno en el servidor"));
    }

    private void verificarConflictoIntegridad(String ruta) throws Exception {
        mvc.perform(post(ruta))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "La operacion incumple una restriccion "
                                + "de integridad de la base de datos"));
    }
}
