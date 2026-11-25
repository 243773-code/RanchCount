package com.ranchcount.api.dto.request;

import com.ranchcount.api.entity.EstadoCultivo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Solicitud para crear o actualizar un lote agricola de siembra")
public record LoteSiembraRequest(
        @Schema(description = "Codigo unico del lote agricola", example = "LOTE-NORTE-01")
        @NotBlank(message = "El codigo del lote es obligatorio")
        @Size(max = 40, message = "El codigo del lote no puede exceder 40 caracteres")
        String codigoLote,

        @Schema(description = "Nombre descriptivo de la parcela o potrero", example = "Parcela El Mirador")
        @NotBlank(message = "El nombre del lote es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @Schema(description = "Tipo o variedad del cultivo", example = "Maiz Forrajero")
        @NotBlank(message = "El tipo de cultivo es obligatorio")
        @Size(max = 80, message = "El tipo de cultivo no puede exceder 80 caracteres")
        String tipoCultivo,

        @Schema(description = "Superficie sembrada en hectareas", example = "12.50")
        @NotNull(message = "La cantidad de hectareas es obligatoria")
        @Positive(message = "Las hectareas deben ser un valor mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "Hectareas admite hasta 8 enteros y 2 decimales")
        BigDecimal hectareas,

        @Schema(description = "Inversion acumulada o presupuestada en el lote", example = "84500.00")
        @NotNull(message = "El monto de inversion es obligatorio")
        @PositiveOrZero(message = "La inversion no puede ser negativa")
        @Digits(integer = 13, fraction = 2, message = "Inversion admite hasta 13 enteros y 2 decimales")
        BigDecimal inversionTotal,

        @Schema(description = "Fecha de inicio de siembra", example = "2026-06-01")
        @NotNull(message = "La fecha de siembra es obligatoria")
        LocalDate fechaSiembra,

        @Schema(description = "Fecha estimada para la cosecha (posterior a la fecha de siembra)", example = "2026-11-15")
        @NotNull(message = "La fecha estimada de cosecha es obligatoria")
        LocalDate fechaEstimadaCosecha,

        @Schema(description = "Estado operativo del cultivo", example = "EN_CRECIMIENTO")
        @NotNull(message = "El estado del cultivo es obligatorio")
        EstadoCultivo estado,

        @Schema(description = "Observaciones agronomicas", example = "Riego por goteo instalado")
        @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
        String observaciones,

        @Schema(description = "Latitud GPS de la parcela (-90 a 90)", example = "20.659700")
        @DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
        @DecimalMax(value = "90.0", message = "La latitud debe ser menor o igual a 90")
        @Digits(integer = 2, fraction = 6, message = "La latitud admite hasta 2 enteros y 6 decimales")
        BigDecimal latitud,

        @Schema(description = "Longitud GPS de la parcela (-180 a 180)", example = "-103.349600")
        @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
        @DecimalMax(value = "180.0", message = "La longitud debe ser menor o igual a 180")
        @Digits(integer = 3, fraction = 6, message = "La longitud admite hasta 3 enteros y 6 decimales")
        BigDecimal longitud
) {
}
