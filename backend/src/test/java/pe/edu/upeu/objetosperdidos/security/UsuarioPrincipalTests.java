
package pe.edu.upeu.objetosperdidos.security;

import org.junit.jupiter.api.Test;
import pe.edu.upeu.objetosperdidos.entity.RolUsuario;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioPrincipalTests {

    @Test
    void verificarRolYEstadoDelUsuario() {

        UsuarioPrincipal principal = new UsuarioPrincipal(
                1L,
                "DNI|12345678",
                "hash-de-prueba",
                RolUsuario.ADMIN,
                true
        );

        assertEquals("DNI|12345678", principal.getUsername());

        assertEquals("hash-de-prueba", principal.getPassword());

        assertTrue(
                principal.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ADMIN"))
        );

        assertTrue(principal.isEnabled());

        UsuarioPrincipal inactivo = new UsuarioPrincipal(
                1L,
                "DNI|12345678",
                "hash-de-prueba",
                RolUsuario.ADMIN,
                false
        );

        assertFalse(inactivo.isEnabled());
    }
}
