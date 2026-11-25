package com.ranchcount.api.controller;

import com.ranchcount.api.dto.request.CambiarPasswordRequest;
import com.ranchcount.api.dto.request.UsuarioCreateRequest;
import com.ranchcount.api.dto.request.UsuarioEstadoRequest;
import com.ranchcount.api.dto.request.UsuarioUpdateRequest;
import com.ranchcount.api.dto.response.UsuarioResponse;
import com.ranchcount.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "2. Usuarios", description = "Gestion de cuentas y roles del rancho (Exclusivo ROLE_OWNER)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios registrados")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar detalle de un usuario por ID")
    public ResponseEntity<UsuarioResponse> obtenerUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo usuario (ROLE_OWNER o ROLE_OPERATOR)")
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearUsuario(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar nombre y telefono de un usuario")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, request));
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = "Cambiar contrasena de un usuario verificando la clave anterior")
    public ResponseEntity<Void> cambiarPassword(
            @PathVariable Long id,
            @Valid @RequestBody CambiarPasswordRequest request,
            Authentication authentication) {
        usuarioService.cambiarPassword(id, request, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o revocar acceso de un usuario")
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioEstadoRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, request, authentication.getName()));
    }
}
