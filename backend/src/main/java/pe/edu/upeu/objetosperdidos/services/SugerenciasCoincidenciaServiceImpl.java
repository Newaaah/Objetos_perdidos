package pe.edu.upeu.objetosperdidos.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.dto.ResultadoSugerenciasDTO;
import pe.edu.upeu.objetosperdidos.dto.SugerenciaCoincidenciaDTO;
import pe.edu.upeu.objetosperdidos.entity.EstadoReportePerdida;
import pe.edu.upeu.objetosperdidos.entity.Objeto;
import pe.edu.upeu.objetosperdidos.entity.ObjetosCustodiados;
import pe.edu.upeu.objetosperdidos.entity.ReporteHallazgos;
import pe.edu.upeu.objetosperdidos.entity.ReportePerdida;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.repository.CandidatosCoincidenciaRepository;

/** Calcula sugerencias para MIS pérdidas; no registra coincidencias ni entregas. */
@Service
public class SugerenciasCoincidenciaServiceImpl implements ISugerenciasCoincidenciaService {

    private static final int MAX_RESULTADOS = 5;
    // Valores orientativos: recalibrar con casos de evaluación UPeU.
    private static final int MIN_COBERTURA = 45;
    private static final int MIN_SIMILITUD = 45;
    private static final int MIN_PRIORIDAD = 40;
    private static final String AVISO = "Sugerencias orientativas; "
            + "no confirman propiedad ni autorizan una devolucion. "
            + "La verificacion corresponde al personal de UPeU.";

    private final CandidatosCoincidenciaRepository candidatos;
    private final ComparadorObjetosService comparador;

    public SugerenciasCoincidenciaServiceImpl(CandidatosCoincidenciaRepository candidatos,
                                         ComparadorObjetosService comparador) {
        this.candidatos = candidatos;
        this.comparador = comparador;
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoSugerenciasDTO sugerir(Long idReporte, Long idPersona) {
        if (idReporte == null || idReporte <= 0 || idPersona == null || idPersona <= 0) {
            throw new IllegalArgumentException("Identificadores invalidos");
        }
        // El ID de persona procede exclusivamente del principal autenticado.
        ReportePerdida perdida = candidatos.buscarPerdidaPropia(idReporte, idPersona)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reporte de perdida no encontrado para el usuario autenticado"));

        if (perdida.getEstado() != EstadoReportePerdida.REPORTADO) {
            return new ResultadoSugerenciasDTO(idReporte,
                    perdida.getEstado().name(), List.of(), AVISO);
        }

        List<CandidatoEvaluado> evaluados = new ArrayList<>();
        Set<Long> hallazgosIncluidos = new HashSet<>();

        // Primero custodias: si ya hubo recepción, esa es la fuente canónica.
        for (ObjetosCustodiados custodia : candidatos.custodiasDisponibles()) {
            if (custodia.getObjeto() == null) continue;
            var resultado = comparador.comparar(perdida, custodia);
            if (!apto(resultado)) continue;
            ReporteHallazgos hallazgo = custodia.getHallazgo();
            if (hallazgo != null && hallazgo.getIdHallazgo() != null) {
                hallazgosIncluidos.add(hallazgo.getIdHallazgo());
            }
            evaluados.add(new CandidatoEvaluado(
                    "CUSTODIA", custodia.getIdCustodia(),
                    categoriaPublica(custodia.getObjeto()), resultado));
        }

        for (ReporteHallazgos hallazgo : candidatos.hallazgosSinCustodia()) {
            if (hallazgo.getObjeto() == null
                    || hallazgosIncluidos.contains(hallazgo.getIdHallazgo())) continue;
            var resultado = comparador.comparar(perdida, hallazgo);
            if (!apto(resultado)) continue;
            evaluados.add(new CandidatoEvaluado(
                    "HALLAZGO", hallazgo.getIdHallazgo(),
                    categoriaPublica(hallazgo.getObjeto()), resultado));
        }

        // El orden NO depende de IDs ni de información de otros propietarios.
        evaluados.sort(Comparator
                .comparingInt((CandidatoEvaluado x) -> x.resultado().prioridad()).reversed()
                .thenComparing(Comparator.comparingInt(
                        (CandidatoEvaluado x) -> x.resultado().cobertura()).reversed())
                .thenComparing(Comparator.comparingInt(
                        (CandidatoEvaluado x) -> x.resultado().similitud()).reversed())
                .thenComparing(CandidatoEvaluado::origen)
                .thenComparing(CandidatoEvaluado::idReferencia));

        List<SugerenciaCoincidenciaDTO> sugerencias = evaluados.stream()
                .limit(MAX_RESULTADOS)
                .map(x -> new SugerenciaCoincidenciaDTO(
                        x.origen(), x.idReferencia(), x.categoria(),
                        nivel(x.resultado().prioridad())))
                .toList();
        return new ResultadoSugerenciasDTO(idReporte, perdida.getEstado().name(),
                sugerencias, AVISO);
    }

    private boolean apto(ComparadorObjetosService.ResultadoComparacion r) {
        return r.cobertura() >= MIN_COBERTURA
                && r.similitud() >= MIN_SIMILITUD
                && r.prioridad() >= MIN_PRIORIDAD;
    }

    private String categoriaPublica(Objeto o) {
        return o.getCategoria() == null || o.getCategoria().getNombre() == null
                ? "Objeto" : o.getCategoria().getNombre();
    }

    private String nivel(int prioridad) {
        if (prioridad >= 75) return "ALTA";
        if (prioridad >= 55) return "MEDIA";
        return "BAJA";
    }

    private record CandidatoEvaluado(
            String origen, Long idReferencia, String categoria,
            ComparadorObjetosService.ResultadoComparacion resultado) { }
}
