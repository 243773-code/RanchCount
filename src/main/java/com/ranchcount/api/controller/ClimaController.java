package com.ranchcount.api.controller;

import com.ranchcount.api.dto.response.ClimaResponse;
import com.ranchcount.api.service.ClimaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clima")
@Tag(name = "6. Clima Local (Open-Meteo)", description = "Consulta meteorologica en tiempo real y pronostico agricola/ganadero via WebClient")
public class ClimaController {

    private final ClimaService climaService;

    public ClimaController(ClimaService climaService) {
        this.climaService = climaService;
    }

    @GetMapping("/actual")
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(
            summary = "Obtener clima actual y pronostico de 5 dias",
            description = "Si no se envian coordenadas GPS desde Android, utiliza las coordenadas por defecto configuradas para el rancho"
    )
    public ResponseEntity<ClimaResponse> consultarClimaActual(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {
        return ResponseEntity.ok(climaService.consultarClimaRancho(lat, lon));
    }
}
