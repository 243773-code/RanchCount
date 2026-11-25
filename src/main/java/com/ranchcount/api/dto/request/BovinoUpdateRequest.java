package com.ranchcount.api.dto.request;

import com.ranchcount.api.entity.EstadoBovino;
import com.ranchcount.api.entity.SexoBovino;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Datos para actualizar integralmente un bovino (Exclusivo ROLE_OWNER)")
public record BovinoUpdateRequest(
        @Schema(description = "Codigo unico de arete", example = "MX-JAL-0042")
        @NotBlank(message = "El codigo de arete es obligatorio")
        @Size(max = 40, message = "El codigo de arete no puede exceder 40 caracteres")
        String areteCodigo,

        @Schema(description = "Nombre del ejemplar", example = "Mariposa")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        @Schema(description = "Raza ganadera", example = "Angus")
        @NotBlank(message = "La raza es obligatoria")
        @Size(max = 80, message = "La raza no puede exceder 80 caracteres")
        String raza,

        @Schema(description = "Sexo del ejemplar", example = "HEMBRA")
        @NotNull(message = "El sexo es obligatorio")
        SexoBovino sexo,

        @Schema(description = "Fecha de nacimiento", example = "2023-04-15")
        @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
        LocalDate fechaNacimiento,

        @Schema(description = "Peso actual en kilogramos", example = "460.00")
        @Positive(message = "El peso debe ser mayor a cero")
        @Digits(integer = 6, fraction = 2, message = "El peso admite hasta 6 enteros y 2 decimales")
        BigDecimal pesoKg,

        @Schema(description = "Estado actual del bovino", example = "CARGADA")
        @NotNull(message = "El estado es obligatorio")
        EstadoBovino estado,

        @Schema(description = "Fecha de monta cuando el estado sea CARGADA", example = "2026-02-01")
        @PastOrPresent(message = "La fecha de monta no puede ser futura")
        LocalDate fechaMonta,

        @Schema(description = "Notas de seguimiento", example = "Revision veterinaria favorable")
        @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
        String notas
) {
}
