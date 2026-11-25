package com.ranchcount.api.dto.response;

import com.ranchcount.api.entity.CategoriaTransaccion;
import com.ranchcount.api.entity.TipoTransaccion;
import com.ranchcount.api.entity.Transaccion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Representacion detallada de una transaccion financiera activa")
public record TransaccionResponse(
        Long id,
        TipoTransaccion tipo,
        CategoriaTransaccion categoria,
        BigDecimal monto,
        String concepto,
        LocalDate fechaTransaccion,
        Long bovinoId,
        String bovinoArete,
        Long loteSiembraId,
        String loteCodigo,
        String registradoPorUsername,
        LocalDateTime creadoAt
) {
    public static TransaccionResponse fromEntity(Transaccion t) {
        return new TransaccionResponse(
                t.getId(),
                t.getTipo(),
                t.getCategoria(),
                t.getMonto(),
                t.getConcepto(),
                t.getFechaTransaccion(),
                t.getBovino() != null ? t.getBovino().getId() : null,
                t.getBovino() != null ? t.getBovino().getAreteCodigo() : null,
                t.getLoteSiembra() != null ? t.getLoteSiembra().getId() : null,
                t.getLoteSiembra() != null ? t.getLoteSiembra().getCodigoLote() : null,
                t.getRegistradoPor() != null ? t.getRegistradoPor().getUsername() : null,
                t.getCreadoAt()
        );
    }
}
