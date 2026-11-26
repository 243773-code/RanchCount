package com.ranchcount.api.client;

import com.ranchcount.api.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class TelegramAlertClient {

    private static final Logger log = LoggerFactory.getLogger(TelegramAlertClient.class);

    private final WebClient telegramWebClient;
    private final String botToken;
    private final String defaultChatId;
    private final boolean alertsEnabled;

    public TelegramAlertClient(
            @Qualifier("telegramWebClient") WebClient telegramWebClient,
            @Value("${app.integrations.telegram.bot-token:}") String botToken,
            @Value("${app.integrations.telegram.chat-id:}") String defaultChatId,
            @Value("${app.integrations.telegram.enabled:true}") boolean alertsEnabled) {
        this.telegramWebClient = telegramWebClient;
        this.botToken = botToken;
        this.defaultChatId = defaultChatId;
        this.alertsEnabled = alertsEnabled;
    }

    public boolean isAlertsEnabled() {
        return alertsEnabled;
    }

    public void enviarAlertaOwner(String mensajeTexto) {
        enviarMensaje(defaultChatId, mensajeTexto);
    }

    public void enviarMensaje(String chatIdDestino, String mensajeTexto) {
        if (!alertsEnabled) {
            log.info("Envio de alertas Telegram deshabilitado por configuracion. Mensaje omitido: {}", mensajeTexto);
            return;
        }

        String targetChatId = (chatIdDestino != null && !chatIdDestino.isBlank())
                ? chatIdDestino.trim()
                : (defaultChatId != null ? defaultChatId.trim() : "");

        if (botToken == null || botToken.isBlank() || targetChatId.isEmpty()
                || "tu_bot_token_de_telegram".equalsIgnoreCase(botToken.trim())
                || "tu_chat_id_de_telegram".equalsIgnoreCase(targetChatId)) {
            log.warn("Telegram Bot no configurado (TELEGRAM_BOT_TOKEN o TELEGRAM_CHAT_ID de ejemplo). Simulando envio de alerta local:\n{}", mensajeTexto);
            return;
        }

        try {
            String respuesta = telegramWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/bot" + botToken.trim() + "/sendMessage")
                            .queryParam("chat_id", targetChatId)
                            .queryParam("text", mensajeTexto)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Alerta enviada exitosamente via Telegram Bot al chat {}. Respuesta: {}",
                    enmascararChatId(targetChatId),
                    respuesta != null ? respuesta.trim() : "OK");
        } catch (WebClientResponseException ex) {
            log.error("Telegram API respondio HTTP {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ExternalApiException(
                    "Error al enviar alerta por Telegram Bot (HTTP " + ex.getStatusCode().value() + ")",
                    ex
            );
        } catch (ExternalApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Fallo de comunicacion con Telegram Bot API: {}", ex.getMessage());
            throw new ExternalApiException("No fue posible conectar con Telegram Bot API", ex);
        }
    }

    private String enmascararChatId(String chatId) {
        if (chatId.length() <= 4) {
            return "****";
        }
        return "***" + chatId.substring(chatId.length() - 4);
    }
}
