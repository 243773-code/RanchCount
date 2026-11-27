# Plan y Documentación de Pruebas de Servicios Web (QA) — RanchCount (RanchZ) Full Stack

> **Materia / Unidad:** U4 - Plan y Documentación de Pruebas de Servicios Web (QA)  
> **Proyecto:** RanchCount (RanchZ) — API RESTful + PostgreSQL (Neon) + Cliente Móvil Android (Kotlin)  
> **Fecha de elaboración:** 2026-11-26  
> **Alcance:** 33 endpoints REST distribuidos en 7 controladores (`/api/v1/**`) + documentación OpenAPI 3.0 (`/v3/api-docs`, `/swagger-ui.html`)

---

## 1. Objetivo y Estrategia de Pruebas

Validar la estabilidad, seguridad, integridad de datos y cumplimiento de reglas de negocio de los servicios web de **RanchCount (RanchZ)** mediante pruebas estructuradas (manuales/semiautomatizadas en **Insomnia** y automatizadas con **JUnit 5 + MockMvc + H2**), cubriendo tanto flujos esperados (*Happy Path*) como escenarios de error, validación de esquemas, vulnerabilidades básicas y buenas prácticas REST.

### 1.1 Entorno de Pruebas

| Componente | Tecnología / Configuración |
|---|---|
| **Servidor API** | Spring Boot 3.4.3 · Java 17 · Puerto `8080` (`http://localhost:8080/api/v1`) |
| **Base de Datos** | PostgreSQL 15+ en la nube (**Neon** con `sslmode=require`) + H2 en modo PostgreSQL para suite automatizada |
| **Seguridad** | Spring Security 6 *Stateless* · JWT firmado con HMAC-SHA256 (`jjwt 0.12.6`) · Contraseñas con **BCrypt** · RBAC (`ROLE_OWNER`, `ROLE_OPERATOR`) |
| **Integraciones Externas** | Open-Meteo API (clima local) · Telegram Bot API (alertas automáticas de partos) |
| **Cliente de Pruebas QA** | Insomnia · Swagger UI · JUnit 5 + MockMvc + MockWebServer |

### 1.2 Trazabilidad de Módulos Evaluados

| Módulo | Controlador | Prefijo |
|---|---|---|
| Autenticación y Perfil | `AuthController` | `/api/v1/auth` |
| Gestión de Usuarios | `UsuarioController` | `/api/v1/usuarios` |
| Control Ganadero | `BovinoController` | `/api/v1/bovinos` |
| Flujo de Caja | `TransaccionController` | `/api/v1/transacciones` |
| Control Agrícola | `LoteSiembraController` | `/api/v1/lotes` |
| Meteorología | `ClimaController` | `/api/v1/clima` |
| Alertas Programadas | `AlertaController` | `/api/v1/alertas` |

---

## 2. Matriz Resumen de Casos de Prueba (17 Casos)

| ID | Categoría | Título / Escenario | Método y Endpoint | Rol / Auth | Resultado Esperado | Estado |
|---|---|---|---|---|---|---|
| **CP-01** | Funcional (Camino Feliz) | Login exitoso de propietario y emisión de JWT | `POST /api/v1/auth/login` | Público | `200 OK` con `accessToken`, `tokenType: "Bearer"` y `rol: "ROLE_OWNER"` | ✅ PASS |
| **CP-02** | Funcional (Camino Feliz) | Alta de hembra bovina en endpoint específico | `POST /api/v1/bovinos/hembras` | `OWNER` / `OPERATOR` | `201 Created`, `sexo: "HEMBRA"`, `areteCodigo` normalizado a mayúsculas | ✅ PASS |
| **CP-03** | Funcional (Camino Feliz) | Registro de monta natural por días aproximados | `PATCH /api/v1/bovinos/{id}/monta` | `OWNER` / `OPERATOR` | `200 OK`, `estado: "CARGADA"`, retro-cálculo `fechaMonta` y `+283` días en `fechaEstimadaParto` | ✅ PASS |
| **CP-04** | Funcional (Camino Feliz) | Creación de lote agrícola con coordenadas GPS | `POST /api/v1/lotes` | `OWNER` | `201 Created`, `latitud`/`longitud` guardadas e `inversionPorHectarea` calculada | ✅ PASS |
| **CP-05** | Funcional (Camino Feliz) | Borrado lógico de transacción (`@SQLDelete`) | `DELETE /api/v1/transacciones/{id}` | `OWNER` | `204 No Content`; posterior `GET` retorna `404` pero persiste `eliminado_at` en BD | ✅ PASS |
| **CP-06** | Validación Tipos/Esquemas | Campos obligatorios vacíos, negativos y latitud fuera de rango | `POST /api/v1/lotes` | `OWNER` | `400 Bad Request` con mapa `validationErrors` (`codigoLote`, `hectareas`, `latitud`) | ✅ PASS |
| **CP-07** | Validación Tipos/Esquemas | Texto en campo numérico, enum inválido o JSON malformado | `POST /api/v1/bovinos` | `OWNER` / `OPERATOR` | `400 Bad Request` controlado sin caída del servidor | ✅ PASS |
| **CP-08** | Validación Tipos/Esquemas | Username con formato inválido y contraseña menor a 8 caracteres | `POST /api/v1/usuarios` | `OWNER` | `400 Bad Request` con `validationErrors` de `@Pattern` y `@Size` | ✅ PASS |
| **CP-09** | Lógica de Negocio | Registrar macho con estado reproductivo femenino (`CARGADA`) | `POST /api/v1/bovinos/machos` | `OWNER` / `OPERATOR` | `400 Bad Request` (`BusinessRuleException`) rechazando incompatibilidad sexo/estado | ✅ PASS |
| **CP-10** | Lógica de Negocio | Registro duplicado de arete bovino (`areteCodigo`) | `POST /api/v1/bovinos` | `OWNER` / `OPERATOR` | `409 Conflict` (`DuplicateResourceException`) protegiendo unicidad | ✅ PASS |
| **CP-11** | Lógica de Negocio | Consulta de recurso con ID inexistente (`99999`) | `GET /api/v1/bovinos/99999` | `OWNER` / `OPERATOR` | `404 Not Found` (`ResourceNotFoundException`) | ✅ PASS |
| **CP-12** | Lógica de Negocio | Coordenada incompleta (`latitud` sin `longitud`) o monta ambigua | `POST /api/v1/lotes` | `OWNER` | `400 Bad Request` (`BusinessRuleException`) exigiendo par de coordenadas | ✅ PASS |
| **CP-13** | Seguridad | Acceso sin token JWT o con token alterado/inválido | `GET /api/v1/bovinos` | Sin token / Alterado | `401 Unauthorized` emitido por `JwtAuthenticationEntryPoint` | ✅ PASS |
| **CP-14** | Seguridad (RBAC) | Operador intenta acceder a finanzas o gestión de usuarios | `POST /api/v1/transacciones` | `ROLE_OPERATOR` | `403 Forbidden` emitido por `CustomAccessDeniedHandler` | ✅ PASS |
| **CP-15** | Seguridad (Inyección/XSS) | Inyección SQL en login (`' OR 1=1 --`) y XSS en username (`<script>`) | `POST /auth/login` y `POST /usuarios` | Público / `OWNER` | `401 Unauthorized` (JPA parametrizado) y `400 Bad Request` (`@Pattern` rechaza `<script>`) | ✅ PASS |
| **CP-16** | Seguridad (Exposición) | Verificación de no exposición de stack traces, SQL ni `passwordHash` | `GET /api/v1/ruta-inexistente` y `GET /usuarios` | `OWNER` | `404`/`200` sin `stackTrace`, sin nombres de tabla y sin `passwordHash` | ✅ PASS |
| **CP-17** | Buenas Prácticas REST | Verbos HTTP, `Content-Type`, OpenAPI 3.0, CORS y latencia | `GET /v3/api-docs` y `GET /clima/actual` | Público / `OWNER` | `200 OK`, `Content-Type: application/json`, tiempo `< 500ms` local, CORS cerrado por defecto | ✅ PASS |

---

## 3. Especificación Detallada de Casos de Prueba y Evidencias

### Categoría 1: Pruebas Funcionales (Camino Feliz)

#### CP-01 — Autenticación exitosa del propietario (Login JWT)
* **Endpoint:** `POST http://localhost:8080/api/v1/auth/login`
* **Headers:** `Content-Type: application/json`
* **Body enviado:**
  ```json
  {
    "username": "owner",
    "password": "Zebadua2005"
  }
  ```
* **Resultado esperado:** Código `200 OK`, `Content-Type: application/json`, token Bearer emitido y `rol: "ROLE_OWNER"`.

![Evidencia CP-01](evidencias/CP-01.png)

#### CP-02 — Registro exitoso de bovino hembra en endpoint específico
* **Endpoint:** `POST http://localhost:8080/api/v1/bovinos/hembras`
* **Headers:** `Authorization: Bearer {{ownerToken}}`, `Content-Type: application/json`
* **Body enviado:**
  ```json
  {
    "areteCodigo": "mx-qa-001",
    "nombre": "Estrella QA",
    "raza": "Brahman",
    "fechaNacimiento": "2023-03-15",
    "pesoKg": 445.00,
    "estado": "VACIA",
    "notas": "Registro funcional QA"
  }
  ```
* **Resultado esperado:** Código `201 Created`, `"sexo": "HEMBRA"`, `"areteCodigo": "MX-QA-001"` (normalizado a mayúsculas).

![Evidencia CP-02](evidencias/CP-02.png)

#### CP-03 — Registro de monta natural por días aproximados (`+283` días)
* **Endpoint:** `PATCH http://localhost:8080/api/v1/bovinos/{{bovinoId}}/monta`
* **Headers:** `Authorization: Bearer {{ownerToken}}`, `Content-Type: application/json`
* **Body enviado:**
  ```json
  {
    "diasGestacionAproximados": 90,
    "notas": "Monta natural estimada por palpacion (~3 meses)"
  }
  ```
* **Resultado esperado:** Código `200 OK`, `"estado": "CARGADA"`, `"fechaMonta"` retro-calculada (`hoy - 90 días`) y `"fechaEstimadaParto"` igual a `fechaMonta + 283 días`.

![Evidencia CP-03](evidencias/CP-03.png)

#### CP-04 — Creación de lote agrícola con coordenadas GPS e inversión por hectárea
* **Endpoint:** `POST http://localhost:8080/api/v1/lotes`
* **Headers:** `Authorization: Bearer {{ownerToken}}`, `Content-Type: application/json`
* **Body enviado:**
  ```json
  {
    "codigoLote": "LOTE-QA-01",
    "nombre": "Parcela QA Sur",
    "tipoCultivo": "Sorgo Forrajero",
    "hectareas": 10.00,
    "latitud": 20.659700,
    "longitud": -103.349600,
    "inversionTotal": 45000.00,
    "fechaSiembra": "2026-06-01",
    "fechaEstimadaCosecha": "2026-12-01",
    "estado": "EN_CRECIMIENTO",
    "observaciones": "Lote georreferenciado de prueba"
  }
  ```
* **Resultado esperado:** Código `201 Created`, `"inversionPorHectarea": 4500.00`, `"latitud": 20.659700`, `"longitud": -103.349600`.

![Evidencia CP-04](evidencias/CP-04.png)

#### CP-05 — Eliminación lógica de transacción financiera (`@SQLDelete`)
* **Precondición:** Crear previamente una transacción con `POST /api/v1/transacciones` para obtener su ID.
* **Endpoint:** `DELETE http://localhost:8080/api/v1/transacciones/{{transaccionId}}`
* **Headers:** `Authorization: Bearer {{ownerToken}}`
* **Resultado esperado:** Código `204 No Content` sin cuerpo. Un `GET` posterior a ese ID devuelve `404 Not Found` gracias a `@SQLRestriction("eliminado_at IS NULL")`, manteniendo la fila auditada en PostgreSQL.

![Evidencia CP-05](evidencias/CP-05.png)

---

### Categoría 2: Validación de Tipos de Datos y Esquemas

#### CP-06 — Campos obligatorios vacíos, valores negativos y latitud fuera de rango
* **Endpoint:** `POST http://localhost:8080/api/v1/lotes`
* **Headers:** `Authorization: Bearer {{ownerToken}}`, `Content-Type: application/json`
* **Body enviado:**
  ```json
  {
    "codigoLote": "",
    "nombre": "Parcela Error",
    "tipoCultivo": "Maiz",
    "hectareas": -5.0,
    "latitud": 95.0,
    "longitud": -103.349600,
    "inversionTotal": 15000.00,
    "fechaSiembra": "2026-06-01",
    "fechaEstimadaCosecha": "2026-10-15",
    "estado": "SEMBRADO"
  }
  ```
* **Resultado esperado:** Código `400 Bad Request` con objeto `validationErrors` detallando `codigoLote`, `hectareas` y `latitud`.

![Evidencia CP-06](evidencias/CP-06.png)

#### CP-07 — Tipo de dato incompatible (texto en numérico / enum inexistente)
* **Endpoint:** `POST http://localhost:8080/api/v1/bovinos`
* **Headers:** `Authorization: Bearer {{ownerToken}}`, `Content-Type: application/json`
* **Body enviado:**
  ```json
  {
    "areteCodigo": "MX-ERR-TIPO",
    "raza": "Angus",
    "sexo": "DESCONOCIDO",
    "pesoKg": "cuatrocientos",
    "estado": "VACIA"
  }
  ```
* **Resultado esperado:** Código `400 Bad Request` capturado por `HttpMessageNotReadableException`: `"El cuerpo JSON de la peticion tiene formato invalido o contiene valores enum/fecha no reconocidos"`. El servidor permanece estable.

![Evidencia CP-07](evidencias/CP-07.png)

#### CP-08 — Validación de formato de `username` y longitud mínima de `password`
* **Endpoint:** `POST http://localhost:8080/api/v1/usuarios`
* **Headers:** `Authorization: Bearer {{ownerToken}}`, `Content-Type: application/json`
* **Body enviado:**
  ```json
  {
    "nombreCompleto": "Usuario Invalido",
    "username": "juan perez!!",
    "password": "123",
    "telefonoWhatsapp": "abc",
    "rol": "ROLE_OPERATOR"
  }
  ```
* **Resultado esperado:** Código `400 Bad Request` con `validationErrors` para `username`, `password` y `telefonoWhatsapp`.

![Evidencia CP-08](evidencias/CP-08.png)

---

### Categoría 3: Lógica de Negocio y Errores Lógicos

#### CP-09 — Incompatibilidad de sexo y estado reproductivo (Macho en estado `CARGADA`)
* **Endpoint:** `POST http://localhost:8080/api/v1/bovinos/machos`
* **Headers:** `Authorization: Bearer {{ownerToken}}`, `Content-Type: application/json`
* **Body enviado:**
  ```json
  {
    "areteCodigo": "MX-MAC-ERR-01",
    "nombre": "Toro Prueba",
    "raza": "Charolais",
    "fechaNacimiento": "2022-01-01",
    "pesoKg": 750.00,
    "estado": "CARGADA"
  }
  ```
* **Resultado esperado:** Código `400 Bad Request` (`BusinessRuleException`): `"Un ejemplar MACHO no puede asignarse al estado reproductivo femenino CARGADA"`.

![Evidencia CP-09](evidencias/CP-09.png)

#### CP-10 — Prevención de duplicidad de código de arete (`409 Conflict`)
* **Precondición:** El arete `"MX-QA-001"` ya fue registrado previamente.
* **Endpoint:** `POST http://localhost:8080/api/v1/bovinos/hembras`
* **Headers:** `Authorization: Bearer {{ownerToken}}`, `Content-Type: application/json`
* **Body enviado:**
  ```json
  {
    "areteCodigo": "MX-QA-001",
    "nombre": "Duplicada",
    "raza": "Brahman",
    "pesoKg": 420.00,
    "estado": "VACIA"
  }
  ```
* **Resultado esperado:** Código `409 Conflict` (`DuplicateResourceException`): `"Ya existe un bovino registrado con el arete: MX-QA-001"`.

![Evidencia CP-10](evidencias/CP-10.png)

#### CP-11 — Consulta de bovino con ID inexistente (`404 Not Found`)
* **Endpoint:** `GET http://localhost:8080/api/v1/bovinos/99999`
* **Headers:** `Authorization: Bearer {{ownerToken}}`
* **Resultado esperado:** Código `404 Not Found` (`ResourceNotFoundException`): `"Bovino no encontrado con id: '99999'"`.

![Evidencia CP-11](evidencias/CP-11.png)

#### CP-12 — Coordenadas GPS incompletas (latitud sin longitud)
* **Endpoint:** `POST http://localhost:8080/api/v1/lotes`
* **Headers:** `Authorization: Bearer {{ownerToken}}`, `Content-Type: application/json`
* **Body enviado:**
  ```json
  {
    "codigoLote": "LOTE-SIN-LON",
    "nombre": "Parcela Incompleta",
    "tipoCultivo": "Avena",
    "hectareas": 6.00,
    "latitud": 20.659700,
    "inversionTotal": 12000.00,
    "fechaSiembra": "2026-06-01",
    "fechaEstimadaCosecha": "2026-11-30",
    "estado": "SEMBRADO"
  }
  ```
* **Resultado esperado:** Código `400 Bad Request` (`BusinessRuleException`): `"Debe proporcionar tanto la latitud como la longitud del lote"`.

![Evidencia CP-12](evidencias/CP-12.png)

---

### Categoría 4: Seguridad y Búsqueda de Vulnerabilidades Básicas

#### CP-13 — Control de acceso sin token y con token alterado (`401 Unauthorized`)
* **Subcaso A (Sin token):** `GET http://localhost:8080/api/v1/bovinos` sin encabezado `Authorization`.
* **Subcaso B (Token alterado):** `GET http://localhost:8080/api/v1/bovinos` con `Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.token_alterado.firma_invalida`.
* **Resultado esperado:** Código `401 Unauthorized` emitido por `JwtAuthenticationEntryPoint` con mensaje controlado (`"Autenticacion requerida..."` o `"Token JWT invalido o malformado"`).

![Evidencia CP-13](evidencias/CP-13.png)

#### CP-14 — Escalación de privilegios por rol insuficiente (`403 Forbidden`)
* **Precondición:** Iniciar sesión con un usuario de rol `ROLE_OPERATOR` (ej. `operador_qa`) y usar su token.
* **Endpoint:** `POST http://localhost:8080/api/v1/transacciones`
* **Headers:** `Authorization: Bearer {{operatorToken}}`, `Content-Type: application/json`
* **Body enviado:**
  ```json
  {
    "tipo": "EGRESO",
    "categoria": "ALIMENTO_GANADO",
    "monto": 1500.00,
    "concepto": "Intento no autorizado desde operador",
    "fechaTransaccion": "2026-11-26"
  }
  ```
* **Resultado esperado:** Código `403 Forbidden` emitido por `CustomAccessDeniedHandler`, bloqueando al operador de registrar o alterar finanzas.

![Evidencia CP-14](evidencias/CP-14.png)

#### CP-15 — Pruebas de Inyección SQL y XSS básico en entradas
* **Subcaso A (Inyección SQL en Login):**
  * **Endpoint:** `POST http://localhost:8080/api/v1/auth/login`
  * **Body:**
    ```json
    {
      "username": "' OR 1=1 --",
      "password": "' OR '1'='1"
    }
    ```
  * **Resultado esperado:** Código `401 Unauthorized` (`"Usuario o contrasena incorrectos"`). JPA/Hibernate usa consultas parametrizadas (`PreparedStatement`), impidiendo alterar la sentencia SQL.
* **Subcaso B (Payload XSS en creación de usuario):**
  * **Endpoint:** `POST http://localhost:8080/api/v1/usuarios` (`Authorization: Bearer {{ownerToken}}`)
  * **Body:**
    ```json
    {
      "nombreCompleto": "Prueba XSS",
      "username": "<script>alert(1)</script>",
      "password": "PasswordValida123!",
      "rol": "ROLE_OPERATOR"
    }
    ```
  * **Resultado esperado:** Código `400 Bad Request` rechazado por la expresión regular `@Pattern(regexp = "^[a-zA-Z0-9._-]+$")`.

![Evidencia CP-15.1 - SQL Injection](evidencias/CP-15.1.png)

![Evidencia CP-15.2 - XSS](evidencias/CP-15.2.png)

#### CP-16 — Verificación de no exposición de información sensible y stack traces
* **Subcaso A (Error en ruta inexistente):** `GET http://localhost:8080/api/v1/endpoint-falso` (`Authorization: Bearer {{ownerToken}}`) → `404 Not Found` con `ApiErrorResponse` limpio (sin stack trace, sin clases internas ni nombres de tablas).
* **Subcaso B (Listado de usuarios):** `GET http://localhost:8080/api/v1/usuarios` (`Authorization: Bearer {{ownerToken}}`) → `200 OK` verificando que el DTO `UsuarioResponse` **nunca** incluye el campo `passwordHash`.

![Evidencia CP-16.1 - Error sin stack trace](evidencias/CP-16.1.png)

![Evidencia CP-16.2 - Usuarios sin passwordHash](evidencias/CP-16.2.png)

---

### Categoría 5: Buenas Prácticas de APIs REST

#### CP-17 — Verbos HTTP, Headers, OpenAPI 3.0, CORS y Tiempos de Respuesta
* **Endpoints:**
  1. `GET http://localhost:8080/v3/api-docs` (público) → verifica especificación OpenAPI 3.0 activa y `Content-Type: application/json`.
  2. `GET http://localhost:8080/api/v1/clima/actual` (`Authorization: Bearer {{ownerToken}}`) → verifica integración externa con `WebClient` (timeout configurado en 8s/12s en `WebClientConfig`).
  3. Verificación en respuesta de headers: `Content-Type: application/json`, método HTTP semántico (`GET`/`POST`/`PUT`/`PATCH`/`DELETE`), tiempos locales `< 500 ms` y ausencia de cabecera permisiva `Access-Control-Allow-Origin: *` (CORS cerrado por defecto dado que el consumidor principal es la app nativa Android con Retrofit).

![Evidencia CP-17 - Buenas practicas REST](evidencias/CP-17.2.png)

---

## 4. Honestidad Técnica — Defectos Reales Detectados y Corregidos Durante el QA

Durante la fase de pruebas previas a la entrega se detectaron y resolvieron **4 incidentes reales** documentados con transparencia técnica:

| ID Defecto | Síntoma / Error Detectado | Causa Raíz | Corrección Aplicada | Estado |
|---|---|---|---|---|
| **BUG-01** | `Connection refused: /[0:0:0:0:0:0:0:1]:80` al disparar `POST /api/v1/alertas/verificar-partos` | `TelegramAlertClient` construía la ruta como URI relativa (`/bot...`) y `WebClient` resolvía hacia `localhost:80` en lugar de `api.telegram.org`. | Se reemplazó por la URI absoluta `https://api.telegram.org/bot{token}/sendMessage?...` en `TelegramAlertClient.java`. | ✅ Corregido |
| **BUG-02** | `401 Unauthorized ("Usuario o contrasena incorrectos")` en `POST /api/v1/auth/login` tras actualizar `INITIAL_OWNER_PASSWORD` en `.env` | `DataInitializer` únicamente creaba el usuario `owner` cuando `usuarioRepository.count() == 0`; sobre la BD ya poblada de Neon conservaba el hash antiguo. | Se actualizó `DataInitializer.java` para verificar al arranque al usuario `owner` y sincronizar su hash BCrypt con `.env`. | ✅ Corregido |
| **BUG-03** | Fallo de proveedor externo de mensajería CallMeBot (`"This bot is full..."`) bloqueando las alertas de parto | Limitación del servicio gratuito no oficial de CallMeBot sobre WhatsApp. | Migración completa hacia la API oficial gratuita de **Telegram Bot** (`TelegramAlertClient`, `WebClientConfig`, `AlertaPartoServiceImpl`). | ✅ Corregido |
| **BUG-04** | Brecha funcional en registro ganadero y agrícola detectada en revisión técnica | `LoteSiembra` carecía de georreferenciación GPS, `BovinoController` no ofrecía endpoints separados por sexo (`/hembras`, `/machos`) y `MontaRequest` no contemplaba monta natural sin fecha observada. | Se añadieron `latitud`/`longitud` pareadas en `LoteSiembra`, endpoints `/hembras` y `/machos` conservando `GET /bovinos` general, y `diasGestacionAproximados` en `MontaRequest`. | ✅ Corregido |

---

## 5. Cobertura Automatizada Complementaria en el Repositorio

Además de las pruebas manuales en Insomnia, el repositorio cuenta con pruebas de integración automatizadas ejecutables con JUnit 5 y MockMvc:

* [`RanchCountIntegrationTests.java`](../../src/test/java/com/ranchcount/api/RanchCountIntegrationTests.java):
  * Test 1: Seguridad JWT (`401`), creación de operador (`201`), bloqueo RBAC (`403`), regla de gestación `+283` días y reporte de parto con alta automática de cría.
  * Test 2: Precisión financiera con `BigDecimal` y verificación física de borrado lógico (`@SQLDelete` / `@SQLRestriction`) en tabla `transacciones`.
  * Test 3: Validaciones Jakarta (`400` con `validationErrors`), cálculo de inversión por hectárea y contrato OpenAPI `/v3/api-docs`.
  * Test 4: Coordenadas GPS en `Lotes`, endpoints separados por sexo (`/hembras`, `/machos`) y monta natural por `diasGestacionAproximados`.
* [`ExternalClientsIntegrationTests.java`](../../src/test/java/com/ranchcount/api/ExternalClientsIntegrationTests.java):
  * Tests 4 y 5: Simulación con `MockWebServer` del consumo real de Open-Meteo y Telegram Bot API, incluyendo control anti-duplicidad diaria de alertas.
