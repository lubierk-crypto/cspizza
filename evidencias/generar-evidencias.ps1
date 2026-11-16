<#
    Generador de evidencias de prueba de la API REST de cspizza.

    Ejecuta un recorrido CRUD completo sobre los ocho modulos, comprueba las reglas
    de seguridad (sesion, roles, CSRF) y las validaciones, y guarda la transcripcion
    de cada peticion y respuesta en la carpeta de salida.

    Uso:
        .\mvnw.cmd spring-boot:run            (en otra terminal)
        powershell -ExecutionPolicy Bypass -File .\evidencias\generar-evidencias.ps1

    Parametros:
        -BaseUrl  URL base de la aplicacion (por defecto http://localhost:8080)
        -Salida   Carpeta donde se guardan las transcripciones (por defecto .\evidencias\transcripciones)
#>
param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Salida = "$PSScriptRoot\transcripciones"
)

$ErrorActionPreference = "Continue"
New-Item -ItemType Directory -Force -Path $Salida | Out-Null

$sesion = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$lineas = New-Object System.Collections.Generic.List[string]

function Escribir([string]$texto) {
    $lineas.Add($texto)
    Write-Host $texto
}

function Separador([string]$titulo) {
    $guion = "=" * 78
    Escribir ""
    Escribir $guion
    Escribir " $titulo"
    Escribir (" " + (Get-Date -Format "yyyy-MM-dd HH:mm:ss"))
    Escribir $guion
    Escribir ""
}

function Token-Csrf {
    $r = Invoke-WebRequest -Uri "$BaseUrl/login" -WebSession $sesion -UseBasicParsing
    return ([regex]::Match($r.Content, 'name="_csrf"[^>]*value="([^"]+)"')).Groups[1].Value
}

# Las respuestas JSON se envian en UTF-8 sin declarar el charset: hay que leerlas como UTF-8
# para no perder los caracteres acentuados de los mensajes en español.
function Leer-Utf8($flujo) {
    if ($flujo -eq $null) { return "" }
    $memoria = New-Object System.IO.MemoryStream
    $flujo.CopyTo($memoria)
    $flujo.Close()
    return [System.Text.Encoding]::UTF8.GetString($memoria.ToArray())
}

function Peticion {
    param(
        [string]$Metodo,
        [string]$Ruta,
        [string]$Cuerpo = "",
        [switch]$SinCsrf
    )
    $url = "$BaseUrl$Ruta"
    $cabeceras = @{}
    if (-not $SinCsrf) { $cabeceras["X-CSRF-TOKEN"] = $script:token }
    # Invoke-WebRequest guarda las cabeceras en la sesion y las reenvia en las siguientes
    # peticiones: hay que retirarlas para poder probar el caso "sin token CSRF".
    $sesion.Headers.Remove("X-CSRF-TOKEN")

    Escribir "[$Metodo] $Ruta"
    if ($Cuerpo -ne "") { Escribir "    Body: $Cuerpo" }
    if (-not $SinCsrf) { Escribir "    Header: X-CSRF-TOKEN: <token de la sesion>" }
    else { Escribir "    Header: (sin token CSRF)" }

    $params = @{ Uri = $url; Method = $Metodo; WebSession = $sesion; UseBasicParsing = $true; Headers = $cabeceras }
    if ($Cuerpo -ne "") {
        $params["Body"] = $Cuerpo
        $params["ContentType"] = "application/json"
    }
    try {
        $r = Invoke-WebRequest @params
        $estado = $r.StatusCode
        $texto = Leer-Utf8 $r.RawContentStream
    } catch {
        $respuesta = $_.Exception.Response
        if ($respuesta -eq $null) {
            Escribir "    ERROR: $($_.Exception.Message)"
            return @{ Estado = 0; Cuerpo = "" }
        }
        $estado = [int]$respuesta.StatusCode
        $texto = Leer-Utf8 $respuesta.GetResponseStream()
    }
    Escribir "    HTTP $estado"
    if ($texto -ne "") {
        Escribir ("    " + ($texto -replace "`r`n", " "))
    }
    Escribir ""
    return @{ Estado = $estado; Cuerpo = $texto }
}

# --------------------------------------------------------------------------
# Autenticacion
# --------------------------------------------------------------------------
Separador "0. AUTENTICACION Y CSRF"

$token = Token-Csrf
Escribir "[GET] /login  (formulario de inicio de sesion)"
Escribir "    HTTP 200"
Escribir "    El formulario incluye el campo oculto _csrf: $($token.Substring(0, 16))..."
Escribir ""

Invoke-WebRequest -Uri "$BaseUrl/login" -Method Post -WebSession $sesion -UseBasicParsing `
        -Body @{ username = "admin"; password = "admin123"; _csrf = $token } -MaximumRedirection 5 | Out-Null
Escribir "[POST] /login  (username=admin, password=admin123)"
Escribir "    HTTP 302  ->  se emite la cookie de sesion y el usuario queda autenticado"
Escribir ""
$token = Token-Csrf

# --------------------------------------------------------------------------
# Recorrido CRUD por modulo
# --------------------------------------------------------------------------
$modulos = @(
    @{ Nombre = "Categorias"; Ruta = "/api/categorias"; Id = "idCategoria";
        Crear = '{"nombre":"Evidencia Demo","descripcion":"Categoria de prueba","estado":"ACTIVO"}';
        Actualizar = '{"nombre":"Evidencia Demo EDITADA","descripcion":"Actualizada","estado":"ACTIVO"}' },
    @{ Nombre = "Clientes"; Ruta = "/api/clientes"; Id = "idCliente";
        Crear = '{"nombre":"Cliente","apellido":"Evidencia","telefono":"3000000000","correo":"evidencia@example.test","direccion":"Calle 1 #2-3","estado":"ACTIVO"}';
        Actualizar = '{"nombre":"Cliente EDITADO","apellido":"Evidencia","correo":"evidencia@example.test","estado":"ACTIVO"}' },
    @{ Nombre = "Usuarios"; Ruta = "/api/usuarios"; Id = "idUsuario";
        Crear = '{"nombreUsuario":"evidencia_user","nombre":"Usuario","apellido":"Evidencia","rol":"EMPLEADO","correo":"evidencia_usuario@example.test","password":"clave-evidencia"}';
        Actualizar = '{"nombreUsuario":"evidencia_user","nombre":"Usuario EDITADO","apellido":"Evidencia","rol":"EMPLEADO","correo":"evidencia_usuario@example.test","password":"clave-evidencia"}' },
    @{ Nombre = "Productos"; Ruta = "/api/productos"; Id = "idProducto";
        Crear = '{"idCategoria":1,"nombre":"Pizza Evidencia","descripcion":"Producto de prueba","precioUnitario":25000,"stock":10,"estado":"ACTIVO"}';
        Actualizar = '{"idCategoria":1,"nombre":"Pizza Evidencia EDITADA","precioUnitario":27000,"stock":12,"estado":"ACTIVO"}' }
)

$ids = @{}

foreach ($m in $modulos) {
    Separador ("1. CRUD - " + $m.Nombre.ToUpper())

    $r = Peticion -Metodo "GET" -Ruta $m.Ruta
    Escribir ("    Elementos devueltos: " + (($r.Cuerpo | ConvertFrom-Json).Count))
    Escribir ""

    $r = Peticion -Metodo "POST" -Ruta $m.Ruta -Cuerpo $m.Crear
    $creado = $r.Cuerpo | ConvertFrom-Json
    $ids[$m.Id] = $creado.($m.Id)
    Escribir "    Id creado: $($ids[$m.Id])"
    Escribir ""

    $r = Peticion -Metodo "GET" -Ruta "$($m.Ruta)/$($ids[$m.Id])"
    Escribir ""

    $json = ($m.Actualizar -replace '"idCategoria":\d+', ('"idCategoria":' + $ids["idCategoria"]))
    $r = Peticion -Metodo "PUT" -Ruta "$($m.Ruta)/$($ids[$m.Id])" -Cuerpo $json
    Escribir ""

    $r = Peticion -Metodo "DELETE" -Ruta "$($m.Ruta)/$($ids[$m.Id])"
    Escribir ""

    $r = Peticion -Metodo "GET" -Ruta "$($m.Ruta)/$($ids[$m.Id])"
    Escribir "    (404 esperado: el registro fue eliminado)"
    Escribir ""
}

# --------------------------------------------------------------------------
# Modulos con dependencias: pedido, detalle y factura
# --------------------------------------------------------------------------
Separador "2. DATOS DE REFERENCIA (cliente, usuario y producto para el pedido)"

$r = Peticion -Metodo "POST" -Ruta "/api/clientes" -Cuerpo '{"nombre":"Cliente","apellido":"Referencia","correo":"referencia@example.test","estado":"ACTIVO"}'
$clienteRef = ($r.Cuerpo | ConvertFrom-Json).idCliente
Escribir "    Cliente de referencia creado con id $clienteRef"
Escribir ""

$r = Peticion -Metodo "POST" -Ruta "/api/usuarios" -Cuerpo '{"nombreUsuario":"referencia_user","nombre":"Usuario","apellido":"Referencia","rol":"EMPLEADO","correo":"referencia_usuario@example.test","password":"clave-referencia"}'
$usuarioRef = ($r.Cuerpo | ConvertFrom-Json).idUsuario
Escribir "    Usuario de referencia creado con id $usuarioRef"
Escribir ""

$r = Peticion -Metodo "POST" -Ruta "/api/productos" -Cuerpo '{"idCategoria":1,"nombre":"Pizza Referencia","precioUnitario":25000,"stock":10,"estado":"ACTIVO"}'
$productoRef = ($r.Cuerpo | ConvertFrom-Json).idProducto
Escribir "    Producto de referencia creado con id $productoRef (stock 10)"
Escribir ""

Separador "3. CRUD - PEDIDOS (depende de cliente y usuario)"

$r = Peticion -Metodo "GET" -Ruta "/api/pedidos"
Escribir ("    Elementos devueltos: " + (($r.Cuerpo | ConvertFrom-Json).Count))
Escribir ""

$r = Peticion -Metodo "POST" -Ruta "/api/pedidos" -Cuerpo ('{"idCliente":' + $clienteRef + ',"idUsuario":' + $usuarioRef + ',"estado":"PENDIENTE","observacion":"Pedido de evidencia","tipoEntrega":"DOMICILIO","direccionEntrega":"Calle 1 #2-3"}')
$pedidoId = ($r.Cuerpo | ConvertFrom-Json).idPedido
Escribir "    Id creado: $pedidoId"
Escribir ""

$r = Peticion -Metodo "GET" -Ruta "/api/pedidos/$pedidoId"
Escribir ""

$r = Peticion -Metodo "PUT" -Ruta "/api/pedidos/$pedidoId" -Cuerpo ('{"idCliente":' + $clienteRef + ',"idUsuario":' + $usuarioRef + ',"estado":"EN_PREPARACION","observacion":"Pedido actualizado"}')
Escribir ""

Separador "4. CRUD - DETALLES DE PEDIDO (depende de pedido y producto)"

$r = Peticion -Metodo "GET" -Ruta "/api/detalle-pedidos"
Escribir ("    Elementos devueltos: " + (($r.Cuerpo | ConvertFrom-Json).Count))
Escribir ""

$r = Peticion -Metodo "POST" -Ruta "/api/detalle-pedidos" -Cuerpo ('{"idPedido":' + $pedidoId + ',"idProducto":' + $productoRef + ',"cantidad":2,"observacion":"Detalle de evidencia"}')
$detalleId = ($r.Cuerpo | ConvertFrom-Json).idDetallePedido
Escribir "    Id creado: $detalleId"
Escribir ""

$r = Peticion -Metodo "GET" -Ruta "/api/detalle-pedidos/$detalleId"
Escribir ""

$r = Peticion -Metodo "PUT" -Ruta "/api/detalle-pedidos/$detalleId" -Cuerpo ('{"idPedido":' + $pedidoId + ',"idProducto":' + $productoRef + ',"cantidad":3}')
Escribir ""

$r = Peticion -Metodo "GET" -Ruta "/api/pedidos/$pedidoId"
Escribir "    (el total del pedido se recalcula con el detalle: 3 x 25.000 = 75.000)"
Escribir ""

$r = Peticion -Metodo "DELETE" -Ruta "/api/detalle-pedidos/$detalleId"
Escribir ""

Separador "5. CRUD - FACTURAS (depende de pedido)"

$r = Peticion -Metodo "GET" -Ruta "/api/facturas"
Escribir ("    Elementos devueltos: " + (($r.Cuerpo | ConvertFrom-Json).Count))
Escribir ""

$numero = "FAC-EVID-" + (Get-Date -Format "HHmmss")
$r = Peticion -Metodo "POST" -Ruta "/api/facturas" -Cuerpo ('{"idPedido":' + $pedidoId + ',"numeroFactura":"' + $numero + '","iva":1900,"estado":"EMITIDA"}')
$facturaId = ($r.Cuerpo | ConvertFrom-Json).idFactura
Escribir "    Id creado: $facturaId"
Escribir ""

$r = Peticion -Metodo "GET" -Ruta "/api/facturas/$facturaId"
Escribir ""

$r = Peticion -Metodo "PUT" -Ruta "/api/facturas/$facturaId" -Cuerpo ('{"idPedido":' + $pedidoId + ',"numeroFactura":"' + $numero + '","iva":2850,"estado":"PAGADA"}')
Escribir ""

$r = Peticion -Metodo "DELETE" -Ruta "/api/facturas/$facturaId"
Escribir ""

Separador "6. CRUD - INVENTARIOS (depende de producto; el stock es el valor canonico)"

$r = Peticion -Metodo "GET" -Ruta "/api/inventarios"
Escribir ("    Elementos devueltos: " + (($r.Cuerpo | ConvertFrom-Json).Count))
Escribir ""

$r = Peticion -Metodo "POST" -Ruta "/api/productos" -Cuerpo '{"idCategoria":1,"nombre":"Producto para inventario","precioUnitario":9000,"stock":8,"estado":"ACTIVO"}'
$productoInventario = ($r.Cuerpo | ConvertFrom-Json).idProducto
Escribir "    Producto de apoyo creado con id $productoInventario (stock 8)"
Escribir ""

$inventarios = (Invoke-WebRequest -Uri "$BaseUrl/api/inventarios" -WebSession $sesion -UseBasicParsing).Content | ConvertFrom-Json
$auto = $inventarios | Where-Object { $_.idProducto -eq $productoInventario } | Select-Object -First 1
$r = Peticion -Metodo "POST" -Ruta "/api/inventarios" -Cuerpo ('{"idProducto":' + $productoInventario + ',"cantidadMinima":1,"cantidadMaxima":20}')
Escribir "    (409 esperado: el producto ya tiene inventario automatico)"
Escribir ""

$r = Peticion -Metodo "DELETE" -Ruta "/api/inventarios/$($auto.idInventario)"
Escribir ""

$r = Peticion -Metodo "POST" -Ruta "/api/inventarios" -Cuerpo ('{"idProducto":' + $productoInventario + ',"cantidadMinima":1,"cantidadMaxima":20,"ubicacion":"BODEGA-A"}')
Escribir "    (201 esperado: cantidadActual se inicializa con el stock del producto, que es 8)"
Escribir ""

$r = Peticion -Metodo "GET" -Ruta "/api/inventarios"
Escribir ""

# --------------------------------------------------------------------------
# Validaciones
# --------------------------------------------------------------------------
Separador "7. VALIDACION DE DATOS (400) Y CONFLICTOS (409)"

$r = Peticion -Metodo "POST" -Ruta "/api/categorias" -Cuerpo '{"nombre":""}'
Escribir "    (400 esperado: falta el nombre)"
Escribir ""

$r = Peticion -Metodo "PUT" -Ruta "/api/categorias/99999999" -Cuerpo '{"nombre":"No existe"}'
Escribir "    (404 esperado: el registro no existe)"
Escribir ""

$r = Peticion -Metodo "POST" -Ruta "/api/inventarios" -Cuerpo ('{"idProducto":' + $productoInventario + ',"cantidadMinima":1,"cantidadMaxima":20}')
Escribir "    (409 esperado: inventario duplicado para el mismo producto)"
Escribir ""

$r = Peticion -Metodo "GET" -Ruta "/api/usuarios"
Escribir "    (la respuesta no incluye el campo password del usuario)"
Escribir ""

# --------------------------------------------------------------------------
# Seguridad
# --------------------------------------------------------------------------
Separador "8. SEGURIDAD (CSRF, sesion y roles)"

$r = Peticion -Metodo "POST" -Ruta "/api/categorias" -Cuerpo '{"nombre":"Sin token CSRF"}' -SinCsrf
Escribir "    (403 esperado: falta el token X-CSRF-TOKEN)"
Escribir ""

$otra = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
    $r = Invoke-WebRequest -Uri "$BaseUrl/api/categorias" -WebSession $otra -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
    Escribir "[GET] /api/categorias sin sesion"
    Escribir "    HTTP $($r.StatusCode)"
} catch {
    Escribir "[GET] /api/categorias sin sesion"
    Escribir "    HTTP $([int]$_.Exception.Response.StatusCode)  (redireccion al login: la API exige autenticacion)"
}
Escribir ""

$sesionGerente = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$tokenGerente = ([regex]::Match((Invoke-WebRequest -Uri "$BaseUrl/login" -WebSession $sesionGerente -UseBasicParsing).Content, 'name="_csrf"[^>]*value="([^"]+)"')).Groups[1].Value
Invoke-WebRequest -Uri "$BaseUrl/login" -Method Post -WebSession $sesionGerente -UseBasicParsing `
        -Body @{ username = "gerente"; password = "gerente123"; _csrf = $tokenGerente } -MaximumRedirection 5 | Out-Null
Escribir "[GET] /clientes con sesion de GERENTE (el modulo es de ADMIN y EMPLEADO)"
try {
    $r = Invoke-WebRequest -Uri "$BaseUrl/clientes" -WebSession $sesionGerente -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
    Escribir "    HTTP $($r.StatusCode)"
} catch {
    Escribir "    HTTP $([int]$_.Exception.Response.StatusCode)  (403 esperado: el rol no tiene permiso)"
}
Escribir ""

# --------------------------------------------------------------------------
# Limpieza de los registros de prueba
# --------------------------------------------------------------------------
Separador "9. LIMPIEZA DE LOS REGISTROS DE PRUEBA"

foreach ($par in @(@("facturas", $facturaId), @("pedidos", $pedidoId),
        @("detalle-pedidos", $detalleId), @("inventarios", $auto.idInventario), @("productos", $productoInventario),
        @("productos", $productoRef), @("clientes", $clienteRef), @("usuarios", $usuarioRef))) {
    $ruta = "/api/$($par[0])"
    if ($par[1] -ne $null) {
        $r = Peticion -Metodo "DELETE" -Ruta "$ruta/$($par[1])"
    }
}

$nombreArchivo = Join-Path $Salida ("transcripcion-" + (Get-Date -Format "yyyyMMdd-HHmmss") + ".txt")
$lineas | Out-File -FilePath $nombreArchivo -Encoding utf8
Escribir ""
Escribir "Transcripcion guardada en: $nombreArchivo"
