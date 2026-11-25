package com.ranchcount.api.service.impl;

import com.ranchcount.api.dto.request.CambiarPasswordRequest;
import com.ranchcount.api.dto.request.UsuarioCreateRequest;
import com.ranchcount.api.dto.request.UsuarioEstadoRequest;
import com.ranchcount.api.dto.request.UsuarioUpdateRequest;
import com.ranchcount.api.dto.response.UsuarioResponse;
import com.ranchcount.api.entity.Usuario;
import com.ranchcount.api.exception.BusinessRuleException;
import com.ranchcount.api.exception.DuplicateResourceException;
import com.ranchcount.api.exception.ResourceNotFoundException;
import com.ranchcount.api.repository.UsuarioRepository;
import com.ranchcount.api.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return UsuarioResponse.fromEntity(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse crearUsuario(UsuarioCreateRequest request) {
        String normalizedUsername = request.username().trim().toLowerCase();
        if (usuarioRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new DuplicateResourceException("Ya existe un usuario registrado con el username: " + normalizedUsername);
        }

        String phone = (request.telefonoWhatsapp() == null || request.telefonoWhatsapp().isBlank())
                ? null
                : request.telefonoWhatsapp().trim();

        Usuario nuevo = new Usuario(
                request.nombreCompleto().trim(),
                normalizedUsername,
                passwordEncoder.encode(request.password()),
                phone,
                request.rol()
        );

        return UsuarioResponse.fromEntity(usuarioRepository.save(nuevo));
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarUsuario(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setTelefonoWhatsapp(
                (request.telefonoWhatsapp() == null || request.telefonoWhatsapp().isBlank())
                        ? null
                        : request.telefonoWhatsapp().trim()
        );

        return UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void cambiarPassword(Long id, CambiarPasswordRequest request, String authenticatedUsername) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (!request.passwordNueva().equals(request.passwordConfirmacion())) {
            throw new BusinessRuleException("La confirmacion de la nueva contrasena no coincide");
        }

        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPasswordHash())) {
            throw new BusinessRuleException("La contrasena actual es incorrecta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.passwordNueva()));
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, UsuarioEstadoRequest request, String authenticatedUsername) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (!request.activo() && usuario.getUsername().equalsIgnoreCase(authenticatedUsername)) {
            throw new BusinessRuleException("No puede desactivar su propia cuenta activa de propietario");
        }

        usuario.setActivo(request.activo());
        return UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
    }
}
