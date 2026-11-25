package com.ranchcount.api.service;

import com.ranchcount.api.dto.request.CambiarPasswordRequest;
import com.ranchcount.api.dto.request.UsuarioCreateRequest;
import com.ranchcount.api.dto.request.UsuarioEstadoRequest;
import com.ranchcount.api.dto.request.UsuarioUpdateRequest;
import com.ranchcount.api.dto.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponse> listarTodos();

    UsuarioResponse obtenerPorId(Long id);

    UsuarioResponse crearUsuario(UsuarioCreateRequest request);

    UsuarioResponse actualizarUsuario(Long id, UsuarioUpdateRequest request);

    void cambiarPassword(Long id, CambiarPasswordRequest request, String authenticatedUsername);

    UsuarioResponse cambiarEstado(Long id, UsuarioEstadoRequest request, String authenticatedUsername);
}
