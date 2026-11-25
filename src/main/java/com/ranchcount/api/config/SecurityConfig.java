package com.ranchcount.api.config;

import com.ranchcount.api.security.CustomAccessDeniedHandler;
import com.ranchcount.api.security.JwtAuthenticationEntryPoint;
import com.ranchcount.api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publicos (Login y documentacion interactiva OpenAPI 3.0)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error"
                        ).permitAll()

                        // Permisos compartidos para ROLE_OWNER y ROLE_OPERATOR:
                        // Lectura general (GET) + Registro de bovinos, montas y partos
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").hasAnyRole("OWNER", "OPERATOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/bovinos/**").hasAnyRole("OWNER", "OPERATOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/bovinos", "/api/v1/bovinos/hembras", "/api/v1/bovinos/machos").hasAnyRole("OWNER", "OPERATOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bovinos/*/monta").hasAnyRole("OWNER", "OPERATOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bovinos/*/parto").hasAnyRole("OWNER", "OPERATOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/transacciones/**").hasAnyRole("OWNER", "OPERATOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/lotes/**").hasAnyRole("OWNER", "OPERATOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/clima/**").hasAnyRole("OWNER", "OPERATOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/rancho").hasAnyRole("OWNER", "OPERATOR")

                        // Cualquier otra operacion (finanzas, lotes escritura, edicion/borrado, usuarios, alertas)
                        // es exclusiva de ROLE_OWNER
                        .anyRequest().hasRole("OWNER")
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
