-- Consultas para demostrar la persistencia de la informacion en MySQL.
-- Ejecutar con:  mysql -u root -p cspizza < evidencias\persistencia-mysql.sql

USE cspizza;

-- 1) Las ocho tablas del modelo de datos y su numero de filas.
SELECT 'categorias' AS tabla, COUNT(*) AS filas FROM categorias
UNION ALL SELECT 'clientes',        COUNT(*) FROM clientes
UNION ALL SELECT 'usuarios',        COUNT(*) FROM usuarios
UNION ALL SELECT 'productos',       COUNT(*) FROM productos
UNION ALL SELECT 'pedidos',         COUNT(*) FROM pedidos
UNION ALL SELECT 'detalle_pedidos', COUNT(*) FROM detalle_pedidos
UNION ALL SELECT 'facturas',        COUNT(*) FROM facturas
UNION ALL SELECT 'inventarios',     COUNT(*) FROM inventarios;

-- 2) Productos con su categoria y su inventario (relaciones del modelo de datos).
SELECT c.nombre            AS categoria,
       p.nombre            AS producto,
       p.precio_unitario   AS precio,
       p.stock             AS stock_canonico,
       i.cantidad_actual   AS cantidad_inventario,
       i.cantidad_minima,
       i.ubicacion
FROM productos p
JOIN categorias c   ON c.id_categoria = p.id_categoria
LEFT JOIN inventarios i ON i.id_producto = p.id_producto
ORDER BY c.nombre, p.nombre;

-- 3) Pedidos con su cliente, sus detalles y su factura (flujo completo).
SELECT pe.id_pedido,
       cl.nombre         AS cliente,
       u.nombre_usuario  AS usuario,
       pe.estado,
       dp.cantidad,
       pr.nombre         AS producto,
       dp.subtotal,
       pe.total_pedido,
       f.numero_factura,
       f.subtotal        AS subtotal_factura,
       f.iva,
       f.total           AS total_factura
FROM pedidos pe
JOIN clientes cl       ON cl.id_cliente = pe.id_cliente
JOIN usuarios u        ON u.id_usuario  = pe.id_usuario
JOIN detalle_pedidos dp ON dp.id_pedido  = pe.id_pedido
JOIN productos pr      ON pr.id_producto = dp.id_producto
LEFT JOIN facturas f   ON f.id_pedido   = pe.id_pedido
ORDER BY pe.id_pedido;

-- 4) Verificacion de la regla de negocio: el total del pedido es la suma de sus detalles.
SELECT pe.id_pedido,
       pe.total_pedido                     AS total_guardado,
       SUM(dp.cantidad * dp.precio_unitario) AS total_calculado,
       IF(pe.total_pedido = SUM(dp.cantidad * dp.precio_unitario), 'OK', 'REVISAR') AS estado
FROM pedidos pe
JOIN detalle_pedidos dp ON dp.id_pedido = pe.id_pedido
GROUP BY pe.id_pedido, pe.total_pedido;

-- 5) Verificacion de la regla de negocio: el inventario sigue al stock canonico del producto.
SELECT p.id_producto,
       p.nombre          AS producto,
       p.stock           AS stock_canonico,
       i.cantidad_actual AS cantidad_inventario,
       IF(p.stock = i.cantidad_actual, 'OK', 'REVISAR') AS estado
FROM productos p
JOIN inventarios i ON i.id_producto = p.id_producto
ORDER BY p.id_producto;

-- 6) Integridad referencial: las claves foraneas de cada tabla.
SELECT
  (SELECT COUNT(*) FROM productos WHERE id_categoria NOT IN (SELECT id_categoria FROM categorias))  AS productos_sin_categoria,
  (SELECT COUNT(*) FROM pedidos WHERE id_cliente  NOT IN (SELECT id_cliente  FROM clientes))       AS pedidos_sin_cliente,
  (SELECT COUNT(*) FROM pedidos WHERE id_usuario  NOT IN (SELECT id_usuario  FROM usuarios))       AS pedidos_sin_usuario,
  (SELECT COUNT(*) FROM detalle_pedidos WHERE id_pedido  NOT IN (SELECT id_pedido  FROM pedidos))  AS detalles_sin_pedido,
  (SELECT COUNT(*) FROM facturas WHERE id_pedido NOT IN (SELECT id_pedido FROM pedidos))             AS facturas_sin_pedido,
  (SELECT COUNT(*) FROM inventarios WHERE id_producto NOT IN (SELECT id_producto FROM productos))    AS inventarios_sin_producto;
