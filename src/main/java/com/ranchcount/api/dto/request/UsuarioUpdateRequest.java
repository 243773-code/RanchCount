package com.ranchcount.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud para actualizar el perfil de un usuario (nombre y telefono)")
public record UsuarioUpdateRequest(

        @Schema(description = "Nombre completo actualizado", example = "Juan Carlos Perez Vaquero")
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre completo no puede exceder 120 caracteres")
        String nombreCompleto,

        @Schema(description = "Numero celular con codigo internacional para alertas WhatsApp", example = "+5213312345678")
        @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$",
                message = "El telefono debe incluir entre 10 y 15 digitos (opcionalmente precedido por +)")
        String telefonoWhatsapp
) {
}
