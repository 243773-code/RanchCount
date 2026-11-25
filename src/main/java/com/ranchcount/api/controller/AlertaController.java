package com.ranchcount.api.controller;

import com.ranchcount.api.dto.response.AlertaPartoResultadoResponse;
import com.ranchcount.api.service.AlertaPartoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alertas")
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "7. Alertas Programadas (Telegram Bot)", description = "Verificacion de partos proximos (<= 7 dias) y disparo manual de prueba (Exclusivo ROLE_OWNER)")
public class AlertaController {

    private final AlertaPartoService alertaPartoService;

    public AlertaController(AlertaPartoService alertaPartoService) {
        this.alertaPartoService = alertaPartoService;
    }

    @PostMapping("/verificar-partos")
    @Operation(
            summary = "Ejecutar manualmente la revision de partos y envio Telegram",
            description = "Util para verificar en tiempo real la integracion con Telegram Bot API sin esperar al cron diario de las 07:00 AM"
    )
    public ResponseEntity<AlertaPartoResultadoResponse> verificarPartosManualmente(
            @RequestParam(defaultValue = "false") boolean forzarReenvioHoy) {
        return ResponseEntity.ok(alertaPartoService.procesarAlertasPartosProximos(forzarReenvioHoy));
    }
}
