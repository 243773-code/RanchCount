-- =============================================================================
-- RanchCount (RanchZ) - Script DDL inicial PostgreSQL
-- Version  : 1.0.0
-- Motor    : PostgreSQL 15+
-- Charset  : UTF-8
-- Autor    : RanchCount Backend
-- Uso      : Ejecutar una vez en la BD vacia, o dejar ddl-auto=update en Spring
--            para que Hibernate lo gestione automaticamente en desarrollo.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. TABLA: usuarios
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id              BIGSERIAL       PRIMARY KEY,
    nombre_completo VARCHAR(120)    NOT NULL,
    username        VARCHAR(60)     NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    telefono_whatsapp VARCHAR(25),
    rol             VARCHAR(25)     NOT NULL
                        CHECK (rol IN ('ROLE_OWNER', 'ROLE_OPERATOR')),
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    creado_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios (LOWER(username));
CREATE INDEX IF NOT EXISTS idx_usuarios_rol_activo ON usuarios (rol, activo);

COMMENT ON TABLE  usuarios IS 'Propietarios y operadores del rancho con autenticacion JWT';
COMMENT ON COLUMN usuarios.rol IS 'ROLE_OWNER: acceso total; ROLE_OPERATOR: acceso operativo limitado';

-- ---------------------------------------------------------------------------
-- 2. TABLA: bovinos
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bovinos (
    id                      BIGSERIAL       PRIMARY KEY,
    arete_codigo            VARCHAR(40)     NOT NULL UNIQUE,
    nombre                  VARCHAR(80),
    raza                    VARCHAR(80)     NOT NULL,
    sexo                    VARCHAR(15)     NOT NULL
                                CHECK (sexo IN ('MACHO', 'HEMBRA')),
    fecha_nacimiento        DATE,
    peso_kg                 NUMERIC(8, 2)   CHECK (peso_kg > 0),
    estado                  VARCHAR(20)     NOT NULL
                                CHECK (estado IN ('VACIA', 'CARGADA', 'LACTANDO', 'ENGORDA', 'SEMENTAL', 'VENDIDO', 'BAJA')),
    fecha_monta             DATE,
    fecha_estimada_parto    DATE,
    fecha_ultimo_parto      DATE,
    ultima_alerta_parto     DATE,
    notas                   VARCHAR(500),
    creado_at               TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bovinos_estado ON bovinos (estado);
CREATE INDEX IF NOT EXISTS idx_bovinos_sexo ON bovinos (sexo);
CREATE INDEX IF NOT EXISTS idx_bovinos_estado_sexo ON bovinos (estado, sexo);
CREATE INDEX IF NOT EXISTS idx_bovinos_fecha_estimada_parto ON bovinos (fecha_estimada_parto)
    WHERE estado = 'CARGADA';
CREATE INDEX IF NOT EXISTS idx_bovinos_arete ON bovinos (LOWER(arete_codigo));

COMMENT ON TABLE  bovinos IS 'Inventario ganadero del rancho con seguimiento reproductivo';
COMMENT ON COLUMN bovinos.estado IS 'CARGADA = gestacion activa (monta + 283 dias = fecha_estimada_parto)';
COMMENT ON COLUMN bovinos.arete_codigo IS 'Identificador unico SINIIGA o interno, almacenado en mayusculas';

-- ---------------------------------------------------------------------------
-- 3. TABLA: lotes_siembra
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lotes_siembra (
    id                      BIGSERIAL           PRIMARY KEY,
    codigo_lote             VARCHAR(40)         NOT NULL UNIQUE,
    nombre                  VARCHAR(100)        NOT NULL,
    tipo_cultivo            VARCHAR(80)         NOT NULL,
    hectareas               NUMERIC(10, 2)      NOT NULL CHECK (hectareas > 0),
    latitud                 NUMERIC(9, 6)       CHECK (latitud BETWEEN -90 AND 90),
    longitud                NUMERIC(9, 6)       CHECK (longitud BETWEEN -180 AND 180),
    inversion_total         NUMERIC(15, 2)      NOT NULL CHECK (inversion_total >= 0),
    fecha_siembra           DATE                NOT NULL,
    fecha_estimada_cosecha  DATE                NOT NULL,
    estado                  VARCHAR(25)         NOT NULL
                                CHECK (estado IN ('PREPARACION', 'SEMBRADO', 'EN_CRECIMIENTO', 'COSECHADO', 'SINIESTRADO')),
    observaciones           VARCHAR(500),
    creado_at               TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_at          TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_lotes_fechas CHECK (fecha_estimada_cosecha > fecha_siembra)
);

CREATE INDEX IF NOT EXISTS idx_lotes_estado ON lotes_siembra (estado);
CREATE INDEX IF NOT EXISTS idx_lotes_fecha_siembra ON lotes_siembra (fecha_siembra DESC);
CREATE INDEX IF NOT EXISTS idx_lotes_codigo ON lotes_siembra (LOWER(codigo_lote));

COMMENT ON TABLE  lotes_siembra IS 'Parcelas agricolas con seguimiento de cultivo e inversion';
COMMENT ON COLUMN lotes_siembra.inversion_total IS 'Monto total invertido o presupuestado en el lote (MXN)';

-- ---------------------------------------------------------------------------
-- 4. TABLA: transacciones  (soft-delete via eliminado_at)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transacciones (
    id                  BIGSERIAL           PRIMARY KEY,
    tipo                VARCHAR(15)         NOT NULL
                            CHECK (tipo IN ('INGRESO', 'EGRESO')),
    categoria           VARCHAR(35)         NOT NULL
                            CHECK (categoria IN (
                                'VENTA_GANADO', 'VENTA_COSECHA', 'VENTA_LECHE',
                                'INSUMOS_AGRICOLAS', 'ALIMENTO_GANADO', 'VETERINARIA',
                                'NOMINA', 'MANTENIMIENTO', 'SERVICIOS', 'OTROS'
                            )),
    monto               NUMERIC(15, 2)      NOT NULL CHECK (monto > 0),
    concepto            VARCHAR(255)        NOT NULL,
    fecha_transaccion   DATE                NOT NULL,
    bovino_id           BIGINT              REFERENCES bovinos (id) ON DELETE SET NULL,
    lote_siembra_id     BIGINT              REFERENCES lotes_siembra (id) ON DELETE SET NULL,
    registrado_por_id   BIGINT              NOT NULL REFERENCES usuarios (id) ON DELETE RESTRICT,
    creado_at           TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    eliminado_at        TIMESTAMP                           -- NULL = activa; NOT NULL = eliminada logicamente
);

-- Indices para consultas frecuentes (solo registros activos)
CREATE INDEX IF NOT EXISTS idx_transacciones_tipo ON transacciones (tipo)
    WHERE eliminado_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_transacciones_fecha ON transacciones (fecha_transaccion DESC)
    WHERE eliminado_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_transacciones_tipo_fecha ON transacciones (tipo, fecha_transaccion DESC)
    WHERE eliminado_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_transacciones_bovino ON transacciones (bovino_id)
    WHERE eliminado_at IS NULL AND bovino_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_transacciones_lote ON transacciones (lote_siembra_id)
    WHERE eliminado_at IS NULL AND lote_siembra_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_transacciones_registrado_por ON transacciones (registrado_por_id);

COMMENT ON TABLE  transacciones IS 'Flujo de caja del rancho con borrado logico (eliminado_at IS NULL = activa)';
COMMENT ON COLUMN transacciones.eliminado_at IS 'Soft-delete: Hibernate @SQLDelete actualiza este campo; JPA @SQLRestriction lo filtra automaticamente';

-- ---------------------------------------------------------------------------
-- 5. FUNCION: actualizar campo updated_at automaticamente (opcional, Hibernate lo gestiona)
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_bovinos_updated_at
    BEFORE UPDATE ON bovinos
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE OR REPLACE TRIGGER trg_lotes_siembra_updated_at
    BEFORE UPDATE ON lotes_siembra
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- =============================================================================
-- FIN DEL SCRIPT DDL
-- =============================================================================
