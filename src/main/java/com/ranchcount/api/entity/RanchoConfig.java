package com.ranchcount.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rancho_config")
public class RanchoConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_rancho", nullable = false, length = 100)
    private String nombreRancho;

    @Column(name = "proposito", nullable = false, length = 120)
    private String proposito;

    @Column(name = "hectareas_totales", precision = 10, scale = 2)
    private BigDecimal hectareasTotales;

    @Column(name = "ubicacion_texto", length = 160)
    private String ubicacionTexto;

    @Column(name = "creado_at", nullable = false, updatable = false)
    private LocalDateTime creadoAt;

    @Column(name = "actualizado_at", nullable = false)
    private LocalDateTime actualizadoAt;

    public RanchoConfig() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.creadoAt = now;
        this.actualizadoAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreRancho() {
        return nombreRancho;
    }

    public void setNombreRancho(String nombreRancho) {
        this.nombreRancho = nombreRancho;
    }

    public String getProposito() {
        return proposito;
    }

    public void setProposito(String proposito) {
        this.proposito = proposito;
    }

    public BigDecimal getHectareasTotales() {
        return hectareasTotales;
    }

    public void setHectareasTotales(BigDecimal hectareasTotales) {
        this.hectareasTotales = hectareasTotales;
    }

    public String getUbicacionTexto() {
        return ubicacionTexto;
    }

    public void setUbicacionTexto(String ubicacionTexto) {
        this.ubicacionTexto = ubicacionTexto;
    }

    public LocalDateTime getCreadoAt() {
        return creadoAt;
    }

    public void setCreadoAt(LocalDateTime creadoAt) {
        this.creadoAt = creadoAt;
    }

    public LocalDateTime getActualizadoAt() {
        return actualizadoAt;
    }

    public void setActualizadoAt(LocalDateTime actualizadoAt) {
        this.actualizadoAt = actualizadoAt;
    }
}
