<#
.SYNOPSIS
  Script de Inyeccion de Datos y Validacion Automatizada E2E de Endpoints para RanchCount (RanchZ).
  Ejecutar con el backend corriendo en http://localhost:8080.
#>

$baseUrl = "http://localhost:8080/api/v1"
$ErrorActionPreference = "Stop"

function Log-Step($msg) {
    Write-Host "`n>>> [TEST] $msg" -ForegroundColor Cyan
}

function Log-Success($msg) {
    Write-Host " [OK] $msg" -ForegroundColor Green
}

function Log-Fail($msg) {
    Write-Host " [FAIL] $msg" -ForegroundColor Red
}

try {
    # -------------------------------------------------------------
    # 0. Verificacion de conexion
    # -------------------------------------------------------------
    Log-Step "0. Verificando disponibilidad de la API..."
    try {
        $doc = Invoke-RestMethod -Uri "http://localhost:8080/v3/api-docs" -Method Get
        Log-Success "Backend en linea! OpenAPI version: $($doc.openapi)"
    } catch {
        Log-Fail "El servidor no esta encendido en http://localhost:8080. Dale Run/Play en IntelliJ primero."
        exit 1
    }

    # -------------------------------------------------------------
    # 1. Autenticacion (Login del Owner inicial)
    # -------------------------------------------------------------
    Log-Step "1. Autenticando con usuario 'owner'..."
    $loginBody = @{
        username = "owner"
        password = "TU_PASSWORD_AQUI"
    } | ConvertTo-Json

    $authRes = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $authRes.accessToken
    $headers = @{
        Authorization = "Bearer $token"
        "Content-Type" = "application/json"
    }
    Log-Success "Login exitoso. Rol: $($authRes.rol) | Usuario: $($authRes.username)"

    # Validar /auth/me
    $me = Invoke-RestMethod -Uri "$baseUrl/auth/me" -Method Get -Headers $headers
    Log-Success "Perfil verificado: $($me.nombreCompleto) ($($me.rol))"

    # -------------------------------------------------------------
    # 2. Registrar Usuario Operador
    # -------------------------------------------------------------
    Log-Step "2. Registrando un Operador de prueba..."
    $opUsername = "operador_" + (Get-Random -Minimum 100 -Maximum 999)
    $crearOpBody = @{
        nombreCompleto = "Juan Perez Vaquero"
        username = $opUsername
        password = "Operador2026!"
        telefonoWhatsapp = "+529991234567"
        rol = "ROLE_OPERATOR"
    } | ConvertTo-Json

    $opRes = Invoke-RestMethod -Uri "$baseUrl/usuarios" -Method Post -Body $crearOpBody -Headers $headers
    Log-Success "Operador creado con exito: $($opRes.username) con ID $($opRes.id)"

    # -------------------------------------------------------------
    # 3. Inyeccion de Ganado (Bovinos)
    # -------------------------------------------------------------
    Log-Step "3. Inyectando Bovinos (Hembra Vacia, Semental, y Hembra para Monta)..."
    
    $tagSuffix = (Get-Random -Minimum 1000 -Maximum 9999)

    # 3.1 Vaca reproductora
    $vacaBody = @{
        areteCodigo = "MX-RN-$tagSuffix-1"
        nombre = "La Consentida"
        raza = "Brahman"
        sexo = "HEMBRA"
        fechaNacimiento = "2023-01-15"
        pesoKg = 485.50
        estado = "VACIA"
        notas = "Vaca seleccionada para monta"
    } | ConvertTo-Json

    $vacaRes = Invoke-RestMethod -Uri "$baseUrl/bovinos" -Method Post -Body $vacaBody -Headers $headers
    $vacaId = $vacaRes.id
    Log-Success "Vaca creada: $($vacaRes.areteCodigo) - ID: $vacaId ($($vacaRes.estado))"

    # 3.2 Toro Semental
    $toroBody = @{
        areteCodigo = "MX-RN-$tagSuffix-2"
        nombre = "El Patron"
        raza = "Charolais"
        sexo = "MACHO"
        fechaNacimiento = "2021-06-10"
        pesoKg = 890.00
        estado = "SEMENTAL"
        notas = "Semental activo"
    } | ConvertTo-Json

    $toroRes = Invoke-RestMethod -Uri "$baseUrl/bovinos" -Method Post -Body $toroBody -Headers $headers
    Log-Success "Toro creado: $($toroRes.areteCodigo) - ID: $($toroRes.id) ($($toroRes.estado))"

    # -------------------------------------------------------------
    # 4. Probar Regla de Negocio Ganadera: Registro de Monta (+283 dias)
    # -------------------------------------------------------------
    Log-Step "4. Registrando monta para la vaca #$vacaId..."
    $hoy = (Get-Date).ToString("yyyy-MM-dd")
    $montaBody = @{
        fechaMonta = $hoy
        notas = "Monta natural con toro El Patron"
    } | ConvertTo-Json

    $montaRes = Invoke-RestMethod -Uri "$baseUrl/bovinos/$vacaId/monta" -Method Patch -Body $montaBody -Headers $headers
    Log-Success "Monta registrada! Nuevo Estado: $($montaRes.estado) | Fecha Estimada Parto (+283 dias): $($montaRes.fechaEstimadaParto)"

    # Listar proximos partos
    $partos = Invoke-RestMethod -Uri "$baseUrl/bovinos/proximos-partos?dias=300" -Method Get -Headers $headers
    Log-Success "Consulta de proximos partos exitosa. Total en lista: $($partos.Count)"

    # -------------------------------------------------------------
    # 5. Inyeccion de Lotes de Siembra (Modulo Agricola)
    # -------------------------------------------------------------
    Log-Step "5. Inyectando Lote de Siembra..."
    $loteCodigo = "LT-$tagSuffix"
    $fSiembra = (Get-Date).ToString("yyyy-MM-dd")
    $fCosecha = (Get-Date).AddMonths(4).ToString("yyyy-MM-dd")

    $loteBody = @{
        codigoLote = $loteCodigo
        nombre = "Parcela La Herradura"
        tipoCultivo = "Sorgo Forrajero"
        hectareas = 12.50
        inversionTotal = 45000.00
        fechaSiembra = $fSiembra
        fechaEstimadaCosecha = $fCosecha
        estado = "EN_CRECIMIENTO"
        observaciones = "Semilla certificada y riego tecnificado"
    } | ConvertTo-Json

    $loteRes = Invoke-RestMethod -Uri "$baseUrl/lotes" -Method Post -Body $loteBody -Headers $headers
    $loteId = $loteRes.id
    Log-Success "Lote creado: $($loteRes.codigoLote) | Inversion/Ha calculada: $($loteRes.inversionPorHectarea) MXN/ha"

    # -------------------------------------------------------------
    # 6. Inyeccion de Transacciones Financieras (Ingreso y Egreso)
    # -------------------------------------------------------------
    Log-Step "6. Inyectando Transacciones Financieras..."

    # 6.1 Egreso (asociado al lote)
    $egresoBody = @{
        tipo = "EGRESO"
        categoria = "INSUMOS_AGRICOLAS"
        monto = 8500.00
        concepto = "Compra de fertilizante organico para $loteCodigo"
        fechaTransaccion = $hoy
        loteSiembraId = $loteId
    } | ConvertTo-Json

    $egresoRes = Invoke-RestMethod -Uri "$baseUrl/transacciones" -Method Post -Body $egresoBody -Headers $headers
    Log-Success "Egreso registrado ID: $($egresoRes.id) | Monto: $($egresoRes.monto) MXN"

    # 6.2 Ingreso (asociado a venta/leche)
    $ingresoBody = @{
        tipo = "INGRESO"
        categoria = "VENTA_LECHE"
        monto = 22400.00
        concepto = "Venta quincenal de leche bronca"
        fechaTransaccion = $hoy
        bovinoId = $vacaId
    } | ConvertTo-Json

    $ingresoRes = Invoke-RestMethod -Uri "$baseUrl/transacciones" -Method Post -Body $ingresoBody -Headers $headers
    Log-Success "Ingreso registrado ID: $($ingresoRes.id) | Monto: $($ingresoRes.monto) MXN"

    # 6.3 Resumen Financiero
    $resumen = Invoke-RestMethod -Uri "$baseUrl/transacciones/resumen" -Method Get -Headers $headers
    Log-Success "Resumen Financiero consultado:"
    Write-Host "   -> Total Ingresos: $($resumen.totalIngresos) MXN" -ForegroundColor Yellow
    Write-Host "   -> Total Egresos:  $($resumen.totalEgresos) MXN" -ForegroundColor Yellow
    Write-Host "   -> Balance Neto:   $($resumen.balanceNeto) MXN" -ForegroundColor Green

    # -------------------------------------------------------------
    # 7. Clima en tiempo real (Open-Meteo)
    # -------------------------------------------------------------
    Log-Step "7. Consultando Clima Local (Open-Meteo)..."
    $clima = Invoke-RestMethod -Uri "$baseUrl/clima/actual" -Method Get -Headers $headers
    Log-Success "Clima obtenido exitosamente:"
    Write-Host "   -> Temperatura actual: $($clima.temperaturaActualC) C" -ForegroundColor Yellow
    Write-Host "   -> Condicion:          $($clima.condicionActual)" -ForegroundColor Yellow
    Write-Host "   -> Recomendacion:      $($clima.recomendacionOperativa)" -ForegroundColor Cyan

    # -------------------------------------------------------------
    # 8. Verificacion de Alertas de Parto (Telegram Bot API)
    # -------------------------------------------------------------
    Log-Step "8. Verificando Servicio de Alertas (Telegram)..."
    $alertaRes = Invoke-RestMethod -Uri "$baseUrl/alertas/verificar-partos?forzarReenvioHoy=true" -Method Post -Headers $headers
    Log-Success "Revision de alertas ejecutada exitosamente:"
    Write-Host "   -> Alertas habilitadas: $($alertaRes.alertasHabilitadas)" -ForegroundColor Yellow
    Write-Host "   -> Candidatas parto:    $($alertaRes.bovinosEnVentanaParto)" -ForegroundColor Yellow

    Write-Host "`n=======================================================" -ForegroundColor Green
    Write-Host " ¡TODOS LOS ENDPOINTS FUNCIONAN AL 100%!" -ForegroundColor Green
    Write-Host "=======================================================`n" -ForegroundColor Green

} catch {
    Log-Fail "Error en la prueba: $_"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        Write-Host "Detalle del error del servidor: $($reader.ReadToEnd())" -ForegroundColor Red
    }
}
