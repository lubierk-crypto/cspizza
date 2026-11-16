# Guion de demostracion del prototipo cspizza

Duracion sugerida: 6-8 minutos. El orden importa: primero la persistencia, despues la API y
al final la interfaz web, para cerrar mostrando que los tres caminos usan la misma base de datos.

## Antes de empezar (5 minutos antes)

1. Tener MySQL abierto y la base `cspizza` creada e importada (`src/main/resources/schema.sql`).
2. Arrancar la aplicacion: `.\mvnw.cmd spring-boot:run`.
3. Abrir tres ventanas: la aplicacion (`http://localhost:8080`), phpMyAdmin (base `cspizza`)
   y Postman (coleccion `postman/CSPizza.postman_collection.json`).

## 1. Presentacion del sistema (1 minuto)

- El problema: gestion de una pizzeria (productos, clientes, pedidos, facturacion, inventario).
- La solucion: aplicacion web Spring Boot con API REST, interfaz Thymeleaf y MySQL.
- El modelo de datos: 8 entidades relacionadas (producto-categoria, pedido-cliente/usuario,
  detalle-pedido/producto, factura-pedido, inventario-producto).
- La arquitectura por capas: `model/entity` -> `model/repository` -> `service` -> `controller`.

## 2. Persistencia en MySQL (1 minuto)

- En phpMyAdmin, mostrar el diagrama de la base `cspizza` con las 8 tablas.
- Ejecutar `evidencias\persistencia-mysql.sql` y commented:
  - el conteo de filas de cada tabla,
  - la consulta que cruza producto + categoria + inventario,
  - la verificacion de que el total de cada pedido coincide con la suma de sus detalles.

## 3. API REST con Postman (3 minutos)

Ejecutar la coleccion en este orden, mostrando la respuesta de cada peticion:

1. **Autenticacion y CSRF**: obtener el formulario, iniciar sesion, renovar el token.
2. **Categorias**: listar -> crear -> consultar -> actualizar -> eliminar -> consultar (404).
3. **Productos**: crear un producto con stock y comprobar que la API crea su inventario.
4. **Inventarios**: intentar crear un inventario duplicado (409) y despues crearlo sin
   `cantidad_actual` (201, toma el stock del producto).
5. **Pedido completo**: crear cliente y usuario, luego pedido, detalle y factura; mostrar que
   el total del pedido se recalcula con el detalle.
6. **Errores controlados**: crear una categoria sin nombre (400 con el detalle del campo),
   consultar un id inexistente (404), duplicar un numero de factura (409).
7. **Seguridad**: repetir un POST sin el token CSRF (403) y revisar en phpMyAdmin que el
   registro NO se creo.

## 4. Interfaz web (1 minuto)

- Entrar en `http://localhost:8080` con `admin/admin123`.
- Recorrer el panel: categorias, productos, clientes, pedidos, facturas, inventario.
- Crear un producto desde el formulario y **refrescar phpMyAdmin** para mostrar la fila nueva.
- Envolver con un error: guardar un producto sin nombre y mostrar que la aplicacion vuelve al
  formulario con el mensaje (no aparece una pagina de error).
- Mostrar el cierre de sesion (POST con CSRF).

## 5. Pruebas automaticas (30 segundos)

```powershell
.\mvnw.cmd test
```

- 45 pruebas en verde: servicios con H2, seguridad por rol y CSRF con MockMvc, y los flujos web
  que antes fallaban.

## 6. Cierre (30 segundos)

- Mencionar las cifras: 8 entidades, 8 modulos CRUD, 44 peticiones en la coleccion de Postman,
  45 pruebas automaticas, 8 tablas en MySQL.
- Dejar claro que el login con roles protege las vistas y la API.
