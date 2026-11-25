package com.ranchcount.api.controller;

import com.ranchcount.api.dto.request.RanchoConfigRequest;
import com.ranchcount.api.dto.response.RanchoConfigResponse;
import com.ranchcount.api.service.RanchoConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rancho")
@Tag(name = "8. Perfil del Rancho", description = "Personalizacion del nombre, proposito productivo, superficie en hectareas y ubicacion del rancho")
public class RanchoConfigController {

    private final RanchoConfigService ranchoConfigService;

    public RanchoConfigController(RanchoConfigService ranchoConfigService) {
        this.ranchoConfigService = ranchoConfigService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Consultar configuracion actual del rancho")
    public ResponseEntity<RanchoConfigResponse> obtener() {
        return ResponseEntity.ok(ranchoConfigService.obtenerConfiguracion());
    }

    @PutMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Actualizar personalizacion del rancho (Exclusivo ROLE_OWNER)")
    public ResponseEntity<RanchoConfigResponse> actualizar(@Valid @RequestBody RanchoConfigRequest request) {
        return ResponseEntity.ok(ranchoConfigService.actualizarConfiguracion(request));
    }
}
