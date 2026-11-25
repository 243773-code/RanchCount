package com.ranchcount.api.service.impl;

import com.ranchcount.api.client.TelegramAlertClient;
import com.ranchcount.api.dto.response.AlertaPartoResultadoResponse;
import com.ranchcount.api.entity.Bovino;
import com.ranchcount.api.entity.EstadoBovino;
import com.ranchcount.api.repository.BovinoRepository;
import com.ranchcount.api.service.AlertaPartoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertaPartoServiceImpl implements AlertaPartoService {

    private static final Logger log = LoggerFactory.getLogger(AlertaPartoServiceImpl.class);

    private final BovinoRepository bovinoRepository;
    private final TelegramAlertClient telegramClient;
    private final int diasAnticipacion;
    private final String configuredChatId;

    public AlertaPartoServiceImpl(
            BovinoRepository bovinoRepository,
            TelegramAlertClient telegramClient,
            @Value("${app.scheduler.parto-anticipation-days:7}") int diasAnticipacion,
            @Value("${app.integrations.telegram.chat-id:}") String configuredChatId) {
        this.bovinoRepository = bovinoRepository;
        this.telegramClient = telegramClient;
        this.diasAnticipacion = diasAnticipacion;
        this.configuredChatId = configuredChatId;
    }

    @Override
    @Transactional
    public AlertaPartoResultadoResponse procesarAlertasPartosProximos(boolean forzarReenvioHoy) {
        LocalDate hoy = LocalDate.now();
        LocalDate limiteInferior = hoy.minusDays(3);
        LocalDate limiteSuperior = hoy.plusDays(diasAnticipacion);

        List<Bovino> candidatas = bovinoRepository
                .findByEstadoAndFechaEstimadaPartoBetweenOrderByFechaEstimadaPartoAsc(
                        EstadoBovino.CARGADA,
                        limiteInferior,
                        limiteSuperior
                );

        int enviadas = 0;
        int omitidas = 0;
        List<String> aretesNotificados = new ArrayList<>();

        for (Bovino vaca : candidatas) {
            if (!forzarReenvioHoy && hoy.equals(vaca.getUltimaAlertaParto())) {
                omitidas++;
                continue;
            }

            long diasRestantes = ChronoUnit.DAYS.between(hoy, vaca.getFechaEstimadaParto());
            String mensaje = construirMensajeAlerta(vaca, diasRestantes);

            try {
                telegramClient.enviarMensaje(configuredChatId, mensaje);
                vaca.setUltimaAlertaParto(hoy);
                bovinoRepository.save(vaca);
                enviadas++;
                aretesNotificados.add(vaca.getAreteCodigo());
            } catch (Exception ex) {
                log.error("No se pudo enviar alerta Telegram para bovino arete {}: {}",
                        vaca.getAreteCodigo(), ex.getMessage());
            }
        }

        return new AlertaPartoResultadoResponse(
                LocalDateTime.now(),
                telegramClient.isAlertsEnabled(),
                candidatas.size(),
                enviadas,
                omitidas,
                aretesNotificados
        );
    }

    private String construirMensajeAlerta(Bovino vaca, long diasRestantes) {
        String identificador = (vaca.getNombre() != null && !vaca.getNombre().isBlank())
                ? vaca.getAreteCodigo() + " (" + vaca.getNombre() + ")"
                : vaca.getAreteCodigo();

        String urgencia;
        if (diasRestantes < 0) {
            urgencia = "🚨 ATENCION: Fecha estimada cumplida hace " + Math.abs(diasRestantes) + " dias";
        } else if (diasRestantes == 0) {
            urgencia = "⚠️ PARTO PROGRAMADO PARA HOY";
        } else {
            urgencia = "⏳ Faltan aprox. " + diasRestantes + " dias para el parto";
        }

        return String.format(
                "🐄 ALERTA DE PARTO - RANCHCOUNT\n\n"
                        + "Ejemplar: %s\n"
                        + "Raza: %s\n"
                        + "Fecha estimada: %s\n"
                        + "Estado: %s\n\n"
                        + "Favor de preparar corral de maternidad e inspeccionar a la vaca.",
                identificador,
                vaca.getRaza(),
                vaca.getFechaEstimadaParto(),
                urgencia
        );
    }
}
