# CSPizza

Sistema de gestión para una pizzería, desarrollado con Java 17 y Spring Boot 4.1.2. Conserva las ocho áreas funcionales, las vistas Thymeleaf y sus rutas web/REST.

## Arquitectura

Las solicitudes siguen el flujo **MVC/REST → servicio transaccional → Spring Data JPA → MySQL**. Los `VO` se mantienen como DTO para no acoplar la interfaz ni el JSON al modelo persistente.

| Paquete | Responsabilidad |
| --- | --- |
| `controller.mvc` | Vistas Thymeleaf y rutas web existentes. |
| `controller.res` | API JSON, validación y errores HTTP centralizados. |
| `service` | Reglas de negocio, validaciones y transacciones. |
| `model.entity` | Ocho entidades JPA con relaciones y restricciones. |
| `model.repository` | Repositorios Spring Data `JpaRepository`. |
| `model.vo` | DTO utilizados por las vistas y la API. |

Relaciones: producto–categoría; pedido–cliente/usuario; detalle–pedido/producto; factura–pedido; inventario–producto.

## Módulos y rutas

| Módulo | Interfaz web | API |
| --- | --- | --- |
| Categorías | `/categorias` | `/api/categorias` |
| Clientes | `/clientes` | `/api/clientes` |
| Detalles de pedido | `/detalle-pedidos` | `/api/detalle-pedidos` |
| Facturas | `/facturas` | `/api/facturas` |
| Inventarios | `/inventarios` | `/api/inventarios` |
| Pedidos | `/pedidos`, `/pedidos/nuevo`, `/pedidos/{id}`, `/pedidos/cocina` | `/api/pedidos`, `/api/pedidos/completo` |
| Productos | `/productos` | `/api/productos` |
| Usuarios | `/usuarios` | `/api/usuarios` |

## Requisitos y configuración

- JDK 17 y MySQL.
- Maven Wrapper (`mvnw.cmd` en Windows).

La conexión predeterminada es `localhost:3306/cspizza`. Se puede configurar mediante `CSPIZZA_DB_URL`, `CSPIZZA_DB_USERNAME` y `CSPIZZA_DB_PASSWORD`; no es necesario guardar secretos en el repositorio. Antes del primer inicio, crea la base e importa manualmente `src/main/resources/schema.sql` (o `cspizza.sql` desde la raíz). Los scripts crean las tablas y cargan datos de ejemplo idempotentes; no se ejecutan automáticamente contra la base en cada inicio. Hibernate usa `ddl-auto=validate` para verificar el esquema y detener el arranque si no coincide con las entidades; no modifica ni elimina tablas. `schema.sql` es el esquema de referencia.

Los scripts crean de forma idempotente datos de ejemplo y resuelven relaciones mediante claves de negocio, no mediante IDs auto-incrementales fijos. Haz una copia de seguridad antes de aplicar cambios de esquema manualmente. `inventarios` permite cero o un registro por producto. Si la base existente tiene filas duplicadas para un producto, Hibernate no las elimina y la restricción única puede impedir el arranque. Antes de iniciar la aplicación, reconcilia manualmente:

1. Haz una copia de seguridad completa y localiza los duplicados:

   ```sql
   SELECT id_producto, COUNT(*) AS cantidad_filas,
          GROUP_CONCAT(id_inventario ORDER BY id_inventario) AS ids
   FROM inventarios
   GROUP BY id_producto
   HAVING COUNT(*) > 1;
   ```

2. Inspecciona todas las filas afectadas y decide qué metadatos de ubicación, mínimos/máximos y estado deben conservarse. `productos.stock` es el valor canónico para `cantidad_actual`.
3. Antes de cualquier limpieza manual, exporta/copia todas las filas afectadas a una tabla de archivo con fecha y verifica la copia. Conserva el registro elegido y refleja allí `productos.stock`; solo después de validar la copia y aprobar la reconciliación, elimina manualmente las filas redundantes necesarias para cumplir la unicidad.
4. Vuelve a ejecutar la consulta de duplicados y confirma que no devuelve filas. Para una instalación existente, comprueba si ya hay un índice único sobre `id_producto`:

   ```sql
   SHOW INDEX FROM inventarios
   WHERE Column_name = 'id_producto' AND Non_unique = 0;
   ```

   Solo si la consulta no devuelve índices únicos y ya no hay duplicados, aplica esta migración **una sola vez**:

   ```sql
   ALTER TABLE inventarios
     ADD CONSTRAINT uk_inventarios_producto UNIQUE (id_producto);
   ```

   No ejecutes el `ALTER TABLE` si ya existe un índice único equivalente. Las instalaciones nuevas ya incluyen esta restricción en `schema.sql`.

La aplicación nunca fusiona ni borra automáticamente inventarios duplicados.

Para ejecutar en Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación escucha en `http://localhost:8080`.

## Reglas de negocio

- `productos.stock` es la fuente canónica. Al crear o actualizar un producto, su cantidad se sincroniza dentro de la misma transacción con `inventarios.cantidad_actual`.
- Cada producto puede tener cero o un inventario. Crear un inventario duplicado devuelve `409`; inventarios duplicados preexistentes se preservan y requieren reconciliación manual.
- `productos.stock` es canónico. Crear un producto crea su registro de inventario; al crear o actualizar un inventario, `cantidad_actual` se inicializa/sincroniza desde `productos.stock`, y las actualizaciones de producto sincronizan la fila si existe. `cantidad_actual` es opcional en las solicitudes: si se envía, se ignora y prevalece siempre el stock del producto. Eliminar un inventario no cambia el stock ni crea una fila sustituta; al eliminar un producto también se elimina el inventario que aún exista, dentro de la misma transacción.
- Los subtotales de detalle se calculan con el precio vigente del producto y la cantidad. El total del pedido se recalcula al guardar, modificar o eliminar un detalle. Los totales recibidos en solicitudes no se consideran autoritativos.
- Subtotal y total de factura se calculan en el servidor a partir del pedido y del IVA indicado.

## Validación y mensajes de error

- Las vistas web validan los datos con Bean Validation (`@Valid`) y vuelven al formulario con el mensaje de error debajo del campo correspondiente; los errores de negocio (`IllegalArgumentException`, `NotFoundException`, `ConflictException`) también se muestran en el formulario en lugar de producir una página de error 500.
- Editar o eliminar un registro inexistente redirige al listado del módulo con un mensaje informativo.
- La API REST responde con `400` (validación), `404` (registro inexistente) y `409` (duplicados o integridad referencial), siempre en JSON con el detalle de los campos inválidos.

## Seguridad

Spring Security conserva el login de formulario, roles y protección CSRF. Las rutas administrativas siguen restringidas por rol. El cierre de sesión es POST y requiere CSRF. Las cuentas `admin/admin123`, `gerente/gerente123` y `empleado/empleado123` son usuarios **en memoria de desarrollo**; reemplázalas antes de cualquier despliegue. No son las cuentas guardadas en la tabla `usuarios`.

Los endpoints REST requieren autenticación. Para operaciones POST/PUT/DELETE envía `X-CSRF-TOKEN` con el token de la sesión. La colección `postman/CSPizza.postman_collection.json` incluye solicitudes para obtener el token e iniciar sesión, además de CRUD para cada módulo.

## API REST

Cada recurso conserva sus rutas CRUD:

| Método | Respuesta |
| --- | --- |
| `GET /api/{recurso}` | `200` y lista |
| `GET /api/{recurso}/{id}` | `200` o `404` |
| `POST /api/{recurso}` | `201` |
| `PUT /api/{recurso}/{id}` | `200`; usa el ID de la ruta |
| `DELETE /api/{recurso}/{id}` | `204` |

Los errores JSON centralizados incluyen estado, mensaje, ruta y errores de campos. La validación produce `400`, los registros inexistentes `404` y los conflictos de integridad/duplicados `409`. Las respuestas de usuario nunca incluyen la contraseña.

## Pruebas y compilación

Las pruebas usan H2 en memoria, crean el esquema a partir de las entidades JPA y no cargan datos de producción.

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

La suite cubre los servicios con datos reales en H2, la seguridad por rol y CSRF con MockMvc, y los flujos web que antes fallaban con error 500 (registro inexistente al editar, formulario inválido y relación inexistente).

## Evidencias de pruebas

La carpeta `evidencias/` contiene las evidencias entregables:

| Archivo | Contenido |
| --- | --- |
| `evidencias/generar-evidencias.ps1` | Recorre la API completa y genera la transcripción de cada petición y respuesta. |
| `evidencias/transcripciones/` | Transcripciones con el código HTTP y el JSON de cada llamada. |
| `evidencias/persistencia-mysql.sql` | Consultas que demuestran el almacenamiento y la integridad en MySQL. |
| `evidencias/guion-demo.md` | Guion de la demostración en video o presencial. |

```powershell
# con la aplicacion arrancada
powershell -ExecutionPolicy Bypass -File .\evidencias\generar-evidencias.ps1
```

La colección de Postman (`postman/CSPizza.postman_collection.json`) permite repetir las mismas
pruebas de forma interactiva: 44 peticiones que cubren el login con CSRF y el CRUD de los ocho módulos.

No se ha verificado una conexión a un servidor MySQL real en este entorno.

## Punto de venta (POS)

### Registrar un pedido completo (`/pedidos/nuevo`)

La vista de punto de venta permite tomar un pedido completo en pocos clics:

1. **Izquierda (catálogo):** productos activos con stock > 0, organizados por pestañas de categoría, con buscador por nombre y tarjetas con precio y botón "Agregar". Los productos sin stock muestran "Agotado".
2. **Derecha (pedido actual):** líneas de producto con botones − / + para ajustar cantidad, campo de observación por línea (ej. "sin cebolla"), subtotal por línea y total en vivo.
3. **Datos del pedido:**
   - **Cliente:** selector buscable. Por defecto se usa "Cliente Mostrador" para ventas sin registro. Incluye opción "Nuevo cliente rápido".
   - **Tipo de entrega:** LOCAL, RECOGER o DOMICILIO. La dirección es obligatoria solo en DOMICILIO y se autocompleta con la del cliente.
   - **Observación general:** notas para el pedido completo.
4. **Usuario:** se resuelve automáticamente desde la sesión autenticada (no se digita).

Al guardar, el sistema crea el pedido en estado PENDIENTE con todos sus detalles, calcula el total, descuenta el stock de cada producto y sincroniza `inventarios.cantidad_actual`. Todo en una sola transacción: si algo falla, no se guarda nada.

### Ver detalle de un pedido (`/pedidos/{id}`)

Muestra el cliente, las líneas con subtotales, el total, el estado y un botón "Imprimir ticket" (hoja de 80 mm para impresión térmica).

### Cambiar estado de un pedido

Mientras un pedido esté PENDIENTE o EN_PREPARACIÓN, se pueden agregar productos adicionales. El flujo de estados es:
**PENDIENTE → EN_PREPARACIÓN → LISTO → ENTREGADO**.
Desde PENDIENTE o EN_PREPARACIÓN también se puede **Anular** (estado CANCELADO), lo que repone el stock de los productos. Las transiciones inválidas devuelven 409.

## Tablero de cocina (`/pedidos/cocina`)

Tablero con columnas Pendiente / En preparación / Listo. Cada tarjeta muestra los productos y observaciones del pedido. Se refresca sola cada 30 segundos. Acceso para ADMIN y EMPLEADO.

## API REST adicional

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/api/pedidos/completo` | Crea un pedido con múltiples productos en una sola transacción. Requiere autenticación y CSRF. |

## Contribuciones
Rama de prueba para el ejercicio de Pull Request.
