package com.ranchcount.api.service;

import com.ranchcount.api.dto.request.LoteSiembraRequest;
import com.ranchcount.api.dto.response.LoteSiembraResponse;
import com.ranchcount.api.entity.EstadoCultivo;

import java.util.List;

public interface LoteSiembraService {

    List<LoteSiembraResponse> listar(EstadoCultivo estado);

    LoteSiembraResponse obtenerPorId(Long id);

    LoteSiembraResponse crear(LoteSiembraRequest request);

    LoteSiembraResponse actualizar(Long id, LoteSiembraRequest request);

    void eliminar(Long id);
}
