
package pe.edu.upeu.objetosperdidos.repository;

import java.sql.Types;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import pe.edu.upeu.objetosperdidos.dto.ResumenReclamosDTO;

@Repository
public class ResumenReclamosProcedimientoRepository {

    private final SimpleJdbcCall procedimiento;

    public ResumenReclamosProcedimientoRepository(
            DataSource dataSource
    ) {
        this.procedimiento = new SimpleJdbcCall(dataSource)
                .withSchemaName("UPEU_DATOS")
                .withProcedureName("SP_RESUMEN_RECLAMOS")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlOutParameter(
                                "P_TOTAL",
                                Types.NUMERIC
                        ),
                        new SqlOutParameter(
                                "P_SIN_REVISAR",
                                Types.NUMERIC
                        ),
                        new SqlOutParameter(
                                "P_REVISADOS",
                                Types.NUMERIC
                        )
                );
    }

    public ResumenReclamosDTO ejecutarResumenReclamos() {

        Map<String, Object> salida =
                procedimiento.execute(Map.of());

        return new ResumenReclamosDTO(
                numero(salida.get("P_TOTAL")),
                numero(salida.get("P_SIN_REVISAR")),
                numero(salida.get("P_REVISADOS"))
        );
    }

    private long numero(Object valor) {

        if (valor == null) {
            throw new IllegalStateException(
                    "Oracle no devolvió un valor OUT esperado"
            );
        }

        return ((Number) valor).longValue();
    }
}
