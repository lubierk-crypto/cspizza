# Evidencias de prueba

Este folder contiene las evidencias de las pruebas Realizadas contra la API REST y la
base de datos MySQL del prototipo **cspizza**.

## Contenido

| Archivo | Descripcion |
| --- | --- |
| `generar-evidencias.ps1` | Script que ejecuta el recorrido completo de pruebas y genera la transcripcion. |
| `transcripciones/` | Transcripciones generadas: peticion, codigo de respuesta y cuerpo JSON de cada llamada. |
| `persistencia-mysql.sql` | Consultas SQL para demostrar que la informacion queda almacenada en MySQL. |
| `guion-demo.md` | Guion de la demostracion (video o presencial). |

## Como reproducir las evidencias

Requisitos: JDK 17, MySQL 8 y la base `cspizza` creada a partir de `src/main/resources/schema.sql`.

```powershell
# 1. Terminal 1: arrancar la aplicacion
.\mvnw.cmd spring-boot:run

# 2. Terminal 2: generar la transcripcion de pruebas
powershell -ExecutionPolicy Bypass -File .\evidencias\generar-evidencias.ps1

# 3. Comprobar la persistencia en MySQL
mysql -u root -p cspizza < .\evidencias\persistencia-mysql.sql
```

El script deja la base de datos como estaba (borra los registros que crea).

## Que cubre la transcripcion

1. **Autenticacion y CSRF**: inicio de sesion, emision de la cookie de sesion y obtencion del token.
2. **CRUD de los ocho modulos** (categorias, clientes, usuarios, productos, pedidos, detalles de
   pedido, facturas e inventarios): listar, crear, consultar, actualizar, eliminar y comprobar
   que el registro ya no existe (404).
3. **Reglas de negocio**: el total del pedido se recalcula con el detalle y `cantidad_actual`
   del inventario se inicializa con el stock del producto.
4. **Validaciones**: 400 por datos invalidos, 404 por registros inexistentes y 409 por duplicados.
5. **Seguridad**: 403 sin token CSRF, 403 por rol sin permisos y redireccion al login sin sesion.
6. **Limpieza**: eliminacion de todos los registros creados por la prueba.

## Pruebas automaticas

Ademas de la transcripcion, el proyecto incluye una suite automatica (108 pruebas) que cubre
los mismos caminos:

```powershell
.\mvnw.cmd test
```

Los informes por clase quedan en `target/surefire-reports/`.
