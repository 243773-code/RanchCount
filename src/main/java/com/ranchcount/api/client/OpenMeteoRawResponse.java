package com.ranchcount.api.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoRawResponse(
        double latitude,
        double longitude,
        String timezone,
        double elevation,
        CurrentBlock current,
        DailyBlock daily
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrentBlock(
            String time,
            @JsonProperty("temperature_2m") Double temperature2m,
            @JsonProperty("relative_humidity_2m") Integer relativeHumidity2m,
            @JsonProperty("apparent_temperature") Double apparentTemperature,
            Double precipitation,
            @JsonProperty("weather_code") Integer weatherCode,
            @JsonProperty("wind_speed_10m") Double windSpeed10m
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DailyBlock(
            List<String> time,
            @JsonProperty("weather_code") List<Integer> weatherCode,
            @JsonProperty("temperature_2m_max") List<Double> temperatureMax,
            @JsonProperty("temperature_2m_min") List<Double> temperatureMin,
            @JsonProperty("precipitation_sum") List<Double> precipitationSum
    ) {
    }
}
