package com.ranchcount.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RanchCountIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String loginAndGetBearer(String username, String password) throws Exception {
        String payload = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return "Bearer " + json.get("accessToken").asText();
    }

    @Test
    @DisplayName("1. Seguridad JWT, RBAC (OWNER vs OPERATOR), Regla 283 dias y Reporte de Parto")
    void flujoCompletoGanaderoYPermisosRbac() throws Exception {
        // Sin token debe responder 401 estructurado
        mockMvc.perform(get("/api/v1/bovinos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/bovinos"));

        // Login como OWNER inicial
        String ownerBearer = loginAndGetBearer("owner_test", "OwnerTestPassword123!");

        // OWNER registra a un ROLE_OPERATOR
        String crearOperadorJson = """
                {
                  "nombreCompleto": "Carlos Operador",
                  "username": "carlos_op",
                  "password": "OperadorPassword123!",
                  "telefonoWhatsapp": "+5213311223344",
                  "rol": "ROLE_OPERATOR"
                }
                """;
        mockMvc.perform(post("/api/v1/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearOperadorJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("ROLE_OPERATOR"));

        // Login como OPERATOR
        String operatorBearer = loginAndGetBearer("carlos_op", "OperadorPassword123!");

        // OPERATOR NO puede consultar usuarios ni crear transacciones ni crear lotes (403 Forbidden)
        mockMvc.perform(get("/api/v1/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(post("/api/v1/transacciones")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "EGRESO",
                                  "categoria": "ALIMENTO_GANADO",
                                  "monto": 1200.00,
                                  "concepto": "Pacas de alfalfa",
                                  "fechaTransaccion": "2026-11-01"
                                }
                                """))
                .andExpect(status().isForbidden());

        // OPERATOR SI puede registrar un bovino
        String crearVacaJson = """
                {
                  "areteCodigo": "mx-001",
                  "nombre": "Estrella",
                  "raza": "Brahman",
                  "sexo": "HEMBRA",
                  "fechaNacimiento": "2023-01-10",
                  "pesoKg": 420.50,
                  "estado": "VACIA",
                  "notas": "Ingreso sano"
                }
                """;
        MvcResult vacaResult = mockMvc.perform(post("/api/v1/bovinos")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearVacaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.areteCodigo").value("MX-001"))
                .andExpect(jsonPath("$.estado").value("VACIA"))
                .andReturn();

        long vacaId = objectMapper.readTree(vacaResult.getResponse().getContentAsString()).get("id").asLong();

        // OPERATOR SI puede registrar monta -> pasa a CARGADA y calcula exactamente +283 dias
        LocalDate fechaMonta = LocalDate.now().minusDays(280);
        LocalDate fechaEsperadaParto = fechaMonta.plusDays(283);

        mockMvc.perform(patch("/api/v1/bovinos/" + vacaId + "/monta")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fechaMonta": "%s",
                                  "notas": "Monta registrada en campo"
                                }
                                """.formatted(fechaMonta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CARGADA"))
                .andExpect(jsonPath("$.fechaMonta").value(fechaMonta.toString()))
                .andExpect(jsonPath("$.fechaEstimadaParto").value(fechaEsperadaParto.toString()));

        // OPERATOR NO puede editar con PUT ni eliminar con DELETE un bovino (403 Forbidden)
        mockMvc.perform(put("/api/v1/bovinos/" + vacaId)
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearVacaJson))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/bovinos/" + vacaId)
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer))
                .andExpect(status().isForbidden());

        // OPERATOR SI puede reportar cuando la vaca ya pario (PATCH /{id}/parto) y dar de alta opcionalmente a la cria
        LocalDate fechaParto = LocalDate.now();
        mockMvc.perform(patch("/api/v1/bovinos/" + vacaId + "/parto")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fechaParto": "%s",
                                  "partoExitoso": true,
                                  "areteCria": "MX-CRIA-001",
                                  "sexoCria": "MACHO",
                                  "nombreCria": "Lucero",
                                  "pesoCriaKg": 35.00,
                                  "notas": "Parto sin complicaciones"
                                }
                                """.formatted(fechaParto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("LACTANDO"))
                .andExpect(jsonPath("$.fechaUltimoParto").value(fechaParto.toString()))
                .andExpect(jsonPath("$.fechaEstimadaParto").doesNotExist());

        // Verificar lectura de bovinos incluye a la cria recien nacida
        mockMvc.perform(get("/api/v1/bovinos")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].areteCodigo").value("MX-CRIA-001"));
    }

    @Test
    @DisplayName("2. Transacciones con BigDecimal y Borrado Logico (@SQLDelete y @SQLRestriction)")
    void transaccionesBigDecimalYSoftDelete() throws Exception {
        String ownerBearer = loginAndGetBearer("owner_test", "OwnerTestPassword123!");

        // Registrar INGRESO
        MvcResult ingresoRes = mockMvc.perform(post("/api/v1/transacciones")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "INGRESO",
                                  "categoria": "VENTA_COSECHA",
                                  "monto": 50000.75,
                                  "concepto": "Venta cosecha maiz lote sur",
                                  "fechaTransaccion": "2026-10-20"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        // Registrar EGRESO
        MvcResult egresoRes = mockMvc.perform(post("/api/v1/transacciones")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "EGRESO",
                                  "categoria": "VETERINARIA",
                                  "monto": 10000.25,
                                  "concepto": "Vacunas y desparasitantes",
                                  "fechaTransaccion": "2026-10-22"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long egresoId = objectMapper.readTree(egresoRes.getResponse().getContentAsString()).get("id").asLong();
        long ingresoId = objectMapper.readTree(ingresoRes.getResponse().getContentAsString()).get("id").asLong();

        // Verificar resumen antes del borrado logico (50000.75 - 10000.25 = 40000.50)
        mockMvc.perform(get("/api/v1/transacciones/resumen")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer)
                        .param("desde", "2026-10-01")
                        .param("hasta", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIngresos").value(50000.75))
                .andExpect(jsonPath("$.totalEgresos").value(10000.25))
                .andExpect(jsonPath("$.balanceNeto").value(40000.50));

        // Aplicar DELETE -> dispara @SQLDelete (borrado logico)
        mockMvc.perform(delete("/api/v1/transacciones/" + egresoId)
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer))
                .andExpect(status().isNoContent());

        // @SQLRestriction oculta el registro en consultas JPA (retorna 404)
        mockMvc.perform(get("/api/v1/transacciones/" + egresoId)
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // Pero fisicamente SIGUE en la tabla con eliminado_at != NULL (auditoria intacta)
        Timestamp eliminadoAt = jdbcTemplate.queryForObject(
                "SELECT eliminado_at FROM transacciones WHERE id = ?",
                Timestamp.class,
                egresoId
        );
        assertThat(eliminadoAt).isNotNull();

        // Limpiar el ingreso de prueba tambien para no afectar otros tests
        mockMvc.perform(delete("/api/v1/transacciones/" + ingresoId)
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("3. Validaciones Jakarta (@Valid 400), Lotes de Siembra y OpenAPI 3.0")
    void validacionesJakartaLotesYOpenApi() throws Exception {
        String ownerBearer = loginAndGetBearer("owner_test", "OwnerTestPassword123!");

        // Validar error 400 estructurado ante datos invalidos en Lotes de Siembra
        mockMvc.perform(post("/api/v1/lotes")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoLote": "",
                                  "nombre": "Parcela Norte",
                                  "tipoCultivo": "Sorgo",
                                  "hectareas": -5.0,
                                  "inversionTotal": 15000.00,
                                  "fechaSiembra": "2026-06-01",
                                  "fechaEstimadaCosecha": "2026-10-15",
                                  "estado": "SEMBRADO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.codigoLote").exists())
                .andExpect(jsonPath("$.validationErrors.hectareas").exists());

        // Crear lote valido y verificar calculo de inversionPorHectarea
        mockMvc.perform(post("/api/v1/lotes")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoLote": "LOTE-SUR-01",
                                  "nombre": "Parcela La Loma",
                                  "tipoCultivo": "Avena Forrajera",
                                  "hectareas": 10.00,
                                  "inversionTotal": 45000.00,
                                  "fechaSiembra": "2026-06-01",
                                  "fechaEstimadaCosecha": "2026-12-01",
                                  "estado": "EN_CRECIMIENTO"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inversionPorHectarea").value(4500.00));

        // Verificar que la especificacion OpenAPI 3.0 / Swagger responde publicamente
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("RanchCount (RanchZ) RESTful API"));
    }

    @Test
    @DisplayName("4. Coordenadas GPS en Lotes, Endpoints por Sexo (/hembras y /machos) y Monta Natural Aproximada")
    void coordenadasLotesYEndpointsPorSexo() throws Exception {
        String ownerBearer = loginAndGetBearer("owner_test", "OwnerTestPassword123!");

        // 1) Coordenadas validas en LoteSiembra
        mockMvc.perform(post("/api/v1/lotes")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoLote": "LOTE-GPS-01",
                                  "nombre": "Parcela Georreferenciada",
                                  "tipoCultivo": "Maiz",
                                  "hectareas": 8.00,
                                  "latitud": 20.659700,
                                  "longitud": -103.349600,
                                  "inversionTotal": 32000.00,
                                  "fechaSiembra": "2026-06-01",
                                  "fechaEstimadaCosecha": "2026-12-01",
                                  "estado": "SEMBRADO"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.latitud").value(20.659700))
                .andExpect(jsonPath("$.longitud").value(-103.349600));

        // Latitud fuera de rango (-90 a 90) -> 400
        mockMvc.perform(post("/api/v1/lotes")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoLote": "LOTE-GPS-ERR1",
                                  "nombre": "Parcela Invalida",
                                  "tipoCultivo": "Maiz",
                                  "hectareas": 5.00,
                                  "latitud": 95.0,
                                  "longitud": -103.349600,
                                  "inversionTotal": 10000.00,
                                  "fechaSiembra": "2026-06-01",
                                  "fechaEstimadaCosecha": "2026-12-01",
                                  "estado": "SEMBRADO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.latitud").exists());

        // Latitud sin longitud -> 400 BusinessRuleException
        mockMvc.perform(post("/api/v1/lotes")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigoLote": "LOTE-GPS-ERR2",
                                  "nombre": "Parcela Incompleta",
                                  "tipoCultivo": "Maiz",
                                  "hectareas": 5.00,
                                  "latitud": 20.659700,
                                  "inversionTotal": 10000.00,
                                  "fechaSiembra": "2026-06-01",
                                  "fechaEstimadaCosecha": "2026-12-01",
                                  "estado": "SEMBRADO"
                                }
                                """))
                .andExpect(status().isBadRequest());

        // 2) Crear operador para probar permisos en nuevos endpoints
        mockMvc.perform(post("/api/v1/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreCompleto": "Luis Vaquero",
                                  "username": "luis_op_sexos",
                                  "password": "OperadorPassword123!",
                                  "rol": "ROLE_OPERATOR"
                                }
                                """))
                .andExpect(status().isCreated());
        String operatorBearer = loginAndGetBearer("luis_op_sexos", "OperadorPassword123!");

        // Registrar hembra en POST /api/v1/bovinos/hembras
        MvcResult hembraRes = mockMvc.perform(post("/api/v1/bovinos/hembras")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "areteCodigo": "MX-HEM-101",
                                  "nombre": "Bonita",
                                  "raza": "Angus",
                                  "fechaNacimiento": "2023-01-01",
                                  "pesoKg": 440.00,
                                  "estado": "VACIA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sexo").value("HEMBRA"))
                .andReturn();
        long hembraId = objectMapper.readTree(hembraRes.getResponse().getContentAsString()).get("id").asLong();

        // Registrar macho en POST /api/v1/bovinos/machos
        mockMvc.perform(post("/api/v1/bovinos/machos")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "areteCodigo": "MX-MAC-201",
                                  "nombre": "Titan",
                                  "raza": "Charolais",
                                  "fechaNacimiento": "2022-01-01",
                                  "pesoKg": 810.00,
                                  "estado": "SEMENTAL"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sexo").value("MACHO"));

        // Rechazar macho con estado femenino CARGADA -> 400
        mockMvc.perform(post("/api/v1/bovinos/machos")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "areteCodigo": "MX-MAC-ERR",
                                  "nombre": "Invalido",
                                  "raza": "Charolais",
                                  "estado": "CARGADA"
                                }
                                """))
                .andExpect(status().isBadRequest());

        // GET /hembras solo devuelve HEMBRA y GET /machos solo devuelve MACHO
        MvcResult hembrasList = mockMvc.perform(get("/api/v1/bovinos/hembras")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode hembrasJson = objectMapper.readTree(hembrasList.getResponse().getContentAsString());
        assertThat(hembrasJson).isNotEmpty();
        hembrasJson.forEach(item -> assertThat(item.get("sexo").asText()).isEqualTo("HEMBRA"));

        MvcResult machosList = mockMvc.perform(get("/api/v1/bovinos/machos")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode machosJson = objectMapper.readTree(machosList.getResponse().getContentAsString());
        assertThat(machosJson).isNotEmpty();
        machosJson.forEach(item -> assertThat(item.get("sexo").asText()).isEqualTo("MACHO"));

        // GET /api/v1/bovinos incluye tanto MX-HEM-101 como MX-MAC-201
        String todosBody = mockMvc.perform(get("/api/v1/bovinos")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(todosBody).contains("MX-HEM-101", "MX-MAC-201");

        // 3) Monta natural no observada por diasGestacionAproximados
        LocalDate hoy = LocalDate.now();
        LocalDate fechaMontaRetro = hoy.minusDays(90);
        LocalDate fechaPartoEsperada = fechaMontaRetro.plusDays(283);

        mockMvc.perform(patch("/api/v1/bovinos/" + hembraId + "/monta")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "diasGestacionAproximados": 90,
                                  "notas": "Monta natural detectada por palpacion (~3 meses)"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CARGADA"))
                .andExpect(jsonPath("$.fechaMonta").value(fechaMontaRetro.toString()))
                .andExpect(jsonPath("$.fechaEstimadaParto").value(fechaPartoEsperada.toString()));

        // Si no se envia ni fechaMonta ni diasGestacionAproximados -> 400
        mockMvc.perform(patch("/api/v1/bovinos/" + hembraId + "/monta")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "notas": "Sin fecha ni dias"
                                }
                                """))
                .andExpect(status().isBadRequest());

        // Si se envian ambos -> 400
        mockMvc.perform(patch("/api/v1/bovinos/" + hembraId + "/monta")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fechaMonta": "%s",
                                  "diasGestacionAproximados": 90
                                }
                                """.formatted(hoy)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("5. Perfil del Rancho (GET y PUT /api/v1/rancho) con persistencia y RBAC")
    void perfilDelRanchoPersistenciaYPermisos() throws Exception {
        String ownerBearer = loginAndGetBearer("owner_test", "OwnerTestPassword123!");

        // Configuracion inicial por defecto creada automaticamente en GET
        mockMvc.perform(get("/api/v1/rancho")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreRancho").value("Mi Rancho"))
                .andExpect(jsonPath("$.proposito").value("Doble propósito"));

        // Actualizacion completa por ROLE_OWNER -> 200 OK
        mockMvc.perform(put("/api/v1/rancho")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreRancho": "Los Canelos",
                                  "proposito": "Doble proposito",
                                  "hectareasTotales": 500.00,
                                  "ubicacionTexto": "Ciudad del Carmen, Campeche"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreRancho").value("Los Canelos"))
                .andExpect(jsonPath("$.hectareasTotales").value(500.00))
                .andExpect(jsonPath("$.ubicacionTexto").value("Ciudad del Carmen, Campeche"));

        // Validacion Jakarta (@NotBlank y @Positive) -> 400 Bad Request
        mockMvc.perform(put("/api/v1/rancho")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreRancho": "",
                                  "hectareasTotales": -10.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.nombreRancho").exists())
                .andExpect(jsonPath("$.validationErrors.hectareasTotales").exists());

        // Registrar operador para verificar lectura permitida (200) y escritura denegada (403)
        mockMvc.perform(post("/api/v1/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, ownerBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreCompleto": "Operador Rancho",
                                  "username": "op_rancho_test",
                                  "password": "OperadorPassword123!",
                                  "rol": "ROLE_OPERATOR"
                                }
                                """))
                .andExpect(status().isCreated());
        String operatorBearer = loginAndGetBearer("op_rancho_test", "OperadorPassword123!");

        mockMvc.perform(get("/api/v1/rancho")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreRancho").value("Los Canelos"))
                .andExpect(jsonPath("$.hectareasTotales").value(500.00));

        mockMvc.perform(put("/api/v1/rancho")
                        .header(HttpHeaders.AUTHORIZATION, operatorBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreRancho": "Intento Operador"
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
