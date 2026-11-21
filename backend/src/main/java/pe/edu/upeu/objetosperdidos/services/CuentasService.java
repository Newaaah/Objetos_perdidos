package pe.edu.upeu.objetosperdidos.services;

import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.entity.*;
import pe.edu.upeu.objetosperdidos.exception.*;
import pe.edu.upeu.objetosperdidos.repository.*;

/** Alta segura de clave estudiantil por ADMIN; modificacion propia con clave anterior. */
@Service
public class CuentasService {
    private final UsuarioRepository usuarios;
    private final EstudianteRepository estudiantes;
    private final PersonalRepository personal;
    private final PasswordEncoder passwordEncoder;
    public CuentasService(UsuarioRepository usuarios, EstudianteRepository estudiantes,
                          PersonalRepository personal, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.estudiantes = estudiantes;
        this.personal = personal;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void aprovisionarEstudiante(Long idPersona, String clave, Long idAdmin) {
        // Solo llamado por el endpoint ADMIN; doble comprobacion de actividad.
        Personal admin = personal.findById(idAdmin)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador no tiene registro de personal"));
        if (admin.getEstado() != EstadoPersonal.ACTIVO) throw new ResourceInUseException("Personal no activo");
        if (idPersona == null || idPersona <= 0) throw new IllegalArgumentException("ID invalido");
        validarClave(clave);
        Estudiante estudiante = estudiantes.findById(idPersona)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
        if (estudiante.getEstado() != EstadoEstudiante.ACTIVO || personal.existsById(idPersona)) {
            throw new ResourceInUseException("No se puede habilitar esta cuenta estudiantil");
        }
        Usuario usuario = usuarios.findById(idPersona).orElse(null);
        if (usuario == null) {
            usuario = new Usuario(estudiante.getPersona());
            usuario.setRol(RolUsuario.ESTUDIANTE);
            usuario.setEstado(EstadoUsuario.ACTIVO);
        } else if (usuario.getRol() != RolUsuario.ESTUDIANTE || usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new ResourceInUseException("La cuenta no es un estudiante activo");
        }
        usuario.setPassword(passwordEncoder.encode(clave));
        usuarios.saveAndFlush(usuario);
    }

    @Transactional
    public void cambiarMiClave(Long idPersona, String actual, String nueva) {
        validarClave(nueva);
        Usuario u = usuarios.findById(idPersona)
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no tiene clave configurada"));
        if (u.getEstado() != EstadoUsuario.ACTIVO || u.getPassword() == null
                || !passwordEncoder.matches(actual == null ? "" : actual, u.getPassword())) {
            throw new ResourceInUseException("No se pudo validar la clave anterior");
        }
        if (passwordEncoder.matches(nueva, u.getPassword())) {
            throw new IllegalArgumentException("La nueva clave debe ser diferente");
        }
        u.setPassword(passwordEncoder.encode(nueva));
        usuarios.saveAndFlush(u);
    }

    private static void validarClave(String clave) {
        if (clave == null || clave.length() < 12 || clave.length() > 72
                || clave.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La clave debe tener entre 12 y 72 bytes UTF-8");
        }
    }
}
