package com.ranchcount.api.service.impl;

import com.ranchcount.api.dto.request.RanchoConfigRequest;
import com.ranchcount.api.dto.response.RanchoConfigResponse;
import com.ranchcount.api.entity.RanchoConfig;
import com.ranchcount.api.repository.RanchoConfigRepository;
import com.ranchcount.api.service.RanchoConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RanchoConfigServiceImpl implements RanchoConfigService {

    private static final String DEFAULT_NOMBRE = "Mi Rancho";
    private static final String DEFAULT_PROPOSITO = "Doble propósito";

    private final RanchoConfigRepository ranchoConfigRepository;

    public RanchoConfigServiceImpl(RanchoConfigRepository ranchoConfigRepository) {
        this.ranchoConfigRepository = ranchoConfigRepository;
    }

    @Override
    @Transactional
    public RanchoConfigResponse obtenerConfiguracion() {
        RanchoConfig config = obtenerOCrearPorDefecto();
        return RanchoConfigResponse.fromEntity(config);
    }

    @Override
    @Transactional
    public RanchoConfigResponse actualizarConfiguracion(RanchoConfigRequest request) {
        RanchoConfig config = obtenerOCrearPorDefecto();
        config.setNombreRancho(request.nombreRancho().trim());
        config.setProposito(
                (request.proposito() == null || request.proposito().isBlank())
                        ? DEFAULT_PROPOSITO
                        : request.proposito().trim()
        );
        config.setHectareasTotales(request.hectareasTotales());
        config.setUbicacionTexto(
                (request.ubicacionTexto() == null || request.ubicacionTexto().isBlank())
                        ? null
                        : request.ubicacionTexto().trim()
        );
        return RanchoConfigResponse.fromEntity(ranchoConfigRepository.save(config));
    }

    private RanchoConfig obtenerOCrearPorDefecto() {
        return ranchoConfigRepository.findTopByOrderByIdAsc()
                .orElseGet(() -> {
                    RanchoConfig inicial = new RanchoConfig();
                    inicial.setNombreRancho(DEFAULT_NOMBRE);
                    inicial.setProposito(DEFAULT_PROPOSITO);
                    return ranchoConfigRepository.save(inicial);
                });
    }
}
