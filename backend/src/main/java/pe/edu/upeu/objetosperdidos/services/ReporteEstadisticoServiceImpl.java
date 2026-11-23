package pe.edu.upeu.objetosperdidos.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.dto.*;
import pe.edu.upeu.objetosperdidos.entity.EstadoReporteHallazgo;
import pe.edu.upeu.objetosperdidos.entity.EstadoReportePerdida;
import pe.edu.upeu.objetosperdidos.entity.TipoReclamo;
import pe.edu.upeu.objetosperdidos.repository.*;
import pe.edu.upeu.objetosperdidos.services.IReporteEstadisticoService;

@Service
@Transactional(readOnly = true)
public class ReporteEstadisticoServiceImpl implements IReporteEstadisticoService {

    private final ReportePerdidaRepository perdidas;
    private final ReporteHallazgosRepository hallazgos;
    private final ObjetosCustodiadosRepository custodias;
    private final EntregaRepository entregas;
    private final ReclamosRepository reclamos;
    private final ResumenReclamosProcedimientoRepository procedimiento;

    public ReporteEstadisticoServiceImpl(
            ReportePerdidaRepository perdidas,
            ReporteHallazgosRepository hallazgos,
            ObjetosCustodiadosRepository custodias,
            EntregaRepository entregas,
            ReclamosRepository reclamos,
            ResumenReclamosProcedimientoRepository procedimiento) {
        this.perdidas = perdidas;
        this.hallazgos = hallazgos;
        this.custodias = custodias;
        this.entregas = entregas;
        this.reclamos = reclamos;
        this.procedimiento = procedimiento;
    }

    @Override
    public List<ConteoEstadoDTO> perdidasPorEstado() {
        return estados(perdidas.reportePerdidasPorEstado());
    }

    // NUEVO: "estado" lleva el nombre de la zona del campus
    @Override
    public List<ConteoEstadoDTO> perdidasPorUbicacion() {
        return estados(perdidas.reportePerdidasPorZona());
    }

    @Override
    public List<ConteoEstadoDTO> hallazgosPorEstado() {
        return estados(hallazgos.reporteHallazgosPorEstado());
    }

    @Override
    public List<ConteoEstadoDTO> custodiasPorSituacion() {
        return estados(custodias.reporteCustodiasPorSituacion());
    }

    @Override
    public List<PorcentajeEntregasCategoriaDTO> entregasPorCategoria() {
        return custodias.reportePorcentajeEntregasPorCategoria().stream()
                .map(f -> new PorcentajeEntregasCategoriaDTO(
                        num(f[0]), txt(f[1]), number(f[2]), number(f[3]), decimal(f[4])))
                .toList();
    }

    @Override
    public List<ConteoMesDTO> perdidasPorMes() {
        return meses(perdidas.reportePerdidasPorMes());
    }

    @Override
    public List<ConteoMesDTO> entregasPorMes() {
        return meses(entregas.reporteEntregasPorMes());
    }

    @Override
    public List<ConteoEstadoDTO> reclamosPorEstado() {
        return estados(reclamos.reporteReclamosPorEstado());
    }

    @Override
    public ResumenReclamosDTO resumenReclamosDesdeProcedimiento() {
        return procedimiento.ejecutarResumenReclamos();
    }

    @Override
    public List<SeguimientoReclamoDTO> seguimientoDesdeVista() {
        return reclamos.consultarVistaSeguimientoReclamos().stream()
                .map(f -> new SeguimientoReclamoDTO(
                        num(f[0]), num(f[1]), txt(f[2]), txt(f[3]), txt(f[4]),
                        fechaHora(f[5]), fechaHora(f[6]), txt(f[7])))
                .toList();
    }

    @Override
    public List<PendienteRecepcionDTO> pendientesDeRecepcion(Long idUbicacion) {
        return hallazgos.listarPendientesRecepcion(idUbicacion).stream()
                .map(f -> new PendienteRecepcionDTO(num(f[0]), txt(f[1]), txt(f[2]), fechaHora(f[3])))
                .toList();
    }

    @Override
    public List<CustodiaDisponibleDTO> custodiasDisponibles(Long idUbicacion) {
        return custodias.listarCustodiasDisponiblesEnUbicacion(idUbicacion).stream()
                .map(f -> new CustodiaDisponibleDTO(num(f[0]), txt(f[1]), txt(f[2]), txt(f[3]), fechaHora(f[4])))
                .toList();
    }

    @Override
    public ResumenEstudianteDTO resumenEstudiante(Long idPersona) {
        return new ResumenEstudianteDTO(
                perdidas.countByPersonaIdPersona(idPersona),
                perdidas.countByPersonaIdPersonaAndEstado(idPersona, EstadoReportePerdida.REPORTADO),
                perdidas.countByPersonaIdPersonaAndEstado(idPersona, EstadoReportePerdida.RECUPERADO),
                hallazgos.countByPersonaIdPersona(idPersona),
                hallazgos.countByPersonaIdPersonaAndEstado(idPersona, EstadoReporteHallazgo.APROBADO),
                reclamos.countByPersonaReclamaIdPersonaAndTipoReclamo(idPersona, TipoReclamo.RECLAMO)
        );
    }

    @Override
    public List<MiPerdidaDTO> misPerdidas(Long idPersona) {
        return perdidas.listarPerdidasDePersona(idPersona).stream()
                .map(f -> new MiPerdidaDTO(num(f[0]), num(f[1]), txt(f[2]), txt(f[3]),
                        txt(f[4]), fecha(f[5]), fechaHora(f[6]), txt(f[7])))
                .toList();
    }

    @Override
    public List<MiHallazgoDTO> misHallazgos(Long idPersona) {
        return hallazgos.listarHallazgosDePersona(idPersona).stream()
                .map(f -> new MiHallazgoDTO(num(f[0]), num(f[1]), txt(f[2]), txt(f[3]),
                        txt(f[4]), fechaHora(f[5]), txt(f[6])))
                .toList();
    }

    private List<ConteoEstadoDTO> estados(List<Object[]> filas) {
        return filas.stream().map(f -> new ConteoEstadoDTO(txt(f[0]), number(f[1]))).toList();
    }

    private List<ConteoMesDTO> meses(List<Object[]> filas) {
        return filas.stream().map(f -> new ConteoMesDTO(txt(f[0]), number(f[1]))).toList();
    }

    private static Long num(Object valor) {
        return valor == null ? null : ((Number) valor).longValue();
    }

    private static long number(Object valor) {
        return valor == null ? 0L : ((Number) valor).longValue();
    }

    private static String txt(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private static BigDecimal decimal(Object valor) {
        return valor == null ? BigDecimal.ZERO : new BigDecimal(valor.toString());
    }

    private static LocalDate fecha(Object valor) {
        if (valor == null) return null;
        if (valor instanceof LocalDate d) return d;
        if (valor instanceof LocalDateTime t) return t.toLocalDate();
        if (valor instanceof java.sql.Date d) return d.toLocalDate();
        if (valor instanceof java.util.Date d)
            return new java.sql.Date(d.getTime()).toLocalDate();
        throw new IllegalArgumentException("Tipo de fecha Oracle inesperado: " + valor.getClass().getName());
    }

    private static LocalDateTime fechaHora(Object valor) {
        if (valor == null) return null;
        if (valor instanceof LocalDateTime t) return t;
        if (valor instanceof LocalDate d) return d.atStartOfDay();
        if (valor instanceof Timestamp t) return t.toLocalDateTime();
        if (valor instanceof java.sql.Date d) return d.toLocalDate().atStartOfDay();
        if (valor instanceof java.util.Date d) return new Timestamp(d.getTime()).toLocalDateTime();
        throw new IllegalArgumentException("Tipo de fecha Oracle inesperado: " + valor.getClass().getName());
    }
}