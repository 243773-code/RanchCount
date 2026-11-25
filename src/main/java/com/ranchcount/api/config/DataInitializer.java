package com.ranchcount.api.config;

import com.ranchcount.api.entity.RolUsuario;
import com.ranchcount.api.entity.Usuario;
import com.ranchcount.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.owner.enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${app.bootstrap.owner.full-name:Administrador del Rancho}")
    private String ownerFullName;

    @Value("${app.bootstrap.owner.username:owner}")
    private String ownerUsername;

    // Sin valor por defecto: la contrasena solo se toma de INITIAL_OWNER_PASSWORD (.env o panel de Render)
    @Value("${app.bootstrap.owner.password:}")
    private String ownerPassword;

    @Value("${app.bootstrap.owner.phone:}")
    private String ownerPhone;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!bootstrapEnabled) {
            return;
        }

        String targetUsername = (ownerUsername != null && !ownerUsername.isBlank())
                ? ownerUsername.trim().toLowerCase()
                : "owner";

        if (ownerPassword == null || ownerPassword.isBlank()) {
            log.warn("INITIAL_OWNER_PASSWORD no configurada; se omite la creacion/sincronizacion del usuario ROLE_OWNER.");
            return;
        }
        String targetPassword = ownerPassword.trim();

        Optional<Usuario> existingOwner = usuarioRepository.findByUsernameIgnoreCase(targetUsername);

        if (existingOwner.isEmpty()) {
            Usuario nuevoOwner = new Usuario(
                    ownerFullName,
                    targetUsername,
                    passwordEncoder.encode(targetPassword),
                    (ownerPhone != null && !ownerPhone.isBlank()) ? ownerPhone.trim() : null,
                    RolUsuario.ROLE_OWNER);
            nuevoOwner.setActivo(true);
            usuarioRepository.save(nuevoOwner);
            log.info("Usuario ROLE_OWNER inicial ('{}') creado exitosamente.", targetUsername);
        } else {
            Usuario owner = existingOwner.get();
            // Asegurar que este activo y con la contrasena configurada en .env
            owner.setActivo(true);
            owner.setRol(RolUsuario.ROLE_OWNER);
            owner.setPasswordHash(passwordEncoder.encode(targetPassword));
            usuarioRepository.save(owner);
            log.info("Usuario ROLE_OWNER ('{}') verificado y contrasena sincronizada con .env.", targetUsername);
        }
    }
}
