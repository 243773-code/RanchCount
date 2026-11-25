package com.ranchcount.api;

import com.ranchcount.api.client.OpenMeteoClient;
import com.ranchcount.api.client.TelegramAlertClient;
import com.ranchcount.api.dto.response.AlertaPartoResultadoResponse;
import com.ranchcount.api.dto.response.ClimaResponse;
import com.ranchcount.api.entity.Bovino;
import com.ranchcount.api.entity.EstadoBovino;
import com.ranchcount.api.entity.SexoBovino;
import com.ranchcount.api.repository.BovinoRepository;
import com.ranchcount.api.service.AlertaPartoService;
import com.ranchcount.api.service.ClimaService;
import com.ranchcount.api.service.impl.AlertaPartoServiceImpl;
import com.ranchcount.api.service.impl.ClimaServiceImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ExternalClientsIntegrationTests {

  private MockWebServer mockWebServer;

  @Autowired
  private BovinoRepository bovinoRepository;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  @DisplayName("4. Consumo de Open-Meteo API mediante WebClient y mapeo meteorologico")
  void consumoOpenMeteoViaWebClient() throws InterruptedException {
    String openMeteoMockJson = """
        {
          "latitude": 20.66,
          "longitude": -103.35,
          "timezone": "America/Mexico_City",
          "elevation": 1560.0,
          "current": {
            "time": "2026-11-03T12:00",
            "temperature_2m": 26.4,
            "relative_humidity_2m": 48,
            "apparent_temperature": 25.9,
            "precipitation": 0.0,
            "weather_code": 0,
            "wind_speed_10m": 11.2
          },
          "daily": {
            "time": ["2026-11-03", "2026-11-04"],
            "weather_code": [0, 61],
            "temperature_2m_max": [28.0, 24.5],
            "temperature_2m_min": [13.2, 14.0],
            "precipitation_sum": [0.0, 8.4]
          }
        }
        """;

    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .setBody(openMeteoMockJson));

    String baseUrl = mockWebServer.url("/v1/forecast").toString();
    WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
    OpenMeteoClient client = new OpenMeteoClient(webClient);
    ClimaService climaService = new ClimaServiceImpl(client, 20.6597, -103.3496);

    ClimaResponse respuesta = climaService.consultarClimaRancho(null, null);

    assertThat(respuesta.temperaturaActualC()).isEqualTo(26.4);
    assertThat(respuesta.condicionActual()).isEqualTo("Cielo despejado");
    assertThat(respuesta.pronosticoProximosDias()).hasSize(2);
    assertThat(respuesta.pronosticoProximosDias().get(1).condicion()).isEqualTo("Lluvia");

    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getPath()).contains("latitude=20.6597");
    assertThat(recordedRequest.getPath()).contains("longitude=-103.3496");
  }

  @Test
  @DisplayName("5. Scheduler/Servicio de Alertas consume Telegram Bot API para bovino <= 7 dias de parto")
  void envioAlertaTelegramPartoProximo() throws InterruptedException {
    // Registrar hembra CARGADA a 5 dias de parir
    Bovino vacaProxima = new Bovino();
    vacaProxima.setAreteCodigo("MX-ALERTA-007");
    vacaProxima.setNombre("Luna");
    vacaProxima.setRaza("Holstein");
    vacaProxima.setSexo(SexoBovino.HEMBRA);
    vacaProxima.setPesoKg(new BigDecimal("510.00"));
    vacaProxima.setEstado(EstadoBovino.CARGADA);
    vacaProxima.setFechaMonta(LocalDate.now().minusDays(278));
    vacaProxima.setFechaEstimadaParto(LocalDate.now().plusDays(5));
    bovinoRepository.save(vacaProxima);

    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .setBody("{\"ok\": true, \"result\": {\"message_id\": 1}}"));

    String telegramBaseUrl = mockWebServer.url("/").toString();
    // Quitar barra final si la hay para evitar dobles barras
    if (telegramBaseUrl.endsWith("/")) {
      telegramBaseUrl = telegramBaseUrl.substring(0, telegramBaseUrl.length() - 1);
    }

    WebClient webClient = WebClient.builder().baseUrl(telegramBaseUrl).build();
    TelegramAlertClient telegramClient = new TelegramAlertClient(
        webClient,
        "123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11",
        "987654321",
        true);

    AlertaPartoService alertaService = new AlertaPartoServiceImpl(
        bovinoRepository,
        telegramClient,
        7,
        "987654321");

    AlertaPartoResultadoResponse resultado = alertaService.procesarAlertasPartosProximos(false);

    assertThat(resultado.alertasEnviadasExitosamente()).isGreaterThanOrEqualTo(1);
    assertThat(resultado.aretesNotificados()).contains("MX-ALERTA-007");

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getPath()).contains("/bot123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11/sendMessage");
    assertThat(request.getPath()).contains("chat_id=987654321");
    assertThat(request.getPath()).contains("MX-ALERTA-007");

    // Una segunda ejecucion el mismo dia sin forzar no debe duplicar mensajes
    AlertaPartoResultadoResponse segundaEjecucion = alertaService.procesarAlertasPartosProximos(false);
    assertThat(segundaEjecucion.aretesNotificados()).doesNotContain("MX-ALERTA-007");
    assertThat(segundaEjecucion.alertasOmitidasYaNotificadasHoy()).isGreaterThanOrEqualTo(1);
  }
}
