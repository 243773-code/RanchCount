package com.ranchcount.api.dto.response;

import com.ranchcount.api.entity.EstadoCultivo;
import com.ranchcount.api.entity.LoteSiembra;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Schema(description = "Informacion detallada de un lote de siembra agricola")
public record LoteSiembraResponse(
        Long id,
        String codigoLote,
        String nombre,
        String tipoCultivo,
        BigDecimal hectareas,
        BigDecimal latitud,
        BigDecimal longitud,
        BigDecimal inversionTotal,
        BigDecimal inversionPorHectarea,
        LocalDate fechaSiembra,
        LocalDate fechaEstimadaCosecha,
        Long diasRestantesCosecha,
        EstadoCultivo estado,
        String observaciones,
        LocalDateTime creadoAt,
        LocalDateTime actualizadoAt
) {
    public static LoteSiembraResponse fromEntity(LoteSiembra l) {
        BigDecimal invPorHa = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (l.getHectareas() != null && l.getHectareas().compareTo(BigDecimal.ZERO) > 0 && l.getInversionTotal() != null) {
            invPorHa = l.getInversionTotal().divide(l.getHectareas(), 2, RoundingMode.HALF_UP);
        }

        Long diasRestantes = null;
        if (l.getEstado() != EstadoCultivo.COSECHADO
                && l.getEstado() != EstadoCultivo.SINIESTRADO
                && l.getFechaEstimadaCosecha() != null) {
            diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), l.getFechaEstimadaCosecha());
        }

        return new LoteSiembraResponse(
                l.getId(),
                l.getCodigoLote(),
                l.getNombre(),
                l.getTipoCultivo(),
                l.getHectareas(),
                l.getLatitud(),
                l.getLongitud(),
                l.getInversionTotal(),
                invPorHa,
                l.getFechaSiembra(),
                l.getFechaEstimadaCosecha(),
                diasRestantes,
                l.getEstado(),
                l.getObservaciones(),
                l.getCreadoAt(),
                l.getActualizadoAt()
        );
    }
}
