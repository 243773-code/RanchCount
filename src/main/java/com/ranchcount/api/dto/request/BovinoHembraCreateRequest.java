package com.ranchcount.api.dto.request;

import com.ranchcount.api.entity.EstadoBovino;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Datos para registrar una hembra bovina (el sexo se asigna automaticamente como HEMBRA)")
public record BovinoHembraCreateRequest(
        @Schema(description = "Identificador unico del arete SINIIGA o interno", example = "MX-JAL-0042")
        @NotBlank(message = "El codigo de arete es obligatorio")
        @Size(max = 40, message = "El codigo de arete no puede exceder 40 caracteres")
        String areteCodigo,

        @Schema(description = "Nombre identificativo opcional de la hembra", example = "Mariposa")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        @Schema(description = "Raza ganadera", example = "Angus")
        @NotBlank(message = "La raza del bovino es obligatoria")
        @Size(max = 80, message = "La raza no puede exceder 80 caracteres")
        String raza,

        @Schema(description = "Fecha de nacimiento", example = "2023-04-15")
        @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
        LocalDate fechaNacimiento,

        @Schema(description = "Peso actual en kilogramos", example = "435.50")
        @Positive(message = "El peso en kilogramos debe ser mayor a cero")
        @Digits(integer = 6, fraction = 2, message = "El peso admite hasta 6 enteros y 2 decimales")
        BigDecimal pesoKg,

        @Schema(description = "Estado productivo actual (usar CARGADA para gestacion)", example = "VACIA")
        @NotNull(message = "El estado del bovino es obligatorio")
        EstadoBovino estado,

        @Schema(description = "Fecha de monta (si se registra directamente como CARGADA, el backend calcula +283 dias)", example = "2026-02-01")
        @PastOrPresent(message = "La fecha de monta no puede ser futura")
        LocalDate fechaMonta,

        @Schema(description = "Observaciones sanitarias o de manejo", example = "Vacunacion al dia")
        @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
        String notas
) {
}
