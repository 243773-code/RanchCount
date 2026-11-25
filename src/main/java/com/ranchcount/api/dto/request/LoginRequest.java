package com.ranchcount.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credenciales de autenticacion para obtener token JWT")
public record LoginRequest(
        @Schema(description = "Nombre de usuario unico", example = "owner")
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 3, max = 60, message = "El nombre de usuario debe tener entre 3 y 60 caracteres")
        String username,

        @Schema(description = "Contrasena de acceso", example = "CambiarPassword123!")
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 6, max = 100, message = "La contrasena debe tener entre 6 y 100 caracteres")
        String password
) {
}
