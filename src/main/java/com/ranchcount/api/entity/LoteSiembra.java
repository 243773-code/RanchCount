package com.ranchcount.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lotes_siembra")
public class LoteSiembra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_lote", nullable = false, unique = true, length = 40)
    private String codigoLote;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo_cultivo", nullable = false, length = 80)
    private String tipoCultivo;

    @Column(name = "hectareas", nullable = false, precision = 10, scale = 2)
    private BigDecimal hectareas;

    @Column(name = "latitud", precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(name = "inversion_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal inversionTotal;

    @Column(name = "fecha_siembra", nullable = false)
    private LocalDate fechaSiembra;

    @Column(name = "fecha_estimada_cosecha", nullable = false)
    private LocalDate fechaEstimadaCosecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 25)
    private EstadoCultivo estado;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "creado_at", nullable = false, updatable = false)
    private LocalDateTime creadoAt;

    @Column(name = "actualizado_at", nullable = false)
    private LocalDateTime actualizadoAt;

    public LoteSiembra() {
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

    public String getCodigoLote() {
        return codigoLote;
    }

    public void setCodigoLote(String codigoLote) {
        this.codigoLote = codigoLote;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoCultivo() {
        return tipoCultivo;
    }

    public void setTipoCultivo(String tipoCultivo) {
        this.tipoCultivo = tipoCultivo;
    }

    public BigDecimal getHectareas() {
        return hectareas;
    }

    public void setHectareas(BigDecimal hectareas) {
        this.hectareas = hectareas;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
    }

    public BigDecimal getInversionTotal() {
        return inversionTotal;
    }

    public void setInversionTotal(BigDecimal inversionTotal) {
        this.inversionTotal = inversionTotal;
    }

    public LocalDate getFechaSiembra() {
        return fechaSiembra;
    }

    public void setFechaSiembra(LocalDate fechaSiembra) {
        this.fechaSiembra = fechaSiembra;
    }

    public LocalDate getFechaEstimadaCosecha() {
        return fechaEstimadaCosecha;
    }

    public void setFechaEstimadaCosecha(LocalDate fechaEstimadaCosecha) {
        this.fechaEstimadaCosecha = fechaEstimadaCosecha;
    }

    public EstadoCultivo getEstado() {
        return estado;
    }

    public void setEstado(EstadoCultivo estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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
