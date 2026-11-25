package com.ranchcount.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Resumen consolidado del flujo de caja del rancho")
public record ResumenFinancieroResponse(
        @Schema(description = "Suma de todos los ingresos activos en el periodo", example = "125000.00")
        BigDecimal totalIngresos,

        @Schema(description = "Suma de todos los egresos activos en el periodo", example = "43250.75")
        BigDecimal totalEgresos,

        @Schema(description = "Balance neto (totalIngresos - totalEgresos)", example = "81749.25")
        BigDecimal balanceNeto,

        @Schema(description = "Fecha inicial del filtro aplicado (null si es historico completo)")
        LocalDate fechaInicio,

        @Schema(description = "Fecha final del filtro aplicado (null si es historico completo)")
        LocalDate fechaFin
) {
}
