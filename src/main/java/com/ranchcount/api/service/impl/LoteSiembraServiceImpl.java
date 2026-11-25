package com.ranchcount.api.service.impl;

import com.ranchcount.api.dto.request.LoteSiembraRequest;
import com.ranchcount.api.dto.response.LoteSiembraResponse;
import com.ranchcount.api.entity.EstadoCultivo;
import com.ranchcount.api.entity.LoteSiembra;
import com.ranchcount.api.exception.BusinessRuleException;
import com.ranchcount.api.exception.DuplicateResourceException;
import com.ranchcount.api.exception.ResourceNotFoundException;
import com.ranchcount.api.repository.LoteSiembraRepository;
import com.ranchcount.api.service.LoteSiembraService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class LoteSiembraServiceImpl implements LoteSiembraService {

    private final LoteSiembraRepository loteSiembraRepository;

    public LoteSiembraServiceImpl(LoteSiembraRepository loteSiembraRepository) {
        this.loteSiembraRepository = loteSiembraRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoteSiembraResponse> listar(EstadoCultivo estado) {
        List<LoteSiembra> lotes = (estado != null)
                ? loteSiembraRepository.findByEstadoOrderByFechaSiembraDesc(estado)
                : loteSiembraRepository.findAllByOrderByFechaSiembraDesc();

        return lotes.stream().map(LoteSiembraResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoteSiembraResponse obtenerPorId(Long id) {
        LoteSiembra lote = loteSiembraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoteSiembra", "id", id));
        return LoteSiembraResponse.fromEntity(lote);
    }

    @Override
    @Transactional
    public LoteSiembraResponse crear(LoteSiembraRequest request) {
        String codigoNormalizado = request.codigoLote().trim().toUpperCase();
        if (loteSiembraRepository.existsByCodigoLoteIgnoreCase(codigoNormalizado)) {
            throw new DuplicateResourceException("Ya existe un lote de siembra con el codigo: " + codigoNormalizado);
        }

        validarFechasCultivo(request);
        validarCoordenadas(request.latitud(), request.longitud());

        LoteSiembra lote = new LoteSiembra();
        mapearCampos(lote, request, codigoNormalizado);

        return LoteSiembraResponse.fromEntity(loteSiembraRepository.save(lote));
    }

    @Override
    @Transactional
    public LoteSiembraResponse actualizar(Long id, LoteSiembraRequest request) {
        LoteSiembra lote = loteSiembraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoteSiembra", "id", id));

        String codigoNormalizado = request.codigoLote().trim().toUpperCase();
        if (!lote.getCodigoLote().equalsIgnoreCase(codigoNormalizado)
                && loteSiembraRepository.existsByCodigoLoteIgnoreCase(codigoNormalizado)) {
            throw new DuplicateResourceException("Ya existe otro lote de siembra con el codigo: " + codigoNormalizado);
        }

        validarFechasCultivo(request);
        validarCoordenadas(request.latitud(), request.longitud());
        mapearCampos(lote, request, codigoNormalizado);

        return LoteSiembraResponse.fromEntity(loteSiembraRepository.save(lote));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        LoteSiembra lote = loteSiembraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoteSiembra", "id", id));
        loteSiembraRepository.delete(lote);
    }

    private void validarFechasCultivo(LoteSiembraRequest request) {
        if (!request.fechaEstimadaCosecha().isAfter(request.fechaSiembra())) {
            throw new BusinessRuleException(
                    "La fecha estimada de cosecha debe ser estrictamente posterior a la fecha de siembra"
            );
        }
    }

    private void validarCoordenadas(BigDecimal latitud, BigDecimal longitud) {
        if ((latitud != null) != (longitud != null)) {
            throw new BusinessRuleException(
                    "Debe proporcionar tanto la latitud como la longitud del lote"
            );
        }
    }

    private void mapearCampos(LoteSiembra lote, LoteSiembraRequest request, String codigoNormalizado) {
        lote.setCodigoLote(codigoNormalizado);
        lote.setNombre(request.nombre().trim());
        lote.setTipoCultivo(request.tipoCultivo().trim());
        lote.setHectareas(request.hectareas().setScale(2, RoundingMode.HALF_UP));
        lote.setLatitud(request.latitud() != null ? request.latitud().setScale(6, RoundingMode.HALF_UP) : null);
        lote.setLongitud(request.longitud() != null ? request.longitud().setScale(6, RoundingMode.HALF_UP) : null);
        lote.setInversionTotal(request.inversionTotal().setScale(2, RoundingMode.HALF_UP));
        lote.setFechaSiembra(request.fechaSiembra());
        lote.setFechaEstimadaCosecha(request.fechaEstimadaCosecha());
        lote.setEstado(request.estado());
        lote.setObservaciones(
                request.observaciones() == null || request.observaciones().isBlank()
                        ? null
                        : request.observaciones().trim()
        );
    }
}
