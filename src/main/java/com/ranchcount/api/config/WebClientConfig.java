package com.ranchcount.api.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean(name = "openMeteoWebClient")
    public WebClient openMeteoWebClient(
            @Value("${app.integrations.open-meteo.api-url}") String openMeteoBaseUrl) {
        return buildBaseWebClientBuilder()
                .baseUrl(openMeteoBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean(name = "telegramWebClient")
    public WebClient telegramWebClient(
            @Value("${app.integrations.telegram.api-url:https://api.telegram.org}") String telegramBaseUrl) {
        return buildBaseWebClientBuilder()
                .baseUrl(telegramBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private WebClient.Builder buildBaseWebClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 8000)
                .responseTimeout(Duration.ofSeconds(12));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
