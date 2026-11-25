package com.ranchcount.api.service;

import com.ranchcount.api.dto.request.LoginRequest;
import com.ranchcount.api.dto.response.AuthResponse;
import com.ranchcount.api.dto.response.UsuarioResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    UsuarioResponse obtenerPerfilActual(String username);
}
