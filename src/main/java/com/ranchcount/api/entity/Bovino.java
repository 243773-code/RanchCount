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
@Table(name = "bovinos")
public class Bovino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "arete_codigo", nullable = false, unique = true, length = 40)
    private String areteCodigo;

    @Column(name = "nombre", length = 80)
    private String nombre;

    @Column(name = "raza", nullable = false, length = 80)
    private String raza;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false, length = 15)
    private SexoBovino sexo;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "peso_kg", precision = 8, scale = 2)
    private BigDecimal pesoKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoBovino estado;

    @Column(name = "fecha_monta")
    private LocalDate fechaMonta;

    @Column(name = "fecha_estimada_parto")
    private LocalDate fechaEstimadaParto;

    @Column(name = "fecha_ultimo_parto")
    private LocalDate fechaUltimoParto;

    @Column(name = "ultima_alerta_parto")
    private LocalDate ultimaAlertaParto;

    @Column(name = "notas", length = 500)
    private String notas;

    @Column(name = "creado_at", nullable = false, updatable = false)
    private LocalDateTime creadoAt;

    @Column(name = "actualizado_at", nullable = false)
    private LocalDateTime actualizadoAt;

    public Bovino() {
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

    public String getAreteCodigo() {
        return areteCodigo;
    }

    public void setAreteCodigo(String areteCodigo) {
        this.areteCodigo = areteCodigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public SexoBovino getSexo() {
        return sexo;
    }

    public void setSexo(SexoBovino sexo) {
        this.sexo = sexo;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public EstadoBovino getEstado() {
        return estado;
    }

    public void setEstado(EstadoBovino estado) {
        this.estado = estado;
    }

    public LocalDate getFechaMonta() {
        return fechaMonta;
    }

    public void setFechaMonta(LocalDate fechaMonta) {
        this.fechaMonta = fechaMonta;
    }

    public LocalDate getFechaEstimadaParto() {
        return fechaEstimadaParto;
    }

    public void setFechaEstimadaParto(LocalDate fechaEstimadaParto) {
        this.fechaEstimadaParto = fechaEstimadaParto;
    }

    public LocalDate getFechaUltimoParto() {
        return fechaUltimoParto;
    }

    public void setFechaUltimoParto(LocalDate fechaUltimoParto) {
        this.fechaUltimoParto = fechaUltimoParto;
    }

    public LocalDate getUltimaAlertaParto() {
        return ultimaAlertaParto;
    }

    public void setUltimaAlertaParto(LocalDate ultimaAlertaParto) {
        this.ultimaAlertaParto = ultimaAlertaParto;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
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
