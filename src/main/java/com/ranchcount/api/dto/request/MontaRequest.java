package com.ranchcount.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Solicitud para registrar monta o gestacion detectada; enviar fechaMonta (inseminacion/monta observada) O diasGestacionAproximados (monta natural estimada)")
public record MontaRequest(
        @Schema(description = "Fecha exacta de monta o inseminacion (obligatoria si no se envia diasGestacionAproximados)", example = "2026-10-25")
        @PastOrPresent(message = "La fecha de monta no puede ser futura")
        LocalDate fechaMonta,

        @Schema(description = "Dias aproximados de gestacion en monta natural no observada (1 a 283; obligatorio si no se envia fechaMonta)", example = "90")
        @Min(value = 1, message = "Los dias aproximados de gestacion deben ser al menos 1")
        @Max(value = 283, message = "Los dias aproximados de gestacion no pueden exceder 283")
        Integer diasGestacionAproximados,

        @Schema(description = "Notas sobre semental, pajilla o condicion reproductiva", example = "Monta natural con semental Brahman")
        @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
        String notas
) {
}
