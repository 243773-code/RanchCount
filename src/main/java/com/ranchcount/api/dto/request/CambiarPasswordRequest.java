package com.ranchcount.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud para cambiar la contrasena de un usuario")
public record CambiarPasswordRequest(

        @Schema(description = "Contrasena actual del usuario para verificacion de identidad", example = "MiPasswordActual123!")
        @NotBlank(message = "La contrasena actual es obligatoria")
        String passwordActual,

        @Schema(description = "Nueva contrasena (minimo 8 caracteres)", example = "NuevaClave2026!")
        @NotBlank(message = "La nueva contrasena es obligatoria")
        @Size(min = 8, max = 100, message = "La nueva contrasena debe tener al menos 8 caracteres")
        String passwordNueva,

        @Schema(description = "Confirmacion de la nueva contrasena (debe coincidir)", example = "NuevaClave2026!")
        @NotBlank(message = "La confirmacion de contrasena es obligatoria")
        String passwordConfirmacion
) {
}
