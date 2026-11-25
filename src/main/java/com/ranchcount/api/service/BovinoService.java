package com.ranchcount.api.service;

import com.ranchcount.api.dto.request.BovinoCreateRequest;
import com.ranchcount.api.dto.request.BovinoHembraCreateRequest;
import com.ranchcount.api.dto.request.BovinoMachoCreateRequest;
import com.ranchcount.api.dto.request.BovinoUpdateRequest;
import com.ranchcount.api.dto.request.MontaRequest;
import com.ranchcount.api.dto.request.PartoRequest;
import com.ranchcount.api.dto.response.BovinoResponse;
import com.ranchcount.api.entity.EstadoBovino;
import com.ranchcount.api.entity.SexoBovino;

import java.util.List;

public interface BovinoService {

    int DIAS_GESTACION_BOVINA = 283;

    List<BovinoResponse> listar(EstadoBovino estado, SexoBovino sexo);

    List<BovinoResponse> listarHembras(EstadoBovino estado);

    List<BovinoResponse> listarMachos(EstadoBovino estado);

    BovinoResponse obtenerPorId(Long id);

    List<BovinoResponse> listarProximosPartos(int diasVentana);

    BovinoResponse crear(BovinoCreateRequest request);

    BovinoResponse crearHembra(BovinoHembraCreateRequest request);

    BovinoResponse crearMacho(BovinoMachoCreateRequest request);

    BovinoResponse actualizar(Long id, BovinoUpdateRequest request);

    BovinoResponse registrarMonta(Long id, MontaRequest request);

    BovinoResponse registrarParto(Long id, PartoRequest request);

    void eliminar(Long id);
}
