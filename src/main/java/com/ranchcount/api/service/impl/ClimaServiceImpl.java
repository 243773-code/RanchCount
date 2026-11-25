package com.ranchcount.api.service.impl;

import com.ranchcount.api.client.OpenMeteoClient;
import com.ranchcount.api.client.OpenMeteoRawResponse;
import com.ranchcount.api.dto.response.ClimaResponse;
import com.ranchcount.api.exception.BusinessRuleException;
import com.ranchcount.api.service.ClimaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClimaServiceImpl implements ClimaService {

    private final OpenMeteoClient openMeteoClient;
    private final double defaultLatitude;
    private final double defaultLongitude;

    public ClimaServiceImpl(
            OpenMeteoClient openMeteoClient,
            @Value("${app.integrations.open-meteo.default-latitude}") double defaultLatitude,
            @Value("${app.integrations.open-meteo.default-longitude}") double defaultLongitude) {
        this.openMeteoClient = openMeteoClient;
        this.defaultLatitude = defaultLatitude;
        this.defaultLongitude = defaultLongitude;
    }

    @Override
    public ClimaResponse consultarClimaRancho(Double latitud, Double longitud) {
        double lat = latitud != null ? latitud : defaultLatitude;
        double lon = longitud != null ? longitud : defaultLongitude;

        if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            throw new BusinessRuleException("Coordenadas geograficas fuera de rango permitido (lat: -90..90, lon: -180..180)");
        }

        OpenMeteoRawResponse raw = openMeteoClient.obtenerPronostico(lat, lon);
        OpenMeteoRawResponse.CurrentBlock cur = raw.current();

        String condicionActual = traducirCodigoWmo(cur.weatherCode());
        String recomendacion = generarRecomendacionOperativa(
                cur.temperature2m(),
                cur.precipitation(),
                cur.windSpeed10m(),
                cur.weatherCode()
        );

        List<ClimaResponse.PronosticoDiaItem> pronosticoDias = construirPronosticoDiario(raw.daily());

        return new ClimaResponse(
                raw.latitude(),
                raw.longitude(),
                raw.timezone(),
                raw.elevation(),
                cur.time(),
                cur.temperature2m(),
                cur.apparentTemperature(),
                cur.relativeHumidity2m(),
                cur.precipitation(),
                cur.windSpeed10m(),
                cur.weatherCode(),
                condicionActual,
                recomendacion,
                pronosticoDias
        );
    }

    private List<ClimaResponse.PronosticoDiaItem> construirPronosticoDiario(OpenMeteoRawResponse.DailyBlock daily) {
        List<ClimaResponse.PronosticoDiaItem> lista = new ArrayList<>();
        if (daily == null || daily.time() == null) {
            return lista;
        }
        int dias = daily.time().size();
        for (int i = 0; i < dias; i++) {
            Double max = obtenerSeguro(daily.temperatureMax(), i);
            Double min = obtenerSeguro(daily.temperatureMin(), i);
            Double lluvia = obtenerSeguro(daily.precipitationSum(), i);
            Integer code = obtenerEnteroSeguro(daily.weatherCode(), i);
            lista.add(new ClimaResponse.PronosticoDiaItem(
                    daily.time().get(i),
                    max,
                    min,
                    lluvia,
                    traducirCodigoWmo(code)
            ));
        }
        return lista;
    }

    private Double obtenerSeguro(List<Double> valores, int idx) {
        return (valores != null && idx < valores.size()) ? valores.get(idx) : null;
    }

    private Integer obtenerEnteroSeguro(List<Integer> valores, int idx) {
        return (valores != null && idx < valores.size()) ? valores.get(idx) : null;
    }

    private String traducirCodigoWmo(Integer code) {
        if (code == null) {
            return "No disponible";
        }
        return switch (code) {
            case 0 -> "Cielo despejado";
            case 1, 2 -> "Parcialmente nublado";
            case 3 -> "Nublado";
            case 45, 48 -> "Niebla";
            case 51, 53, 55 -> "Llovizna ligera a moderada";
            case 61, 63, 65 -> "Lluvia";
            case 71, 73, 75 -> "Caida de nieve o granizo fino";
            case 80, 81, 82 -> "Chubascos de lluvia";
            case 95, 96, 99 -> "Tormenta electrica";
            default -> "Condicion meteorologica codigo " + code;
        };
    }

    private String generarRecomendacionOperativa(Double tempC, Double precipMm, Double vientoKmh, Integer wmoCode) {
        if (wmoCode != null && wmoCode >= 95) {
            return "Alerta de tormenta electrica: resguardar ganado y suspender aplicacion de agroquimicos.";
        }
        if (precipMm != null && precipMm > 5.0) {
            return "Lluvia activa significativa: revisar drenaje en lotes de siembra y corrales de paricion.";
        }
        if (tempC != null && tempC >= 35.0) {
            return "Estres termico por calor elevado: asegurar sombra y agua fresca continua en bebederos.";
        }
        if (vientoKmh != null && vientoKmh >= 30.0) {
            return "Viento fuerte: evitar aspersiones agricolas en lotes abiertos por deriva.";
        }
        return "Condiciones favorables para labores ganaderas y agricolas en campo.";
    }
}
