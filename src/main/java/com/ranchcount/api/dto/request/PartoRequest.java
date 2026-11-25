package com.ranchcount.api.dto.request;

import com.ranchcount.api.entity.SexoBovino;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Solicitud para que el operador o propietario reporte que una vaca CARGADA ya pario")
public record PartoRequest(
        @Schema(description = "Fecha en que ocurrio el parto", example = "2026-11-03")
        @NotNull(message = "La fecha de parto es obligatoria")
        @PastOrPresent(message = "La fecha de parto no puede ser futura")
        LocalDate fechaParto,

        @Schema(description = "Indica si el parto logro cria viva (true -> pasa a LACTANDO; false -> pasa a VACIA)", example = "true")
        @NotNull(message = "Debe indicar si el parto fue exitoso")
        Boolean partoExitoso,

        @Schema(description = "Opcional: codigo de arete para dar de alta automaticamente a la cria recien nacida", example = "MX-JAL-0105")
        @Size(max = 40, message = "El codigo de arete de la cria no puede exceder 40 caracteres")
        String areteCria,

        @Schema(description = "Opcional: sexo de la cria (requerido si se envia areteCria)", example = "MACHO")
        SexoBovino sexoCria,

        @Schema(description = "Opcional: nombre identificativo de la cria", example = "Relampago")
        @Size(max = 80, message = "El nombre de la cria no puede exceder 80 caracteres")
        String nombreCria,

        @Schema(description = "Opcional: peso al nacer de la cria en kg", example = "34.50")
        @Positive(message = "El peso de la cria debe ser positivo")
        @Digits(integer = 6, fraction = 2, message = "El peso admite hasta 6 enteros y 2 decimales")
        BigDecimal pesoCriaKg,

        @Schema(description = "Observaciones del parto reportadas por el operador", example = "Parto natural sin complicaciones en potrero norte")
        @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
        String notas
) {
}
