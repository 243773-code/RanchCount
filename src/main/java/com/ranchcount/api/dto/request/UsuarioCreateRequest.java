package com.ranchcount.api.dto.request;

import com.ranchcount.api.entity.RolUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud para registrar un nuevo usuario del rancho (OWNER u OPERATOR)")
public record UsuarioCreateRequest(
        @Schema(description = "Nombre completo del colaborador o propietario", example = "Juan Perez")
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre completo no puede exceder 120 caracteres")
        String nombreCompleto,

        @Schema(description = "Nombre de usuario para iniciar sesion en la app Android", example = "operador1")
        @NotBlank(message = "El username es obligatorio")
        @Size(min = 3, max = 60, message = "El username debe tener entre 3 y 60 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El username solo puede contener letras, numeros, puntos, guiones y guion bajo")
        String username,

        @Schema(description = "Contrasena inicial (minimo 8 caracteres)", example = "Operador2026!")
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, max = 100, message = "La contrasena debe tener al menos 8 caracteres")
        String password,

        @Schema(description = "Numero celular con codigo internacional para alertas WhatsApp", example = "+5213312345678")
        @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$", message = "El telefono debe incluir entre 10 y 15 digitos (opcionalmente precedido por +)")
        String telefonoWhatsapp,

        @Schema(description = "Rol de seguridad estricto", example = "ROLE_OPERATOR")
        @NotNull(message = "El rol del usuario es obligatorio")
        RolUsuario rol
) {
}
