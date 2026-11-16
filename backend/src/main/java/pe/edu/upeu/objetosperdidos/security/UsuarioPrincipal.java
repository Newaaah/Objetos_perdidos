
package pe.edu.upeu.objetosperdidos.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.upeu.objetosperdidos.entity.RolUsuario;

import java.util.Collection;
import java.util.List;

public record UsuarioPrincipal(
        Long idPersona,
        String nombreUsuario,
        String claveHash,
        RolUsuario rol,
        boolean habilitado
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return switch (rol) {
            case ADMIN -> List.of(
                    new SimpleGrantedAuthority("ADMIN"),
                    new SimpleGrantedAuthority("PERSONAL"),
                    new SimpleGrantedAuthority("ESTUDIANTE")
            );

            case PERSONAL -> List.of(
                    new SimpleGrantedAuthority("PERSONAL"),
                    new SimpleGrantedAuthority("ESTUDIANTE")
            );

            case ESTUDIANTE -> List.of(
                    new SimpleGrantedAuthority("ESTUDIANTE")
            );
        };
    }

    @Override
    public String getPassword() {
        return claveHash;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public boolean isEnabled() {
        return habilitado;
    }
}
