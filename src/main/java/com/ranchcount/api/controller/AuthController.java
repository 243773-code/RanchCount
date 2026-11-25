package com.ranchcount.api.controller;

import com.ranchcount.api.dto.request.LoginRequest;
import com.ranchcount.api.dto.response.AuthResponse;
import com.ranchcount.api.dto.response.UsuarioResponse;
import com.ranchcount.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "1. Autenticacion", description = "Inicio de sesion JWT sin estado y validacion de perfil activo")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "Iniciar sesion y obtener token JWT",
            description = "Autentica las credenciales del propietario u operador y retorna un JWT Bearer stateless"
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Consultar perfil del usuario autenticado",
            description = "Permite a la app Android verificar la validez del token y obtener el rol activo"
    )
    public ResponseEntity<UsuarioResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.obtenerPerfilActual(authentication.getName()));
    }
}
