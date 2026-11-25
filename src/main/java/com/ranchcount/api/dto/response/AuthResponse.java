package com.ranchcount.api.dto.response;

import com.ranchcount.api.entity.RolUsuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta exitosa de inicio de sesion con JWT para Android Retrofit")
public record AuthResponse(
        @Schema(description = "Token JWT firmado", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Tipo de esquema HTTP", example = "Bearer")
        String tokenType,

        @Schema(description = "Tiempo de expiracion del token en milisegundos", example = "86400000")
        long expiresInMs,

        @Schema(description = "ID interno del usuario autenticado", example = "1")
        Long usuarioId,

        @Schema(description = "Username autenticado", example = "owner")
        String username,

        @Schema(description = "Nombre completo del usuario", example = "Administrador del Rancho")
        String nombreCompleto,

        @Schema(description = "Rol asignado para control de vistas en Android", example = "ROLE_OWNER")
        RolUsuario rol
) {
}
