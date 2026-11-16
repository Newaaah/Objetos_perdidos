package pe.edu.upeu.objetosperdidos.security;

import org.junit.jupiter.api.Test;
import pe.edu.upeu.objetosperdidos.entity.EstadoUsuario;
import pe.edu.upeu.objetosperdidos.entity.Persona;
import pe.edu.upeu.objetosperdidos.entity.RolUsuario;
import pe.edu.upeu.objetosperdidos.entity.Usuario;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioPrincipalTests {
    @Test
    void mapsTheExistingRoleWithoutRolePrefixAndDisablesInactiveAccounts() {
        Persona persona = new Persona();
        persona.setDocumentoIdentidad("12345678");
        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setRol(RolUsuario.ADMIN);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);

        assertEquals("12345678", principal.getUsername());
        assertEquals("ADMIN", principal.getAuthorities().iterator().next().getAuthority());
        assertTrue(principal.isEnabled());

        usuario.setEstado(EstadoUsuario.INACTIVO);
        assertFalse(principal.isEnabled());
    }
}
