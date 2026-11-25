package com.ranchcount.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Datos meteorologicos actuales y pronostico local del rancho obtenidos de Open-Meteo")
public record ClimaResponse(
        double latitud,
        double longitud,
        String zonaHoraria,
        double elevacionMetros,
        String fechaHoraLectura,
        Double temperaturaActualC,
        Double sensacionTermicaC,
        Integer humedadRelativaPorcentaje,
        Double precipitacionActualMm,
        Double velocidadVientoKmh,
        Integer codigoClimaWmo,
        String condicionActual,
        String recomendacionOperativa,
        List<PronosticoDiaItem> pronosticoProximosDias
) {
    public record PronosticoDiaItem(
            String fecha,
            Double temperaturaMaxC,
            Double temperaturaMinC,
            Double precipitacionTotalMm,
            String condicion
    ) {
    }
}
