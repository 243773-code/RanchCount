package com.ranchcount.api.controller;

import com.ranchcount.api.dto.request.LoteSiembraRequest;
import com.ranchcount.api.dto.response.LoteSiembraResponse;
import com.ranchcount.api.entity.EstadoCultivo;
import com.ranchcount.api.service.LoteSiembraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lotes")
@Tag(name = "5. Lotes de Siembra (Gestion Agricola)", description = "Control de hectareas, inversion, fechas de siembra y cosecha estimada")
public class LoteSiembraController {

    private final LoteSiembraService loteSiembraService;

    public LoteSiembraController(LoteSiembraService loteSiembraService) {
        this.loteSiembraService = loteSiembraService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Listar lotes de siembra con filtro opcional por estado")
    public ResponseEntity<List<LoteSiembraResponse>> listar(
            @RequestParam(required = false) EstadoCultivo estado) {
        return ResponseEntity.ok(loteSiembraService.listar(estado));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Consultar detalle de un lote de siembra por ID")
    public ResponseEntity<LoteSiembraResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(loteSiembraService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Registrar nuevo lote de siembra (Exclusivo ROLE_OWNER)")
    public ResponseEntity<LoteSiembraResponse> crear(@Valid @RequestBody LoteSiembraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loteSiembraService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Actualizar lote de siembra existente (Exclusivo ROLE_OWNER)")
    public ResponseEntity<LoteSiembraResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody LoteSiembraRequest request) {
        return ResponseEntity.ok(loteSiembraService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Eliminar lote de siembra (Exclusivo ROLE_OWNER)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        loteSiembraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
