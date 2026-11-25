package com.ranchcount.api.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String JWT_ERROR_ATTRIBUTE = "JWT_AUTH_ERROR";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (jwt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    request.setAttribute(JWT_ERROR_ATTRIBUTE, "El token JWT es invalido o el usuario se encuentra inactivo");
                }
            }
        } catch (ExpiredJwtException ex) {
            request.setAttribute(JWT_ERROR_ATTRIBUTE, "El token JWT ha expirado; inicie sesion nuevamente");
        } catch (JwtException | UsernameNotFoundException | IllegalArgumentException ex) {
            request.setAttribute(JWT_ERROR_ATTRIBUTE, "Token JWT invalido o malformado");
        }

        filterChain.doFilter(request, response);
    }
}
