package com.ranchcount.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Solicitud para activar o desactivar el acceso de un usuario")
public record UsuarioEstadoRequest(
        @Schema(description = "Estado activo (true) o bloqueado (false)", example = "false")
        @NotNull(message = "El campo activo es obligatorio")
        Boolean activo
) {
}
