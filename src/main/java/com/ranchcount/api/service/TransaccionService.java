package com.ranchcount.api.service;

import com.ranchcount.api.dto.request.TransaccionRequest;
import com.ranchcount.api.dto.response.ResumenFinancieroResponse;
import com.ranchcount.api.dto.response.TransaccionResponse;
import com.ranchcount.api.entity.TipoTransaccion;

import java.time.LocalDate;
import java.util.List;

public interface TransaccionService {

    List<TransaccionResponse> listar(TipoTransaccion tipo, LocalDate desde, LocalDate hasta);

    TransaccionResponse obtenerPorId(Long id);

    ResumenFinancieroResponse obtenerResumen(LocalDate desde, LocalDate hasta);

    TransaccionResponse crear(TransaccionRequest request, String usernameAutenticado);

    TransaccionResponse actualizar(Long id, TransaccionRequest request);

    void eliminarLogicamente(Long id);
}
