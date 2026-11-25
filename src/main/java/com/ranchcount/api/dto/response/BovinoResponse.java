package com.ranchcount.api.dto.response;

import com.ranchcount.api.entity.Bovino;
import com.ranchcount.api.entity.EstadoBovino;
import com.ranchcount.api.entity.SexoBovino;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Schema(description = "Informacion detallada de un ejemplar bovino")
public record BovinoResponse(
        Long id,
        String areteCodigo,
        String nombre,
        String raza,
        SexoBovino sexo,
        LocalDate fechaNacimiento,
        BigDecimal pesoKg,
        EstadoBovino estado,
        LocalDate fechaMonta,
        LocalDate fechaEstimadaParto,
        Long diasRestantesParto,
        LocalDate fechaUltimoParto,
        String notas,
        LocalDateTime creadoAt,
        LocalDateTime actualizadoAt
) {
    public static BovinoResponse fromEntity(Bovino b) {
        Long diasRestantes = null;
        if (b.getEstado() == EstadoBovino.CARGADA && b.getFechaEstimadaParto() != null) {
            diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), b.getFechaEstimadaParto());
        }

        return new BovinoResponse(
                b.getId(),
                b.getAreteCodigo(),
                b.getNombre(),
                b.getRaza(),
                b.getSexo(),
                b.getFechaNacimiento(),
                b.getPesoKg(),
                b.getEstado(),
                b.getFechaMonta(),
                b.getFechaEstimadaParto(),
                diasRestantes,
                b.getFechaUltimoParto(),
                b.getNotas(),
                b.getCreadoAt(),
                b.getActualizadoAt()
        );
    }
}
