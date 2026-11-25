package com.ranchcount.api.service.impl;

import com.ranchcount.api.dto.request.LoginRequest;
import com.ranchcount.api.dto.response.AuthResponse;
import com.ranchcount.api.dto.response.UsuarioResponse;
import com.ranchcount.api.entity.Usuario;
import com.ranchcount.api.exception.ResourceNotFoundException;
import com.ranchcount.api.repository.UsuarioRepository;
import com.ranchcount.api.security.JwtService;
import com.ranchcount.api.security.SecurityUser;
import com.ranchcount.api.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username().trim(),
                        request.password()
                )
        );

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        String token = jwtService.generateToken(securityUser);
        Usuario usuario = securityUser.getUsuario();

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getRol()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfilActual(String username) {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
        return UsuarioResponse.fromEntity(usuario);
    }
}
