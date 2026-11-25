package com.ranchcount.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Estructura estandarizada de respuesta ante errores HTTP")
public record ApiErrorResponse(
        @Schema(description = "Fecha y hora del error", example = "2026-11-03T12:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Codigo de estado HTTP", example = "400")
        int status,

        @Schema(description = "Nombre estandar del estado HTTP", example = "Bad Request")
        String error,

        @Schema(description = "Mensaje descriptivo legible para el cliente movil", example = "Datos de entrada invalidos")
        String message,

        @Schema(description = "Ruta del endpoint invocado", example = "/api/v1/bovinos")
        String path,

        @Schema(description = "Detalle de errores de validacion por campo (cuando aplica)")
        Map<String, String> validationErrors
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ApiErrorResponse ofValidation(int status, String error, String message, String path, Map<String, String> validationErrors) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, validationErrors);
    }
}
