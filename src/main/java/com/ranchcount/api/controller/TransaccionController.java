package com.ranchcount.api.controller;

import com.ranchcount.api.dto.request.TransaccionRequest;
import com.ranchcount.api.dto.response.ResumenFinancieroResponse;
import com.ranchcount.api.dto.response.TransaccionResponse;
import com.ranchcount.api.entity.TipoTransaccion;
import com.ranchcount.api.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transacciones")
@Tag(name = "4. Transacciones (Flujo de Caja)", description = "Ingresos y egresos con BigDecimal y borrado logico (@SQLDelete / @SQLRestriction)")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Listar transacciones activas con filtros opcionales por tipo y rango de fechas")
    public ResponseEntity<List<TransaccionResponse>> listar(
            @RequestParam(required = false) TipoTransaccion tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(transaccionService.listar(tipo, desde, hasta));
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Obtener resumen de flujo de caja (total ingresos, egresos y balance neto)")
    public ResponseEntity<ResumenFinancieroResponse> obtenerResumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(transaccionService.obtenerResumen(desde, hasta));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'OPERATOR')")
    @Operation(summary = "Consultar detalle de una transaccion activa por ID")
    public ResponseEntity<TransaccionResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transaccionService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Registrar ingreso o egreso (Exclusivo ROLE_OWNER)")
    public ResponseEntity<TransaccionResponse> crear(
            @Valid @RequestBody TransaccionRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaccionService.crear(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Actualizar una transaccion existente (Exclusivo ROLE_OWNER)")
    public ResponseEntity<TransaccionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TransaccionRequest request) {
        return ResponseEntity.ok(transaccionService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "Eliminar logicamente una transaccion (Exclusivo ROLE_OWNER)",
            description = "Aplica borrado logico registrando eliminado_at sin destruir el historial en PostgreSQL"
    )
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        transaccionService.eliminarLogicamente(id);
        return ResponseEntity.noContent().build();
    }
}
