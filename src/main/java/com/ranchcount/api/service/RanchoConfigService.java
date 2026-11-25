package com.ranchcount.api.service;

import com.ranchcount.api.dto.request.RanchoConfigRequest;
import com.ranchcount.api.dto.response.RanchoConfigResponse;

public interface RanchoConfigService {
    RanchoConfigResponse obtenerConfiguracion();

    RanchoConfigResponse actualizarConfiguracion(RanchoConfigRequest request);
}
