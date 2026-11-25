package com.ranchcount.api.service;

import com.ranchcount.api.dto.response.AlertaPartoResultadoResponse;

public interface AlertaPartoService {

    AlertaPartoResultadoResponse procesarAlertasPartosProximos(boolean forzarReenvioHoy);
}
