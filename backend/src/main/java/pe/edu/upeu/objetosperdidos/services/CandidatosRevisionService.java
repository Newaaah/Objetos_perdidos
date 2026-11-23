package pe.edu.upeu.objetosperdidos.services;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.dto.CandidatosRevisionDTO;
import pe.edu.upeu.objetosperdidos.dto.MiHallazgoDTO;
import pe.edu.upeu.objetosperdidos.dto.MiPerdidaDTO;

/** Listas de perdidas y hallazgos para elegir en la revision de coincidencias. */
@Service
@Transactional(readOnly = true)
public class CandidatosRevisionService {

    private final EntityManager em;

    public CandidatosRevisionService(EntityManager em) {
        this.em = em;
    }

    public CandidatosRevisionDTO listar() {
        return new CandidatosRevisionDTO(perdidasAbiertas(), hallazgosAprobados());
    }

    // Reportes de perdida que siguen en busqueda
    private List<MiPerdidaDTO> perdidasAbiertas() {
        String sql = """
                SELECT P.ID_REPORTE_PERDIDA, O.ID_OBJETO, O.NOMBRE, C.NOMBRE,
                       P.ESTADO, P.FECHA_PERDIDA, P.FECHA_REPORTE, U.NOMBRE
                FROM UPEU_DATOS.REPORTE_PERDIDA P
                JOIN UPEU_DATOS.OBJETO O ON O.ID_OBJETO = P.ID_OBJETO
                JOIN UPEU_DATOS.CATEGORIA C ON C.ID_CATEGORIA = O.ID_CATEGORIA
                LEFT JOIN UPEU_DATOS.UBICACION U ON U.ID_UBICACION = P.ID_UBICACION_PERDIDA
                WHERE P.ESTADO = 'REPORTADO'
                ORDER BY P.FECHA_REPORTE DESC, P.ID_REPORTE_PERDIDA DESC
                """;
        @SuppressWarnings("unchecked")
        List<Object[]> filas = em.createNativeQuery(sql).getResultList();
        return filas.stream()
                .map(f -> new MiPerdidaDTO(num(f[0]), num(f[1]), txt(f[2]), txt(f[3]),
                        txt(f[4]), fecha(f[5]), fechaHora(f[6]), txt(f[7])))
                .toList();
    }

    // Hallazgos aprobados (en custodia o no) que todavia no fueron entregados
    private List<MiHallazgoDTO> hallazgosAprobados() {
        String sql = """
                SELECT H.ID_HALLAZGO, O.ID_OBJETO, O.NOMBRE, C.NOMBRE,
                       H.ESTADO, H.FECHA_REPORTE, U.NOMBRE
                FROM UPEU_DATOS.REPORTE_HALLAZGOS H
                JOIN UPEU_DATOS.OBJETO O ON O.ID_OBJETO = H.ID_OBJETO
                JOIN UPEU_DATOS.CATEGORIA C ON C.ID_CATEGORIA = O.ID_CATEGORIA
                LEFT JOIN UPEU_DATOS.UBICACION U ON U.ID_UBICACION = H.ID_UBICACION_DESTINO
                WHERE H.ESTADO = 'APROBADO'
                  AND NOT EXISTS (
                        SELECT 1
                        FROM UPEU_DATOS.OBJETOS_CUSTODIADOS OC
                        JOIN UPEU_DATOS.ENTREGA E ON E.ID_CUSTODIA = OC.ID_CUSTODIA
                        WHERE OC.ID_HALLAZGO = H.ID_HALLAZGO)
                ORDER BY H.FECHA_REPORTE DESC, H.ID_HALLAZGO DESC
                """;
        @SuppressWarnings("unchecked")
        List<Object[]> filas = em.createNativeQuery(sql).getResultList();
        return filas.stream()
                .map(f -> new MiHallazgoDTO(num(f[0]), num(f[1]), txt(f[2]), txt(f[3]),
                        txt(f[4]), fechaHora(f[5]), txt(f[6])))
                .toList();
    }

    // ===== Conversiones de Oracle =====
    private static Long num(Object v) {
        return v == null ? null : ((Number) v).longValue();
    }

    private static String txt(Object v) {
        return v == null ? null : v.toString();
    }

    private static LocalDate fecha(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDate d) return d;
        if (v instanceof LocalDateTime t) return t.toLocalDate();
        if (v instanceof java.sql.Date d) return d.toLocalDate();
        if (v instanceof java.util.Date d) return new java.sql.Date(d.getTime()).toLocalDate();
        return null;
    }

    private static LocalDateTime fechaHora(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime t) return t;
        if (v instanceof LocalDate d) return d.atStartOfDay();
        if (v instanceof Timestamp t) return t.toLocalDateTime();
        if (v instanceof java.sql.Date d) return d.toLocalDate().atStartOfDay();
        if (v instanceof java.util.Date d) return new Timestamp(d.getTime()).toLocalDateTime();
        return null;
    }
}