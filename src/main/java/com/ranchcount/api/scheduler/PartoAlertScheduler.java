package com.ranchcount.api.scheduler;

import com.ranchcount.api.dto.response.AlertaPartoResultadoResponse;
import com.ranchcount.api.service.AlertaPartoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PartoAlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(PartoAlertScheduler.class);

    private final AlertaPartoService alertaPartoService;

    public PartoAlertScheduler(AlertaPartoService alertaPartoService) {
        this.alertaPartoService = alertaPartoService;
    }

    /**
     * Tarea programada diaria (por defecto 07:00 AM).
     * Revisa todas las hembras en estado CARGADA que esten a 7 dias o menos de su fecha_estimada_parto
     * y envia alerta push via CallMeBot WhatsApp API al celular del OWNER.
     */
    @Scheduled(cron = "${app.scheduler.parto-alert-cron:0 0 7 * * *}")
    public void ejecutarRevisionDiariaPartos() {
        log.info("Iniciando tarea programada diaria de verificacion de partos proximos...");
        AlertaPartoResultadoResponse resultado = alertaPartoService.procesarAlertasPartosProximos(false);
        log.info("Revision diaria finalizada. Candidatas: {}, Enviadas: {}, Omitidas: {}",
                resultado.bovinosEnVentanaParto(),
                resultado.alertasEnviadasExitosamente(),
                resultado.alertasOmitidasYaNotificadasHoy());
    }
}
