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

@Schema(description = "Datos para registrar un macho bovino (el sexo se asigna automaticamente como MACHO; admite SEMENTAL, ENGORDA, VENDIDO, BAJA)")
public record BovinoMachoCreateRequest(
        @Schema(description = "Identificador unico del arete SINIIGA o interno", example = "MX-JAL-0099")
        @NotBlank(message = "El codigo de arete es obligatorio")
        @Size(max = 40, message = "El codigo de arete no puede exceder 40 caracteres")
        String areteCodigo,

        @Schema(description = "Nombre identificativo opcional del macho", example = "Tornado")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        @Schema(description = "Raza ganadera", example = "Charolais")
        @NotBlank(message = "La raza del bovino es obligatoria")
        @Size(max = 80, message = "La raza no puede exceder 80 caracteres")
        String raza,

        @Schema(description = "Fecha de nacimiento", example = "2022-03-10")
        @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
        LocalDate fechaNacimiento,

        @Schema(description = "Peso actual en kilogramos", example = "780.00")
        @Positive(message = "El peso en kilogramos debe ser mayor a cero")
        @Digits(integer = 6, fraction = 2, message = "El peso admite hasta 6 enteros y 2 decimales")
        BigDecimal pesoKg,

        @Schema(description = "Estado del macho (SEMENTAL, ENGORDA, VENDIDO o BAJA)", example = "SEMENTAL")
        @NotNull(message = "El estado del bovino es obligatorio")
        EstadoBovino estado,

        @Schema(description = "Observaciones sanitarias o de manejo", example = "Semental certificado")
        @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
        String notas
) {
}
