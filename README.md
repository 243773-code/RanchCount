# RanchCount (`RanchZ`) — Backend RESTful API

API RESTful empresarial desarrollada con **Java 17 y Spring Boot 3.4.x** para la gestión operativa, ganadera, agrícola y financiera de un rancho, diseñada para ser consumida por la aplicación móvil nativa **Android (Kotlin + Retrofit)**.

---

## Stack Tecnológico

- **Backend:** Java 17+, Spring Boot 3.4.3 (`Web`, `Data JPA`, `Security`, `Validation`, `WebFlux/WebClient`)
- **Base de Datos:** PostgreSQL + Hibernate 6 (`@SQLDelete` y `@SQLRestriction` para Soft Delete)
- **Seguridad:** Spring Security 6 con **JWT Stateless** (`JJWT 0.12.6`) y control de acceso por roles (`ROLE_OWNER`, `ROLE_OPERATOR`)
- **APIs Externas (vía `WebClient`):**
  - **CallMeBot WhatsApp API:** Envío gratuito de alertas automáticas de partos próximos al WhatsApp del propietario.
  - **Open-Meteo API (`v1/forecast`):** Clima actual y pronóstico de 5 días con recomendaciones operativas para campo.
- **Documentación Interactiva:** Swagger UI / OpenAPI 3.0 (`/swagger-ui.html`)

---

## Arquitectura en 10 Capas (`com.ranchcount.api`)

- `controller` — Controladores REST versionados bajo `/api/v1/*`
- `service` / `service.impl` — Interfaces e implementaciones de reglas de negocio
- `repository` — Repositorios Spring Data JPA
- `entity` — Entidades relacionales y enumeradores de dominio
- `dto.request` / `dto.response` — Java `record`s con validaciones estrictas de Jakarta Validation
- `exception` — `@RestControllerAdvice` (`GlobalExceptionHandler`) con JSON estandarizado (`400, 401, 403, 404, 409, 502, 500`)
- `security` — Filtro JWT sin estado, `JwtService` y manejadores JSON de `401`/`403`
- `config` — Configuración de Seguridad, OpenAPI, `WebClient`, Scheduling y semilla inicial
- `client` — Clientes reactivos `WebClient` hacia CallMeBot WhatsApp y Open-Meteo
- `scheduler` — Tarea diaria `@Scheduled` (`PartoAlertScheduler`)

---

## Configuración de Variables de Entorno

Copia `.env` o define las siguientes variables de entorno en tu sistema operativo o configuración de ejecución en IntelliJ IDEA:

```bash
DB_URL=jdbc:postgresql://localhost:5432/ranchcount_db
DB_USERNAME=postgres
DB_PASSWORD=tu_password_postgres

JWT_SECRET=clave_secreta_segura_de_minimo_32_caracteres_para_hmac_sha256
JWT_EXPIRATION_MS=86400000

WHATSAPP_ALERTS_ENABLED=true
WHATSAPP_API_URL=https://api.callmebot.com/whatsapp.php
WHATSAPP_OWNER_PHONE=+5213300000000
WHATSAPP_CALLMEBOT_APIKEY=tu_apikey_callmebot

OPENMETEO_API_URL=https://api.open-meteo.com/v1/forecast
RANCH_DEFAULT_LAT=20.6597
RANCH_DEFAULT_LON=-103.3496

INITIAL_OWNER_ENABLED=true
INITIAL_OWNER_USERNAME=owner
INITIAL_OWNER_PASSWORD=CambiarPassword123!
INITIAL_OWNER_PHONE=+5213300000000
```

### Cómo activar gratis CallMeBot WhatsApp en 1 minuto
1. Agrega el contacto oficial de CallMeBot (`+34 644 51 95 23`, verificar número activo en [callmebot.com](https://www.callmebot.com/blog/free-api-whatsapp-messages/)) en tu teléfono.
2. Envíale por WhatsApp el mensaje exacto: `I allow callmebot to send me messages`.
3. Recibirás tu `apikey` gratuita; colócala en `WHATSAPP_CALLMEBOT_APIKEY` y tu número con lada internacional en `WHATSAPP_OWNER_PHONE`.

---

## Ejecución y Documentación Interactiva

1. Crear la base de datos en PostgreSQL:
   ```sql
   CREATE DATABASE ranchcount_db;
   ```
2. Ejecutar pruebas automatizadas:
   ```bash
   ./mvnw test
   ```
3. Iniciar el servidor:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Abrir Swagger UI:
   - `http://localhost:8080/swagger-ui.html`
   - Especificación JSON: `http://localhost:8080/v3/api-docs`
