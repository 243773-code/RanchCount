package com.ranchcount.api.controller;

import com.ranchcount.api.dto.request.BovinoCreateRequest;
import com.ranchcount.api.dto.request.BovinoHembraCreateRequest;
import com.ranchcount.api.dto.request.BovinoMachoCreateRequest;
import com.ranchcount.api.dto.request.BovinoUpdateRequest;
import com.ranchcount.api.dto.request.MontaRequest;
import com.ranchcount.api.dto.request.PartoRequest;
import com.ranchcount.api.dto.response.BovinoResponse;
import com.ranchcount.api.entity.EstadoBovino;
import com.ranchcount.api.entity.SexoBovino;
import com.ranchcount.api.service.BovinoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bovinos")
@Tag(name = "3. Bovinos (Gestion Ganadera)", description = "Control de ganado, endpoints separados por sexo, registro de montas (+283 dias CARGADA) y reporte de partos")
public class BovinoController {

    private final BovinoService bovinoService;

    public BovinoController(BovinoService bovinoService) {
        this.bovinoService = bovinoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Listar todos los bovinos (vacas y machos) con filtros opcionales por estado y sexo")
    public ResponseEntity<List<BovinoResponse>> listar(
            @RequestParam(required = false) EstadoBovino estado,
            @RequestParam(required = false) SexoBovino sexo) {
        return ResponseEntity.ok(bovinoService.listar(estado, sexo));
    }

    @GetMapping("/hembras")
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Listar unicamente vacas / hembras con filtro opcional por estado")
    public ResponseEntity<List<BovinoResponse>> listarHembras(
            @RequestParam(required = false) EstadoBovino estado) {
        return ResponseEntity.ok(bovinoService.listarHembras(estado));
    }

    @GetMapping("/machos")
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Listar unicamente machos / sementales / engorda con filtro opcional por estado")
    public ResponseEntity<List<BovinoResponse>> listarMachos(
            @RequestParam(required = false) EstadoBovino estado) {
        return ResponseEntity.ok(bovinoService.listarMachos(estado));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Obtener detalle de un bovino por ID")
    public ResponseEntity<BovinoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(bovinoService.obtenerPorId(id));
    }

    @GetMapping("/proximos-partos")
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Listar hembras en estado CARGADA proximas a su fecha estimada de parto")
    public ResponseEntity<List<BovinoResponse>> listarProximosPartos(
            @RequestParam(defaultValue = "7") int dias) {
        return ResponseEntity.ok(bovinoService.listarProximosPartos(dias));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Registrar un nuevo bovino generico (Accesible por OWNER y OPERATOR)")
    public ResponseEntity<BovinoResponse> crear(@Valid @RequestBody BovinoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bovinoService.crear(request));
    }

    @PostMapping("/hembras")
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Registrar una nueva hembra / vaca (sexo HEMBRA asignado automaticamente)")
    public ResponseEntity<BovinoResponse> crearHembra(@Valid @RequestBody BovinoHembraCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bovinoService.crearHembra(request));
    }

    @PostMapping("/machos")
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Registrar un nuevo macho (sexo MACHO asignado automaticamente)")
    public ResponseEntity<BovinoResponse> crearMacho(@Valid @RequestBody BovinoMachoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bovinoService.crearMacho(request));
    }

    @PatchMapping("/{id}/monta")
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(
            summary = "Registrar monta o gestacion de una vaca (Accesible por OWNER y OPERATOR)",
            description = "Pasa el estado a CARGADA y calcula +283 dias para fechaEstimadaParto. Acepta fechaMonta exacta (inseminacion/monta observada) o diasGestacionAproximados (monta natural estimada)"
    )
    public ResponseEntity<BovinoResponse> registrarMonta(
            @PathVariable Long id,
            @Valid @RequestBody MontaRequest request) {
        return ResponseEntity.ok(bovinoService.registrarMonta(id, request));
    }

    @PatchMapping("/{id}/parto")
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(
            summary = "Reportar parto de una vaca CARGADA (Accesible por OWNER y OPERATOR)",
            description = "Actualiza el estado de la madre a LACTANDO o VACIA, limpia la gestacion activa y permite registrar opcionalmente la cria"
    )
    public ResponseEntity<BovinoResponse> registrarParto(
            @PathVariable Long id,
            @Valid @RequestBody PartoRequest request) {
        return ResponseEntity.ok(bovinoService.registrarParto(id, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Editar informacion general de un bovino (Exclusivo ROLE_OWNER)")
    public ResponseEntity<BovinoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody BovinoUpdateRequest request) {
        return ResponseEntity.ok(bovinoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Eliminar registro de un bovino (Exclusivo ROLE_OWNER)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        bovinoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
