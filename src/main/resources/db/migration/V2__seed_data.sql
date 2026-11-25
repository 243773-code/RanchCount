-- =============================================================================
-- RanchCount (RanchZ) - Datos de prueba / seed para desarrollo
-- ATENCION: NO ejecutar en produccion. Solo para entorno dev/testing.
-- El usuario OWNER se crea automaticamente via DataInitializer.java
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Bovinos de ejemplo
-- ---------------------------------------------------------------------------
INSERT INTO bovinos (arete_codigo, nombre, raza, sexo, fecha_nacimiento, peso_kg, estado, notas)
VALUES
    ('MX-JAL-0001', 'Canela',    'Brahman',       'HEMBRA', '2022-03-15', 480.00, 'VACIA',    'Excelente productora'),
    ('MX-JAL-0002', 'Paloma',    'Angus',          'HEMBRA', '2021-07-20', 510.50, 'LACTANDO', 'Parto exitoso Octubre 2026'),
    ('MX-JAL-0003', 'Tornado',   'Charolais',      'MACHO',  '2020-01-10', 820.00, 'SEMENTAL', 'Semental principal del rancho'),
    ('MX-JAL-0004', 'Estrella',  'Simmental',      'HEMBRA', '2023-05-05', 340.00, 'VACIA',    NULL),
    ('MX-JAL-0005', 'Negrito',   'Angus',          'MACHO',  '2024-01-18', 290.00, 'ENGORDA',  'Destete reciente'),
    ('MX-JAL-0006', 'Lupita',    'Brahman x Angus','HEMBRA', '2022-11-30', 460.00, 'CARGADA',  'Monta con semental Tornado')
ON CONFLICT (arete_codigo) DO NOTHING;

-- Registrar monta para Lupita (MX-JAL-0006) -> fecha estimada parto = fecha_monta + 283 dias
UPDATE bovinos
SET fecha_monta          = CURRENT_DATE - INTERVAL '100 days',
    fecha_estimada_parto = CURRENT_DATE - INTERVAL '100 days' + INTERVAL '283 days'
WHERE arete_codigo = 'MX-JAL-0006';

-- ---------------------------------------------------------------------------
-- Lotes de siembra de ejemplo
-- ---------------------------------------------------------------------------
INSERT INTO lotes_siembra (codigo_lote, nombre, tipo_cultivo, hectareas, latitud, longitud, inversion_total,
                            fecha_siembra, fecha_estimada_cosecha, estado, observaciones)
VALUES
    ('LOTE-N-01', 'Parcela Norte',    'Sorgo Forrajero', 15.00, 20.661200, -103.351000, 67500.00,
     '2026-06-01', '2026-11-15', 'EN_CRECIMIENTO', 'Riego por gravedad; buen desarrollo'),
    ('LOTE-S-01', 'Parcela Sur',      'Avena Forrajera', 10.00, 20.658400, -103.348200, 45000.00,
     '2026-07-15', '2026-12-10', 'SEMBRADO',       'Sin incidencias'),
    ('LOTE-C-01', 'Cerro la Loma',    'Maiz Blanco',     8.50,  20.663100, -103.354500, 38250.00,
     '2025-05-20', '2025-10-30', 'COSECHADO',      'Rendimiento: 6.2 ton/ha. Excelente ciclo'),
    ('LOTE-E-01', 'El Aguaje',        'Sorgo Escobero',  5.00,  20.657000, -103.346100, 12000.00,
     '2026-08-01', '2026-12-20', 'SEMBRADO',       NULL)
ON CONFLICT (codigo_lote) DO NOTHING;

-- =============================================================================
-- FIN DATOS SEMILLA
-- =============================================================================
