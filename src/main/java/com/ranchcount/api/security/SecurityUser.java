package com.ranchcount.api.security;

import com.ranchcount.api.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SecurityUser implements UserDetails {

    private final Usuario usuario;

    public SecurityUser(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Long getId() {
        return usuario.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(usuario.getRol().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(usuario.getActivo());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(usuario.getActivo());
    }
}
