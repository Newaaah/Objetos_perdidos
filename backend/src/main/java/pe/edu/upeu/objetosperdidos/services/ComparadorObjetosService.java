package pe.edu.upeu.objetosperdidos.services;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import pe.edu.upeu.objetosperdidos.entity.Objeto;
import pe.edu.upeu.objetosperdidos.entity.ObjetosCustodiados;
import pe.edu.upeu.objetosperdidos.entity.ReporteHallazgos;
import pe.edu.upeu.objetosperdidos.entity.ReportePerdida;
import pe.edu.upeu.objetosperdidos.entity.Ubicacion;

/**
 * Compara descripciones de objetos y su contexto sin modificar datos.
 * Los valores son puntajes heurísticos, NO probabilidades de propiedad.
 * Los pesos son provisionales: requieren calibración con casos UPeU.
 */
@Service
public class ComparadorObjetosService {

    private static final int PESO_NOMBRE = 30;
    private static final int PESO_CATEGORIA = 25;
    private static final int PESO_MARCA = 15;
    private static final int PESO_COLOR = 15;
    private static final int PESO_TIEMPO = 10;
    private static final int PESO_UBICACION = 5;

    private static final Map<String, String> SINONIMOS = Map.ofEntries(
            Map.entry("audifonos", "audifono"),
            Map.entry("auriculares", "audifono"),
            Map.entry("auricular", "audifono"),
            Map.entry("headphones", "audifono"),
            Map.entry("inalambricos", "inalambrico"),
            Map.entry("inalambrica", "inalambrico"),
            Map.entry("inalambricas", "inalambrico"),
            Map.entry("bluetooth", "inalambrico"),
            Map.entry("celular", "telefono"),
            Map.entry("movil", "telefono"),
            Map.entry("smartphone", "telefono"),
            Map.entry("morral", "mochila"),
            Map.entry("negra", "negro"),
            Map.entry("roja", "rojo"),
            Map.entry("blanca", "blanco"),
            Map.entry("azules", "azul")
    );

    private static final Set<String> PALABRAS_COMUNES = Set.of(
            "un", "una", "unos", "unas", "el", "la", "los", "las",
            "en", "de", "del", "con", "y", "por", "para", "al", "mi", "mis"
    );
    private static final Set<String> DESCONOCIDOS = Set.of(
            "desconocido", "desconocida", "ninguna", "ninguno", "no aplica",
            "sin especificar", "sin marca", "n a", "na", "s m"
    );

    /** Solo características: conserva compatibilidad con el comparador anterior. */
    public ResultadoComparacion comparar(Objeto perdido, Objeto candidato) {
        return compararInterno(perdido, candidato, null, null, null, null);
    }

    /** Pérdida contra hallazgo: fechaReporte es una referencia, NO fechaEncontrado. */
    public ResultadoComparacion comparar(ReportePerdida perdida, ReporteHallazgos hallazgo) {
        Objects.requireNonNull(perdida, "El reporte de pérdida es obligatorio");
        Objects.requireNonNull(hallazgo, "El hallazgo es obligatorio");
        return compararInterno(perdida.getObjeto(), hallazgo.getObjeto(),
                perdida.getFechaPerdida(), hallazgo.getFechaReporte(),
                perdida.getUbicacionPerdida(), hallazgo.getUbicacionEncontrado());
    }

    /**
     * Pérdida contra custodia: NO usa ubicacionActual, que puede ser Mesa de Partes.
     * Si existe hallazgo, prioriza su fecha de reporte; de otro modo usa la
     * fecha de recepción en custodia (también aproximada).
     */
    public ResultadoComparacion comparar(ReportePerdida perdida, ObjetosCustodiados custodia) {
        Objects.requireNonNull(perdida, "El reporte de pérdida es obligatorio");
        Objects.requireNonNull(custodia, "La custodia es obligatoria");
        ReporteHallazgos hallazgo = custodia.getHallazgo();
        LocalDateTime fechaReferencia = hallazgo != null && hallazgo.getFechaReporte() != null
                ? hallazgo.getFechaReporte() : custodia.getFechaRegistro();
        Ubicacion encontrado = custodia.getUbicacionEncontrado();
        if (encontrado == null && hallazgo != null) {
            encontrado = hallazgo.getUbicacionEncontrado();
        }
        return compararInterno(perdida.getObjeto(), custodia.getObjeto(),
                perdida.getFechaPerdida(), fechaReferencia,
                perdida.getUbicacionPerdida(), encontrado);
    }

    private ResultadoComparacion compararInterno(
            Objeto perdido, Objeto candidato, LocalDate fechaPerdida,
            LocalDateTime fechaReferencia, Ubicacion ubicacionPerdida,
            Ubicacion ubicacionEncontrado) {

        Objects.requireNonNull(perdido, "El objeto perdido es obligatorio");
        Objects.requireNonNull(candidato, "El objeto candidato es obligatorio");

        List<DetalleCriterio> detalles = new ArrayList<>();
        agregar(detalles, "nombre", PESO_NOMBRE,
                compararTexto(perdido.getNombre(), candidato.getNombre()));
        agregar(detalles, "categoria", PESO_CATEGORIA,
                compararIdentificador(
                        perdido.getCategoria() == null ? null : perdido.getCategoria().getIdCategoria(),
                        candidato.getCategoria() == null ? null : candidato.getCategoria().getIdCategoria()));
        agregar(detalles, "marca", PESO_MARCA,
                compararTexto(perdido.getMarca(), candidato.getMarca()));
        agregar(detalles, "color", PESO_COLOR,
                compararTexto(perdido.getColor(), candidato.getColor()));
        agregar(detalles, "tiempo", PESO_TIEMPO,
                compatibilidadTemporal(fechaPerdida, fechaReferencia));
        agregar(detalles, "ubicacion", PESO_UBICACION,
                compararIdentificador(
                        ubicacionPerdida == null ? null : ubicacionPerdida.getIdUbicacion(),
                        ubicacionEncontrado == null ? null : ubicacionEncontrado.getIdUbicacion()));

        int pesoComparable = 0;
        double puntosObtenidos = 0;
        for (DetalleCriterio detalle : detalles) {
            if (detalle.proporcion() != null) {
                pesoComparable += detalle.peso();
                puntosObtenidos += detalle.peso() * detalle.proporcion();
            }
        }
        // Cuando hay poca información, la similitud puede parecer alta;
        // la cobertura y la prioridad dejan visible esa incertidumbre.
        int similitud = pesoComparable == 0 ? 0
                : (int) Math.round(100.0 * puntosObtenidos / pesoComparable);
        int cobertura = pesoComparable;
        int prioridad = (int) Math.round(similitud * (0.5 + 0.5 * cobertura / 100.0));
        return new ResultadoComparacion(similitud, cobertura, prioridad, List.copyOf(detalles));
    }

    private void agregar(List<DetalleCriterio> detalles, String nombre, int peso, Double proporcion) {
        String evaluacion;
        if (proporcion == null) {
            evaluacion = "SIN_DATOS";
        } else if (proporcion >= 0.85) {
            evaluacion = "COMPATIBLE";
        } else if (proporcion > 0) {
            evaluacion = "PARCIAL";
        } else {
            evaluacion = "DISTINTO";
        }
        detalles.add(new DetalleCriterio(nombre, peso, proporcion, evaluacion));
    }

    private Double compararIdentificador(Long a, Long b) {
        if (a == null || b == null) {
            return null;
        }
        return a.equals(b) ? 1.0 : 0.0;
    }

    private Double compararTexto(String primero, String segundo) {
        Set<String> a = palabras(primero);
        Set<String> b = palabras(segundo);
        if (a.isEmpty() || b.isEmpty()) {
            return null; // Dato ausente, NO dato contradictorio.
        }
        if (a.equals(b)) {
            return 1.0;
        }
        Set<String> comunes = new HashSet<>(a);
        comunes.retainAll(b);
        return 2.0 * comunes.size() / (a.size() + b.size());
    }

    /**
     * Fecha del hallazgo/custodia = fecha de registro disponible; NO necesariamente
     * fecha real de hallazgo. Un lapso largo no descalifica automáticamente.
     */
    private Double compatibilidadTemporal(LocalDate fechaPerdida, LocalDateTime fechaReferencia) {
        if (fechaPerdida == null || fechaReferencia == null) {
            return null;
        }
        long dias = ChronoUnit.DAYS.between(fechaPerdida, fechaReferencia.toLocalDate());
        if (dias < 0) return 0.0;
        if (dias <= 7) return 1.0;
        if (dias <= 30) return 0.80;
        if (dias <= 90) return 0.60;
        return 0.40;
    }

    private Set<String> palabras(String texto) {
        if (texto == null || texto.isBlank()) {
            return Set.of();
        }
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ").trim();
        if (normalizado.isBlank() || DESCONOCIDOS.contains(normalizado)) {
            return Set.of();
        }
        return Arrays.stream(normalizado.split("\\s+"))
                .filter(p -> !PALABRAS_COMUNES.contains(p))
                .map(p -> SINONIMOS.getOrDefault(p, p))
                .collect(Collectors.toSet());
    }

    /** No contiene nombres, descripciones privadas ni datos de personas. */
    public record DetalleCriterio(String criterio, int peso, Double proporcion, String evaluacion) { }

    /** Puntuaciones heurísticas 0-100; prioridad combina similitud y cobertura. */
    public record ResultadoComparacion(int similitud, int cobertura,
                                        int prioridad, List<DetalleCriterio> criterios) { }
}
