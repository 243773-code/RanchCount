package com.ranchcount.api.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    private static final String LOCAL_FALLBACK_H2_URL =
            "jdbc:h2:file:./data/ranchcount_local;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE";

    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String dbUrl,
            @Value("${spring.datasource.username:postgres}") String dbUsername,
            @Value("${spring.datasource.password:}") String dbPassword,
            @Value("${app.datasource.dev-fallback-enabled:true}") boolean devFallbackEnabled) {

        if (dbUrl != null && dbUrl.startsWith("jdbc:h2:")) {
            return buildHikariDataSource(dbUrl, dbUsername, dbPassword, "org.h2.Driver");
        }

        if (!devFallbackEnabled) {
            return buildHikariDataSource(dbUrl, dbUsername, dbPassword, "org.postgresql.Driver");
        }

        try {
            probarConexionPostgreSql(dbUrl, dbUsername, dbPassword);
            log.info("Conexion verificada exitosamente contra PostgreSQL: {}", dbUrl);
            return buildHikariDataSource(dbUrl, dbUsername, dbPassword, "org.postgresql.Driver");
        } catch (SQLException ex) {
            log.warn("================================================================================");
            log.warn("No se detecto instancia activa de PostgreSQL en '{}' ({}).", dbUrl, ex.getMessage());
            log.warn("Activando BD local persistente en modo compatibilidad PostgreSQL (./data/ranchcount_local).");
            log.warn("En cuanto inicie PostgreSQL y configure DB_URL/DB_PASSWORD en .env, se usara PostgreSQL automaticamente.");
            log.warn("================================================================================");

            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException cnf) {
                throw new IllegalStateException(
                        "PostgreSQL no esta corriendo en " + dbUrl
                                + " y el driver H2 aun no fue recargado por IntelliJ. "
                                + "Inicie PostgreSQL o pulse 'Reload All Maven Projects' en IntelliJ IDEA.",
                        ex
                );
            }

            return buildHikariDataSource(LOCAL_FALLBACK_H2_URL, "sa", "", "org.h2.Driver");
        }
    }

    private void probarConexionPostgreSql(String url, String username, String password) throws SQLException {
        Properties props = new Properties();
        if (username != null) {
            props.setProperty("user", username);
        }
        if (password != null) {
            props.setProperty("password", password);
        }
        props.setProperty("connectTimeout", "2");
        props.setProperty("loginTimeout", "2");

        try (Connection ignored = DriverManager.getConnection(url, props)) {
            // Conexion exitosa
        }
    }

    private DataSource buildHikariDataSource(String url, String username, String password, String driverClassName) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        config.setPoolName("RanchCountHikariPool");
        return new HikariDataSource(config);
    }
}
