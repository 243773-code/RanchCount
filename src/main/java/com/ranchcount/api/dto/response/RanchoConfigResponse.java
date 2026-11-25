package com.ranchcount.api.dto.response;

import com.ranchcount.api.entity.RanchoConfig;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Configuracion persistente del perfil del rancho")
public record RanchoConfigResponse(
        Long id,
        String nombreRancho,
        String proposito,
        BigDecimal hectareasTotales,
        String ubicacionTexto,
        LocalDateTime creadoAt,
        LocalDateTime actualizadoAt
) {
    public static RanchoConfigResponse fromEntity(RanchoConfig entity) {
        return new RanchoConfigResponse(
                entity.getId(),
                entity.getNombreRancho(),
                entity.getProposito(),
                entity.getHectareasTotales(),
                entity.getUbicacionTexto(),
                entity.getCreadoAt(),
                entity.getActualizadoAt()
        );
    }
}
