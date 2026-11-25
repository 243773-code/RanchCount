package com.ranchcount.api.dto.request;

import com.ranchcount.api.entity.CategoriaTransaccion;
import com.ranchcount.api.entity.TipoTransaccion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Solicitud para registrar o editar una transaccion financiera (ingreso o egreso)")
public record TransaccionRequest(
        @Schema(description = "Tipo de flujo de caja (INGRESO o EGRESO)", example = "INGRESO")
        @NotNull(message = "El tipo de transaccion es obligatorio")
        TipoTransaccion tipo,

        @Schema(description = "Categoria contable de la operacion", example = "VENTA_GANADO")
        @NotNull(message = "La categoria de la transaccion es obligatoria")
        CategoriaTransaccion categoria,

        @Schema(description = "Monto exacto usando BigDecimal (mayor a cero)", example = "28500.50")
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser estrictamente positivo")
        @Digits(integer = 13, fraction = 2, message = "El monto admite hasta 13 enteros y 2 decimales")
        BigDecimal monto,

        @Schema(description = "Descripcion o concepto detallado del movimiento", example = "Venta de novillo de engorda en pie")
        @NotBlank(message = "El concepto de la transaccion es obligatorio")
        @Size(max = 255, message = "El concepto no puede exceder 255 caracteres")
        String concepto,

        @Schema(description = "Fecha contable de la operacion", example = "2026-11-03")
        @NotNull(message = "La fecha de la transaccion es obligatoria")
        @PastOrPresent(message = "La fecha de la transaccion no puede ser futura")
        LocalDate fechaTransaccion,

        @Schema(description = "Opcional: ID del bovino asociado al movimiento", example = "1")
        Long bovinoId,

        @Schema(description = "Opcional: ID del lote de siembra asociado al movimiento", example = "1")
        Long loteSiembraId
) {
}
