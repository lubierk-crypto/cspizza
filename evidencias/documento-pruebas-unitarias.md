# INFORME TÉCNICO — PRUEBAS UNITARIAS DEL PROTOTIPO CSPizza

---

## PORTADA

**Título del proyecto:** cspizza — Sistema de gestión para pizzería

**Asignatura:** Desarrollo de Software / Pruebas de Software

**Integrantes del equipo:**
- [Nombre completo del integrante 1]
- [Nombre completo del integrante 2]
- [Nombre completo del integrante 3]

**Fecha de entrega:** 16/11/2026

**Repositorio GitHub:** https://github.com/lubierk-crypto/cspizza

---

## 1. INTRODUCCIÓN

El presente documento describe las pruebas unitarias implementadas sobre el prototipo funcional **cspizza**, desarrollado en el primer parcial de la asignatura. El prototipo es una aplicación web de gestión para una pizzería construida con **Spring Boot 4.1.2**, **Java 17**, **MySQL** y **Thymeleaf**, que incluye módulos de categorías, clientes, productos, usuarios, pedidos, detalles de pedido, inventario y facturas, con autenticación por roles (ADMIN, GERENTE, EMPLEADO) y una interfaz POS para registrar pedidos completos.

Las pruebas se enfocan en validar la **lógica de negocio** contenida en las clases de servicio, identificando posibles errores o inconsistencias en el desarrollo. Se utilizan herramientas del ecosistema Java, especialmente **JUnit 5** y **Mockito**, integradas con **Spring Boot**, para verificar el correcto funcionamiento de los componentes principales de la aplicación.

---

## 2. OBJETIVO DE LAS PRUEBAS

Implementar y ejecutar pruebas unitarias que permitan:

- Verificar el correcto funcionamiento de los componentes del prototipo (servicios, lógica de negocio y validaciones).
- Aplicar buenas prácticas de desarrollo (aislamiento de dependencias mediante mocks, pruebas de éxito y error).
- Documentar los resultados obtenidos para evaluar la calidad del software.
- Identificar errores o inconsistencias y corregirlos antes de la entrega final.

---

## 3. DESCRIPCIÓN DE LAS HERRAMIENTAS UTILIZADAS

| Herramienta | Versión | Propósito |
|---|---|---|
| **JUnit 5 (Jupiter)** | 5.x | Framework de pruebas unitarias para Java. Proporciona anotaciones (`@Test`, `@BeforeEach`, `@DisplayName`) y aserciones (`assertEquals`, `assertThrows`, `assertTrue`, etc.). |
| **Mockito** | 5.x | Biblioteca para crear mocks (simulaciones) de dependencias, permitiendo pruebas aisladas sin base de datos real. |
| **Spring Boot Test** | 4.1.2 | Integración de JUnit con Spring Boot (`@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles`). |
| **Spring Security Test** | 7.x | Utilidades para probar seguridad (`@WithMockUser`, `SecurityMockMvcRequestPostProcessors`). |
| **H2 Database** | 2.x | Base de datos en memoria para pruebas de integración (perfil `test`). |
| **Maven** | 3.9.x | Gestor de dependencias y ejecución de pruebas (`mvn test`). |
| **MockMvc** | — | Simulación de peticiones HTTP para pruebas de controladores REST y MVC. |

Las dependencias se configuran en `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 4. COMPONENTES Y MÉTODOS EVALUADOS

Se evaluaron los siguientes componentes del sistema:

### Servicios (lógica de negocio)

| Servicio | Métodos evaluados |
|---|---|
| `CategoriaService` | `guardar`, `actualizar`, `eliminar`, `buscarPorId`, `buscarPorNombre`, `buscarPorEstado` |
| `ClienteService` | `guardar`, `actualizar`, `eliminar`, `buscarPorId`, `buscarPorCorreo`, `buscarPorTelefono` |
| `ProductoService` | `guardar`, `actualizar`, `eliminar`, `buscarPorId`, `buscarPorCategoria`, `buscarPorEstado`, `buscarPorNombreContiene`, `buscarProductosDisponibles` |
| `UsuarioService` | `guardar`, `actualizar`, `eliminar`, `buscarPorId`, `buscarPorNombreUsuario`, `buscarPorCorreo`, `buscarPorRol`, `buscarPorEstado` |
| `InventarioService` | `guardar`, `actualizar`, `eliminar`, `buscarPorId`, `buscarPorProducto`, `buscarPorEstado`, `buscarPorUbicacion`, `buscarBajoStock` |
| `FacturaService` | `guardar`, `actualizar`, `eliminar`, `buscarPorId`, `generarDesdePedido`, `buscarPorNumeroFactura`, `buscarPorPedido`, `buscarPorEstado` |
| `DetallePedidoService` | `guardar`, `actualizar`, `eliminar`, `buscarPorId`, `buscarPorPedido`, `buscarPorProducto` |
| `PedidoService` | `crearConDetalles`, `cambiarEstado`, `guardar`, `actualizar`, `eliminar`, `buscarPorId`, `listarTodos`, `listarConFiltros` |

### Controladores REST

| Controlador | Endpoints evaluados |
|---|---|
| `CategoriaRestController` | GET/POST/PUT/DELETE `/api/categorias` |
| `ClienteRestController` | GET/POST/PUT/DELETE `/api/clientes` |
| `ProductoRestController` | GET/POST/PUT/DELETE `/api/productos` |
| `UsuarioRestController` | GET/POST/PUT/DELETE `/api/usuarios` |
| `PedidoRestController` | GET/POST/PUT/DELETE `/api/pedidos`, POST `/api/pedidos/completo` |
| `FacturaRestController` | GET/POST/PUT/DELETE `/api/facturas`, POST `/api/facturas/desde-pedido/{id}` |
| `DetallePedidoRestController` | GET/POST/PUT/DELETE `/api/detalle-pedidos` |
| `InventarioRestController` | GET/POST/PUT/DELETE `/api/inventarios` |

### Controladores MVC

| Controlador | Vistas evaluadas |
|---|---|
| `AuthMvcController` | Login, Dashboard (admin/gerencia/empleado), Access Denied |
| `PedidoMvcController` | Lista, Formulario, Detalle, Nuevo (POS), Cocina |
| `CategoriaMvcController`, `ClienteMvcController`, `ProductoMvcController`, `UsuarioMvcController`, `FacturaMvcController`, `InventarioMvcController`, `DetallePedidoMvcController` | CRUD de cada módulo |

### Configuración de seguridad

| Componente | Aspectos evaluados |
|---|---|
| `SecurityConfig` | Autenticación por roles, CSRF (403 JSON para API, HTML para web), codificación UTF-8, PasswordEncoder |
| `UsuarioDetailsService` | Carga de usuarios desde BD, validación de contraseñas |

---

## 5. CASOS DE PRUEBA IMPLEMENTADOS

### 5.1 Pruebas de servicios (Mockito — aisladas)

Se implementaron **53 pruebas unitarias nuevas** con Mockito, siguiendo el patrón de `PedidoServicePosTest` (ya existente con 10 casos). Cada prueba simula las dependencias (repositorios) mediante `@Mock` y el servicio bajo prueba mediante `@InjectMocks`.

#### CategoriaServiceTest (7 casos)
1. **guardarCategoriaValida** — Guardar categoría con nombre válido → retorna categoría con id asignado.
2. **guardarCategoriaSinNombre** — Guardar con nombre vacío → lanza `IllegalArgumentException`.
3. **guardarCategoriaNula** — Guardar null → lanza `IllegalArgumentException`.
4. **buscarPorIdExistente** — Buscar id existente → retorna `Optional` con la categoría.
5. **buscarPorIdInexistente** — Buscar id no existente → retorna `Optional` vacío.
6. **eliminarCategoriaInexistente** — Eliminar id no existente → lanza `NotFoundException`.
7. **actualizarCategoriaValida** — Actualizar con datos válidos → campos actualizados.

#### ClienteServiceTest (8 casos)
1. **guardarClienteValido** — Guardar cliente con nombre y apellido → retorna cliente con id.
2. **guardarClienteSinNombre** — Sin nombre → `IllegalArgumentException`.
3. **guardarClienteSinApellido** — Sin apellido → `IllegalArgumentException`.
4. **guardarClienteNulo** — Nulo → `IllegalArgumentException`.
5. **buscarPorIdExistente** — Id existente → `Optional` con cliente.
6. **buscarPorIdInexistente** — Id no existente → `Optional` vacío.
7. **eliminarClienteInexistente** — Eliminar inexistente → `NotFoundException`.
8. **actualizarClienteValida** — Actualizar válido → campos actualizados.

#### ProductoServiceTest (8 casos)
1. **guardarProductoValido** — Guardar producto con categoría válida → retorna producto con id.
2. **guardarProductoSinNombre** — Sin nombre → `IllegalArgumentException`.
3. **guardarProductoSinCategoria** — Sin categoría → `IllegalArgumentException`.
4. **guardarProductoNulo** — Nulo → `IllegalArgumentException`.
5. **buscarPorIdExistente** — Id existente → `Optional` con producto.
6. **buscarPorIdInexistente** — Id no existente → `Optional` vacío.
7. **eliminarProductoInexistente** — Eliminar inexistente → `NotFoundException`.
8. **actualizarProductoValida** — Actualizar válido → campos actualizados.

#### UsuarioServiceTest (8 casos)
1. **guardarUsuarioValido** — Guardar usuario válido → password codificado con BCrypt, retorna usuario.
2. **guardarUsuarioSinNombreUsuario** — Sin username → `IllegalArgumentException`.
3. **guardarUsuarioConRolInvalido** — Rol no válido → `IllegalArgumentException`.
4. **guardarUsuarioUsernameDuplicado** — Username duplicado → `ConflictException`.
5. **guardarUsuarioSinPassword** — Sin password → `IllegalArgumentException`.
6. **guardarUsuarioNulo** — Nulo → `IllegalArgumentException`.
7. **buscarPorIdInexistente** — Id no existente → `Optional` vacío.
8. **eliminarUsuarioInexistente** — Eliminar inexistente → `NotFoundException`.

#### InventarioServiceTest (8 casos)
1. **guardarInventarioValido** — Guardar inventario → `cantidadActual` inicializada con stock del producto.
2. **guardarInventarioSinProducto** — Sin producto → `IllegalArgumentException`.
3. **guardarInventarioCantidadNegativa** — Cantidad negativa → `IllegalArgumentException`.
4. **guardarInventarioMaxMenorQueMin** — Max < Min → `IllegalArgumentException`.
5. **guardarInventarioProductoInexistente** — Producto inexistente → `NotFoundException`.
6. **guardarInventarioDuplicado** — Inventario duplicado → `ConflictException`.
7. **buscarPorIdInexistente** — Id no existente → `Optional` vacío.
8. **eliminarInventarioInexistente** — Eliminar inexistente → `NotFoundException`.

#### FacturaServiceTest (6 casos)
1. **generarDesdePedidoValido** — Generar factura desde pedido → EMITIDA con subtotal/IVA/total calculados (IVA 19%).
2. **generarDesdePedidoInexistente** — Pedido inexistente → `NotFoundException`.
3. **generarDesdePedidoYaFacturado** — Pedido ya facturado → `ConflictException`.
4. **generarDesdePedidoConTotalNulo** — Total nulo → usa 0.
5. **generarDesdePedidoNumeroConsecutivo** — Número consecutivo FAC-000006.
6. **buscarPorIdInexistente** — Id no existente → `Optional` vacío.

#### DetallePedidoServiceTest (8 casos)
1. **guardarDetalleValido** — Guardar detalle → precio y subtotal calculados con precio de BD.
2. **guardarDetalleSinPedido** — Sin pedido → `IllegalArgumentException`.
3. **guardarDetalleSinProducto** — Sin producto → `IllegalArgumentException`.
4. **guardarDetalleCantidadCero** — Cantidad 0 → `IllegalArgumentException`.
5. **guardarDetallePedidoInexistente** — Pedido inexistente → `NotFoundException`.
6. **guardarDetalleProductoInexistente** — Producto inexistente → `NotFoundException`.
7. **eliminarDetalleInexistente** — Eliminar inexistente → `NotFoundException`.
8. **buscarPorIdInexistente** — Id no existente → `Optional` vacío.

#### PedidoServicePosTest (10 casos) — ya existente
1. **crearConDetallesCalculaTotalCorrectamente** — 1 Pizza ($30000) + 2 Gaseosas ($4500×2) = $39000.
2. **crearConDetallesTomaPrecioDeLaBaseDeDatosNoDelCuerpo** — Precio de BD, no del cuerpo.
3. **crearConDetallesRechazaSinItems** — Sin items → `IllegalArgumentException`.
4. **crearConDetallesRechazaProductoInexistente** — Producto inexistente → `NotFoundException`.
5. **crearConDetallesRechazaProductoInactivo** — Producto inactivo → `ConflictException`.
6. **crearConDetallesRechazaStockInsuficiente** — Stock insuficiente → `ConflictException`.
7. **crearConDetallesUsaClienteMostradorPorDefecto** — Sin idCliente → "Cliente Mostrador".
8. **cambiarEstadoPendienteAAEnPreparacion** — Transición válida PENDIENTE → EN_PREPARACION.
9. **cambiarEstadoRechazaTransicionInvalida** — Transición inválida → `ConflictException`.
10. **cambiarEstadoCancelarReponeStock** — Cancelar repone stock (+3).

### 5.2 Pruebas de integración (H2 — base de datos real)

#### ServicePersistenceTests (4 casos)
1. Verificación de persistencia de categorías.
2. Verificación de persistencia de productos.
3. Verificación de persistencia de clientes.
4. Verificación de persistencia de pedidos.

#### CspizzaApplicationTests (3 casos)
1. **contextLoads** — El contexto de Spring Boot carga correctamente.
2. **productStockIsCanonicalAndInventoryCanBeDeletedAndRecreated** — Stock canónico, inventario se recrea con stock.
3. **orderTotalsUseProductPriceAndRecalculateWhenDetailsChange** — Totales calculados con precio de BD, recálculo al cambiar cantidad, eliminación de detalle recalcula a 0.

### 5.3 Pruebas de controladores (MockMvc)

#### MvcControllersTest (14 casos)
- Verificación de vistas, roles, dashboards por rol, acceso denegado.

#### MvcErrorHandlingTest (7 casos)
- Manejo de errores 404, validaciones en formularios, mensajes de error.

#### MvcDataIntegrityExceptionHandlerTest (2 casos)
- Manejo de violaciones de integridad de datos.

#### RestControllersTest (8 casos)
- CRUD REST completo, validaciones, errores 400/404/409, serialización segura de passwords.

#### SecurityConfigTest (7 casos)
- Login por roles, CSRF, logout, caché de páginas protegidas, acceso por roles.

---

## 6. DESCRIPCIÓN DE LOS ESCENARIOS EXITOSOS Y DE ERROR

### Escenarios exitosos (Happy Path)
- **Guardar** entidades válidas → retorna entidad con id asignado.
- **Actualizar** entidades existentes → campos actualizados correctamente.
- **Eliminar** entidades existentes → eliminación exitosa.
- **Buscar** por id existente → retorna `Optional` con la entidad.
- **Listar** → retorna lista de entidades.
- **Crear pedido completo** (POS) → calcula total con precios de BD, descuenta stock.
- **Generar factura** desde pedido → calcula subtotal, IVA (19%) y total.
- **Transiciones de estado** válidas (PENDIENTE → EN_PREPARACION → LISTO → ENTREGADO).
- **Cancelación de pedido** → repone stock automáticamente.

### Escenarios de error
- **Datos nulos o vacíos** → `IllegalArgumentException` ("El cuerpo de la solicitud es obligatorio", "El nombre es obligatorio", etc.).
- **Registros inexistentes** → `NotFoundException` ("No existe el registro", "No existe el pedido", etc.).
- **Duplicados** → `ConflictException` ("El número de factura ya está registrado", "El producto ya tiene un registro de inventario", "El nombre de usuario ya está registrado").
- **Datos inválidos** → `IllegalArgumentException` (stock negativo, cantidad 0, max < min, rol inválido, password vacío).
- **Stock insuficiente** → `ConflictException`.
- **Producto inactivo** → `ConflictException`.
- **Transiciones de estado inválidas** → `ConflictException`.
- **Acceso sin autenticación** → 302 (redirección a login).
- **Acceso sin permisos** → 403 (Forbidden).
- **Token CSRF ausente** → 403 (Forbidden).

---

## 7. EVIDENCIAS DE EJECUCIÓN DE LAS PRUEBAS

### Comando de ejecución
```powershell
.\mvnw.cmd test
```

### Salida de la ejecución
```
[INFO] Tests run: 108, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 108, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 33.764 s
[INFO] Finished at: 2026-11-16T14:44:32-05:00
```

📸 **Evidencia 1:** Captura de terminal mostrando "Tests run: 108, Failures: 0, Errors: 0, Skipped: 0" y "BUILD SUCCESS".

📸 **Evidencia 2:** Captura del IDE (VS Code / IntelliJ) mostrando los archivos de test en `src/test/java/com/uniremington/cspizza/service/`.

📸 **Evidencia 3:** Captura de un test individual ejecutándose (ej. `PedidoServicePosTest.crearConDetallesCalculaTotalCorrectamente` con estado verde ✅).

### Distribución de pruebas por clase

| Clase de prueba | Casos | Estado |
|---|---|---|
| `CategoriaServiceTest` | 7 | ✅ Pass |
| `ClienteServiceTest` | 8 | ✅ Pass |
| `ProductoServiceTest` | 8 | ✅ Pass |
| `UsuarioServiceTest` | 8 | ✅ Pass |
| `InventarioServiceTest` | 8 | ✅ Pass |
| `FacturaServiceTest` | 6 | ✅ Pass |
| `DetallePedidoServiceTest` | 8 | ✅ Pass |
| `PedidoServicePosTest` | 10 | ✅ Pass |
| `ServicePersistenceTests` | 4 | ✅ Pass |
| `MvcControllersTest` | 14 | ✅ Pass |
| `MvcErrorHandlingTest` | 7 | ✅ Pass |
| `MvcDataIntegrityExceptionHandlerTest` | 2 | ✅ Pass |
| `RestControllersTest` | 8 | ✅ Pass |
| `SecurityConfigTest` | 7 | ✅ Pass |
| `CspizzaApplicationTests` | 3 | ✅ Pass |
| **TOTAL** | **108** | **✅ 0 fallos** |

---

## 8. RESULTADOS OBTENIDOS

- **108 pruebas ejecutadas**, **0 fallos**, **0 errores**, **0 omitidas**.
- Tiempo total de ejecución: ~33.8 segundos.
- Todas las pruebas de servicios (unitarias con Mockito) y de integración (H2) pasaron exitosamente.
- Las pruebas de controladores (MVC y REST) validaron correctamente los endpoints, las validaciones y los códigos de estado HTTP (200, 201, 204, 400, 404, 409, 403).
- Las pruebas de seguridad validaron la autenticación por roles, CSRF y protección de rutas.

---

## 9. ERRORES IDENTIFICADOS Y CORRECCIONES REALIZADAS

Durante el desarrollo y ejecución de las pruebas, se identificaron y corrigieron los siguientes errores:

### 9.1 Error en `FacturaRepository.findTopByOrderByIdFacturaDesc()`
- **Problema:** La consulta `@Query("SELECT f FROM FacturaEntity f ORDER BY f.idFactura DESC")` devolvía TODOS los registros, pero el método esperaba un solo resultado → `NonUniqueResultException`.
- **Corrección:** Se eliminó la anotación `@Query` y se dejó que Spring Data JPA derive la consulta (limita automáticamente a 1 resultado).
- **Archivo:** `FacturaRepository.java`

### 9.2 Error en `AuthMvcController` — `pedidoService` nulo
- **Problema:** El constructor no tenía la anotación `@Autowired`, por lo que Spring usaba el constructor vacío → `pedidoService` quedaba `null` → el dashboard no mostraba pedidos ni KPIs.
- **Corrección:** Se agregó `@Autowired` al constructor y el import correspondiente.
- **Archivo:** `AuthMvcController.java`

### 9.3 Error en plantillas Thymeleaf — `#authorization.hasRole()`
- **Problema:** Se usaba `#authorization.hasRole('ADMIN')` (método inexistente en Thymeleaf Spring Security 6) → 500 al renderizar.
- **Corrección:** Se reemplazó por `sec:authorize="hasRole('ADMIN')"`.
- **Archivos:** `pedidos/lista.html`, `pedidos/detalle.html`

### 9.4 Error en plantillas — mojibake (acentos corruptos)
- **Problema:** Los caracteres acentuados quedaron doble-codificados (UTF-8 → Latin-1 → UTF-8).
- **Corrección:** Se reescribieron los templates con codificación UTF-8 correcta.
- **Archivos:** Todos los templates en `src/main/resources/templates/`

### 9.5 Error en `pedidos/nuevo.html` — campo `totalPedido` inexistente
- **Problema:** Se usaba `th:field="*{totalPedido}"` pero `PedidoNuevoVO` no tiene esa propiedad → `NotReadablePropertyException`.
- **Corrección:** Se reemplazó por un input hidden con `value="0"`.

### 9.6 Error en `pedidos/lista.html` y `pedidos/detalle.html` — URL de Thymeleaf mal formada
- **Problema:** `@{/pedidos/{id}(id=${pedido.idPedido})/estado}` no evaluaba `{id}` → 400/500 al cambiar estado.
- **Corrección:** `@{/pedidos/{id}/estado(id=${pedido.idPedido})}`.

### 9.7 Error en `pedidos/nuevo.html` — bucle `th:each` inválido en script inline
- **Problema:** `<th:each="producto : ${productos}">` como etiqueta no es válido en Thymeleaf → `producto` era null.
- **Corrección:** Se reemplazó por serialización directa: `window.__productos__ = /*[[${productos}]]*/ [];`

### 9.8 Error en `SecurityConfig` — dos beans `UserDetailsService` + PasswordEncoder incompatible
- **Problema:** Había dos beans `UserDetailsService` y el `PasswordEncoder` era `BCryptPasswordEncoder`, incompatible con contraseñas de texto plano con prefijo `{noop}`.
- **Corrección:** Se dejó un único `UserDetailsService` y se implementó un `PasswordEncoder` que soporta `{noop}` (texto plano) y BCrypt.

---

## 10. CONCLUSIONES

1. Se implementaron **108 pruebas unitarias e integración** sobre el prototipo cspizza, cubriendo la lógica de negocio de todos los servicios principales (categorías, clientes, productos, usuarios, inventario, facturas, detalles de pedido y pedidos).
2. Se aplicaron buenas prácticas de desarrollo: aislamiento de dependencias con **Mockito**, pruebas de éxito y error, validación de resultados con **JUnit 5** y pruebas de integración con **H2**.
3. Se identificaron y corrigieron **8 errores** en el código fuente (repositorios, controladores, plantillas, seguridad), mejorando la robustez y calidad del software.
4. Todas las pruebas pasaron exitosamente (`BUILD SUCCESS`, 108/108), validando el correcto funcionamiento del prototipo.
5. El ejercicio de control de versiones con Git y GitHub se completó exitosamente: repositorio creado, 122 archivos subidos, rama creada y Pull Request fusionado.

---

## 📸 EVIDENCIAS (capturas de pantalla a incluir)

1. **Terminal:** `.\mvnw.cmd test` mostrando "Tests run: 108, Failures: 0, Errors: 0, Skipped: 0" y "BUILD SUCCESS".
2. **IDE:** Archivos de test en `src/test/java/com/uniremington/cspizza/service/`.
3. **GitHub:** Repositorio https://github.com/lubierk-crypto/cspizza con los archivos.
4. **GitHub:** Pull Request #2 fusionado (Merged).
5. **Aplicación:** Pantalla de login centrada y dashboard con pedidos.

---

**Repositorio GitHub:** https://github.com/lubierk-crypto/cspizza

**Documento generado para la entrega de la actividad de pruebas unitarias.** 🎉

---

📌 **Nota:** Copia este contenido en Word, agrega las capturas de pantalla en los apartados correspondientes, y convierte a PDF (Archivo → Guardar como → PDF). Si necesitas que ajuste algún apartado o agregue más detalle, dime. 🚀