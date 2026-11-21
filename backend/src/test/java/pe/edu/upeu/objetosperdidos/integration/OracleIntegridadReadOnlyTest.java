package pe.edu.upeu.objetosperdidos.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de integracion SOLO LECTURA contra Oracle real.
 * No inserta, actualiza ni elimina registros.
 * Fallan si APP_UPEU no puede conectarse a Oracle.
 */
@SpringBootTest
class OracleIntegridadReadOnlyTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void compruebaConexionRealYUsuarioAppUpeu() throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT USER FROM DUAL");
             ResultSet rows = statement.executeQuery()) {

            assertTrue(rows.next(), "Oracle debe responder a la consulta SELECT USER FROM DUAL");
            assertEquals("APP_UPEU", rows.getString(1).toUpperCase(Locale.ROOT),
                    "Las pruebas deben usar el usuario de aplicacion, no UPEU_DATOS");
        }
    }

    @Test
    void puedeConsultarTablaCoincidenciaSinModificarDatos() throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM UPEU_DATOS.COINCIDENCIA");
             ResultSet rows = statement.executeQuery()) {

            assertTrue(rows.next(), "Debe poder consultarse COINCIDENCIA");
            assertTrue(rows.getLong(1) >= 0, "El conteo no puede ser negativo");
        }
    }

    @Test
    void indicesDeConfirmacionSiguenSiendoUnicosYValidos() throws Exception {
        String query = """
                SELECT INDEX_NAME, UNIQUENESS, STATUS, INDEX_TYPE
                FROM ALL_INDEXES
                WHERE OWNER = 'UPEU_DATOS'
                  AND TABLE_OWNER = 'UPEU_DATOS'
                  AND TABLE_NAME = 'COINCIDENCIA'
                  AND INDEX_NAME IN ('UX_COINC_CONF_HALL', 'UX_COINC_CONF_PERD')
                """;

        Set<String> encontrados = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet rows = statement.executeQuery()) {

            while (rows.next()) {
                String nombre = rows.getString("INDEX_NAME");
                assertEquals("UNIQUE", rows.getString("UNIQUENESS"),
                        "El indice " + nombre + " debe ser unico");
                assertEquals("VALID", rows.getString("STATUS"),
                        "El indice " + nombre + " debe estar valido");
                assertEquals("FUNCTION-BASED NORMAL", rows.getString("INDEX_TYPE"),
                        "El indice " + nombre + " debe ser basado en expresion");
                encontrados.add(nombre);
            }
        }

        assertEquals(Set.of("UX_COINC_CONF_HALL", "UX_COINC_CONF_PERD"), encontrados,
                "Deben existir ambos indices protectores de coincidencias confirmadas");
    }
}
