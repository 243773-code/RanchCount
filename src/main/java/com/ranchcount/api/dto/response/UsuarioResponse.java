package com.ranchcount.api.dto.response;

import com.ranchcount.api.entity.RolUsuario;
import com.ranchcount.api.entity.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Datos publicos de un usuario del sistema")
public record UsuarioResponse(
        Long id,
        String nombreCompleto,
        String username,
        String telefonoWhatsapp,
        RolUsuario rol,
        Boolean activo,
        LocalDateTime creadoAt
) {
    public static UsuarioResponse fromEntity(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNombreCompleto(),
                u.getUsername(),
                u.getTelefonoWhatsapp(),
                u.getRol(),
                u.getActivo(),
                u.getCreadoAt()
        );
    }
}
