package com.ranchcount.api.client;

import com.ranchcount.api.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class OpenMeteoClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClient.class);

    private final WebClient openMeteoWebClient;

    public OpenMeteoClient(@Qualifier("openMeteoWebClient") WebClient openMeteoWebClient) {
        this.openMeteoWebClient = openMeteoWebClient;
    }

    public OpenMeteoRawResponse obtenerPronostico(double latitud, double longitud) {
        try {
            OpenMeteoRawResponse response = openMeteoWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("latitude", latitud)
                            .queryParam("longitude", longitud)
                            .queryParam("current", "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m")
                            .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum")
                            .queryParam("forecast_days", 5)
                            .queryParam("timezone", "auto")
                            .build())
                    .retrieve()
                    .bodyToMono(OpenMeteoRawResponse.class)
                    .block();

            if (response == null || response.current() == null) {
                throw new ExternalApiException("Open-Meteo retorno una respuesta vacia o incompleta");
            }
            return response;
        } catch (WebClientResponseException ex) {
            log.error("Open-Meteo respondio con HTTP {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ExternalApiException("Error al consultar servicio meteorologico Open-Meteo (HTTP " + ex.getStatusCode().value() + ")", ex);
        } catch (ExternalApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error de red al conectar con Open-Meteo: {}", ex.getMessage());
            throw new ExternalApiException("No fue posible conectar con el servicio de clima Open-Meteo", ex);
        }
    }
}
