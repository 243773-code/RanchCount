package com.ranchcount.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Datos de personalizacion y configuracion general del rancho (Exclusivo ROLE_OWNER)")
public record RanchoConfigRequest(
        @Schema(description = "Nombre del rancho mostrado en toda la aplicacion", example = "Los Canelos")
        @NotBlank(message = "El nombre del rancho es obligatorio")
        @Size(max = 100, message = "El nombre del rancho no puede exceder 100 caracteres")
        String nombreRancho,

        @Schema(description = "Proposito productivo principal", example = "Doble proposito")
        @Size(max = 120, message = "El proposito no puede exceder 120 caracteres")
        String proposito,

        @Schema(description = "Superficie total del rancho en hectareas", example = "500.00")
        @Positive(message = "Las hectareas totales deben ser mayores a cero")
        @Digits(integer = 8, fraction = 2, message = "Las hectareas admiten hasta 8 enteros y 2 decimales")
        BigDecimal hectareasTotales,

        @Schema(description = "Ubicacion descriptiva del rancho (municipio o estado)", example = "Ciudad del Carmen, Campeche")
        @Size(max = 160, message = "La ubicacion no puede exceder 160 caracteres")
        String ubicacionTexto
) {
}
