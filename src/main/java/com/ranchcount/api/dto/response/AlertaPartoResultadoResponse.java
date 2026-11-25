package com.ranchcount.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Resultado de la ejecucion de revision de partos proximos y envio de alertas Telegram")
public record AlertaPartoResultadoResponse(
        LocalDateTime fechaEjecucion,
        boolean alertasHabilitadas,
        int bovinosEnVentanaParto,
        int alertasEnviadasExitosamente,
        int alertasOmitidasYaNotificadasHoy,
        List<String> aretesNotificados
) {
}
