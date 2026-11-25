package com.ranchcount.api.entity;

/**
 * Estados permitidos para el control ganadero.
 * Regla de dominio estricta: sin uso de la letra N con tilde; se utiliza CARGADA para gestacion.
 */
public enum EstadoBovino {
    VACIA,
    CARGADA,
    LACTANDO,
    ENGORDA,
    SEMENTAL,
    VENDIDO,
    BAJA
}
