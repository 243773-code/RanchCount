package com.ranchcount.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
@SQLDelete(sql = "UPDATE transacciones SET eliminado_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("eliminado_at IS NULL")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 15)
    private TipoTransaccion tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 35)
    private CategoriaTransaccion categoria;

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "concepto", nullable = false, length = 255)
    private String concepto;

    @Column(name = "fecha_transaccion", nullable = false)
    private LocalDate fechaTransaccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bovino_id")
    private Bovino bovino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_siembra_id")
    private LoteSiembra loteSiembra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Usuario registradoPor;

    @Column(name = "creado_at", nullable = false, updatable = false)
    private LocalDateTime creadoAt;

    @Column(name = "eliminado_at")
    private LocalDateTime eliminadoAt;

    public Transaccion() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.creadoAt == null) {
            this.creadoAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransaccion tipo) {
        this.tipo = tipo;
    }

    public CategoriaTransaccion getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaTransaccion categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public LocalDate getFechaTransaccion() {
        return fechaTransaccion;
    }

    public void setFechaTransaccion(LocalDate fechaTransaccion) {
        this.fechaTransaccion = fechaTransaccion;
    }

    public Bovino getBovino() {
        return bovino;
    }

    public void setBovino(Bovino bovino) {
        this.bovino = bovino;
    }

    public LoteSiembra getLoteSiembra() {
        return loteSiembra;
    }

    public void setLoteSiembra(LoteSiembra loteSiembra) {
        this.loteSiembra = loteSiembra;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Usuario registradoPor) {
        this.registradoPor = registradoPor;
    }

    public LocalDateTime getCreadoAt() {
        return creadoAt;
    }

    public void setCreadoAt(LocalDateTime creadoAt) {
        this.creadoAt = creadoAt;
    }

    public LocalDateTime getEliminadoAt() {
        return eliminadoAt;
    }

    public void setEliminadoAt(LocalDateTime eliminadoAt) {
        this.eliminadoAt = eliminadoAt;
    }
}
