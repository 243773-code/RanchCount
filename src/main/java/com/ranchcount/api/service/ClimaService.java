package com.ranchcount.api.service;

import com.ranchcount.api.dto.response.ClimaResponse;

public interface ClimaService {

    ClimaResponse consultarClimaRancho(Double latitud, Double longitud);
}
