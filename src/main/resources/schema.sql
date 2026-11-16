CREATE DATABASE IF NOT EXISTS cspizza;
USE cspizza;

CREATE TABLE IF NOT EXISTS categorias (
    id_categoria BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id_categoria)
);

CREATE TABLE IF NOT EXISTS clientes (
    id_cliente BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NULL,
    correo VARCHAR(150) NULL,
    direccion VARCHAR(255) NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_cliente)
);

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT,
    nombre_usuario VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario)
);

CREATE TABLE IF NOT EXISTS productos (
    id_producto BIGINT NOT NULL AUTO_INCREMENT,
    id_categoria BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(255) NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    imagen_url VARCHAR(255) NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_producto),
    CONSTRAINT fk_productos_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
);

CREATE TABLE IF NOT EXISTS pedidos (
    id_pedido BIGINT NOT NULL AUTO_INCREMENT,
    id_cliente BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    fecha_pedido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    total_pedido DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    observacion VARCHAR(255) NULL,
    tipo_entrega VARCHAR(30) NULL,
    direccion_entrega VARCHAR(255) NULL,
    PRIMARY KEY (id_pedido),
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    CONSTRAINT fk_pedidos_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE TABLE IF NOT EXISTS detalle_pedidos (
    id_detalle_pedido BIGINT NOT NULL AUTO_INCREMENT,
    id_pedido BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    observacion VARCHAR(255) NULL,
    PRIMARY KEY (id_detalle_pedido),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

CREATE TABLE IF NOT EXISTS facturas (
    id_factura BIGINT NOT NULL AUTO_INCREMENT,
    id_pedido BIGINT NOT NULL,
    numero_factura VARCHAR(50) NOT NULL UNIQUE,
    fecha_emision DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL,
    iva DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'EMITIDA',
    PRIMARY KEY (id_factura),
    CONSTRAINT fk_facturas_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido)
);

CREATE TABLE IF NOT EXISTS inventarios (
    id_inventario BIGINT NOT NULL AUTO_INCREMENT,
    id_producto BIGINT NOT NULL,
    cantidad_actual INT NOT NULL DEFAULT 0,
    cantidad_minima INT NOT NULL DEFAULT 0,
    cantidad_maxima INT NOT NULL DEFAULT 0,
    ubicacion VARCHAR(150) NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_inventario),
    CONSTRAINT uk_inventarios_producto UNIQUE (id_producto),
    CONSTRAINT fk_inventario_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

-- Datos de ejemplo idempotentes: siempre se resuelven las claves foraneas por una clave de negocio,
-- nunca por identificadores AUTO_INCREMENT fijos. Se conservan registros existentes.
INSERT INTO categorias (nombre, descripcion, estado)
SELECT 'Pizzas', 'Pizzas tradicionales y especiales', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre = 'Pizzas');
INSERT INTO categorias (nombre, descripcion, estado)
SELECT 'Bebidas', 'Bebidas frias y calientes', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre = 'Bebidas');
INSERT INTO categorias (nombre, descripcion, estado)
SELECT 'Complementos', 'Complementos y acompanamientos', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre = 'Complementos');

INSERT INTO clientes (nombre, apellido, telefono, correo, direccion, estado)
SELECT 'Ana', 'Garcia', '3001112233', 'ana@correo.com', 'Calle 10 #20-30', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE correo = 'ana@correo.com');
INSERT INTO clientes (nombre, apellido, telefono, correo, direccion, estado)
SELECT 'Luis', 'Perez', '3002223344', 'luis@correo.com', 'Carrera 11 #25-15', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE correo = 'luis@correo.com');

INSERT INTO usuarios (nombre_usuario, nombre, apellido, rol, correo, password, estado)
SELECT 'admin', 'Administrador', 'Sistema', 'ADMIN', 'admin@cspizza.com', 'admin123', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE nombre_usuario = 'admin');
INSERT INTO usuarios (nombre_usuario, nombre, apellido, rol, correo, password, estado)
SELECT 'gerente', 'Gerente', 'General', 'GERENTE', 'gerente@cspizza.com', 'gerente123', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE nombre_usuario = 'gerente');
INSERT INTO usuarios (nombre_usuario, nombre, apellido, rol, correo, password, estado)
SELECT 'empleado', 'Empleado', 'Caja', 'EMPLEADO', 'empleado@cspizza.com', 'empleado123', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE nombre_usuario = 'empleado');

INSERT INTO productos (id_categoria, nombre, descripcion, precio_unitario, stock, estado, imagen_url)
SELECT c.id_categoria, 'Pizza Margarita', 'Pizza con salsa de tomate, mozzarella y albahaca', 26000.00, 20, 'ACTIVO', 'images/pizza-margarita.jpg'
FROM categorias c WHERE c.nombre = 'Pizzas'
AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.id_categoria = c.id_categoria AND p.nombre = 'Pizza Margarita');
INSERT INTO productos (id_categoria, nombre, descripcion, precio_unitario, stock, estado, imagen_url)
SELECT c.id_categoria, 'Pizza Pepperoni', 'Pizza con pepperoni y queso mozzarella', 30000.00, 18, 'ACTIVO', 'images/pizza-pepperoni.jpg'
FROM categorias c WHERE c.nombre = 'Pizzas'
AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.id_categoria = c.id_categoria AND p.nombre = 'Pizza Pepperoni');
INSERT INTO productos (id_categoria, nombre, descripcion, precio_unitario, stock, estado, imagen_url)
SELECT c.id_categoria, 'Gaseosa 600ml', 'Gaseosa fría de diferentes sabores', 4500.00, 40, 'ACTIVO', 'images/gaseosa.jpg'
FROM categorias c WHERE c.nombre = 'Bebidas'
AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.id_categoria = c.id_categoria AND p.nombre = 'Gaseosa 600ml');
INSERT INTO productos (id_categoria, nombre, descripcion, precio_unitario, stock, estado, imagen_url)
SELECT c.id_categoria, 'Coca-Cola 400ml', 'Refresco de cola 400ml', 3500.00, 50, 'ACTIVO', 'images/coca-cola-400ml.jpg'
FROM categorias c WHERE c.nombre = 'Bebidas'
AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.id_categoria = c.id_categoria AND p.nombre = 'Coca-Cola 400ml');
INSERT INTO productos (id_categoria, nombre, descripcion, precio_unitario, stock, estado, imagen_url)
SELECT c.id_categoria, 'Coca-Cola 1.5L', 'Refresco de cola 1.5 litros', 8500.00, 30, 'ACTIVO', 'images/coca-cola-1.5l.jpg'
FROM categorias c WHERE c.nombre = 'Bebidas'
AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.id_categoria = c.id_categoria AND p.nombre = 'Coca-Cola 1.5L');
INSERT INTO productos (id_categoria, nombre, descripcion, precio_unitario, stock, estado, imagen_url)
SELECT c.id_categoria, 'Jugo de Naranja 500ml', 'Jugo natural de naranja 500ml', 4200.00, 25, 'ACTIVO', 'images/jugo-naranja.jpg'
FROM categorias c WHERE c.nombre = 'Bebidas'
AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.id_categoria = c.id_categoria AND p.nombre = 'Jugo de Naranja 500ml');
INSERT INTO productos (id_categoria, nombre, descripcion, precio_unitario, stock, estado, imagen_url)
SELECT c.id_categoria, 'Papas Fritas', 'Papas fritas con sal', 7000.00, 35, 'ACTIVO', 'images/papas-fritas.jpg'
FROM categorias c WHERE c.nombre = 'Complementos'
AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.id_categoria = c.id_categoria AND p.nombre = 'Papas Fritas');
INSERT INTO productos (id_categoria, nombre, descripcion, precio_unitario, stock, estado, imagen_url)
SELECT c.id_categoria, 'Aros de Cebolla', 'Aros de cebolla empanizados', 6500.00, 20, 'ACTIVO', 'images/aros-cebolla.jpg'
FROM categorias c WHERE c.nombre = 'Complementos'
AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.id_categoria = c.id_categoria AND p.nombre = 'Aros de Cebolla');

-- Cliente predeterminado para ventas sin registro (mostrador)
INSERT INTO clientes (nombre, apellido, telefono, correo, direccion, estado)
SELECT 'Cliente', 'Mostrador', '0000000000', 'mostrador@cspizza.local', 'Mostrador', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE correo = 'mostrador@cspizza.local');

INSERT INTO pedidos (id_cliente, id_usuario, estado, total_pedido, observacion, tipo_entrega, direccion_entrega)
SELECT c.id_cliente, u.id_usuario, 'PENDIENTE', 30500.00, 'Sin cebolla', 'DOMICILIO', 'Calle 10 #20-30'
FROM clientes c CROSS JOIN usuarios u
WHERE c.correo = 'ana@correo.com' AND u.nombre_usuario = 'empleado'
AND NOT EXISTS (SELECT 1 FROM pedidos p WHERE p.id_cliente = c.id_cliente AND p.id_usuario = u.id_usuario AND p.observacion = 'Sin cebolla');
INSERT INTO pedidos (id_cliente, id_usuario, estado, total_pedido, observacion, tipo_entrega, direccion_entrega)
SELECT c.id_cliente, u.id_usuario, 'ENTREGADO', 26000.00, 'Sin comentarios', 'LOCAL', 'Sucursal principal'
FROM clientes c CROSS JOIN usuarios u
WHERE c.correo = 'luis@correo.com' AND u.nombre_usuario = 'empleado'
AND NOT EXISTS (SELECT 1 FROM pedidos p WHERE p.id_cliente = c.id_cliente AND p.id_usuario = u.id_usuario AND p.observacion = 'Sin comentarios');

INSERT INTO detalle_pedidos (id_pedido, id_producto, cantidad, precio_unitario, subtotal, observacion)
SELECT p.id_pedido, pr.id_producto, 1, pr.precio_unitario, pr.precio_unitario, 'Base normal'
FROM pedidos p JOIN clientes c ON c.id_cliente = p.id_cliente JOIN productos pr ON pr.nombre = 'Pizza Margarita'
WHERE c.correo = 'ana@correo.com' AND p.observacion = 'Sin cebolla'
AND NOT EXISTS (SELECT 1 FROM detalle_pedidos d WHERE d.id_pedido = p.id_pedido AND d.id_producto = pr.id_producto);
INSERT INTO detalle_pedidos (id_pedido, id_producto, cantidad, precio_unitario, subtotal, observacion)
SELECT p.id_pedido, pr.id_producto, 1, pr.precio_unitario, pr.precio_unitario, 'Sin hielo'
FROM pedidos p JOIN clientes c ON c.id_cliente = p.id_cliente JOIN productos pr ON pr.nombre = 'Gaseosa 600ml'
WHERE c.correo = 'ana@correo.com' AND p.observacion = 'Sin cebolla'
AND NOT EXISTS (SELECT 1 FROM detalle_pedidos d WHERE d.id_pedido = p.id_pedido AND d.id_producto = pr.id_producto);

INSERT INTO facturas (id_pedido, numero_factura, subtotal, iva, total, estado)
SELECT p.id_pedido, 'FAC-000001', 30500.00, 0.00, 30500.00, 'EMITIDA'
FROM pedidos p JOIN clientes c ON c.id_cliente = p.id_cliente
WHERE c.correo = 'ana@correo.com' AND p.observacion = 'Sin cebolla'
AND NOT EXISTS (SELECT 1 FROM facturas f WHERE f.numero_factura = 'FAC-000001');
INSERT INTO facturas (id_pedido, numero_factura, subtotal, iva, total, estado)
SELECT p.id_pedido, 'FAC-000002', 26000.00, 0.00, 26000.00, 'PAGADA'
FROM pedidos p JOIN clientes c ON c.id_cliente = p.id_cliente
WHERE c.correo = 'luis@correo.com' AND p.observacion = 'Sin comentarios'
AND NOT EXISTS (SELECT 1 FROM facturas f WHERE f.numero_factura = 'FAC-000002');

INSERT INTO inventarios (id_producto, cantidad_actual, cantidad_minima, cantidad_maxima, ubicacion, estado)
SELECT p.id_producto, p.stock, 5, 50, 'BODEGA-A', 'ACTIVO'
FROM productos p WHERE p.nombre = 'Pizza Margarita'
AND NOT EXISTS (SELECT 1 FROM inventarios i WHERE i.id_producto = p.id_producto);
INSERT INTO inventarios (id_producto, cantidad_actual, cantidad_minima, cantidad_maxima, ubicacion, estado)
SELECT p.id_producto, p.stock, 5, 40, 'BODEGA-A', 'ACTIVO'
FROM productos p WHERE p.nombre = 'Pizza Pepperoni'
AND NOT EXISTS (SELECT 1 FROM inventarios i WHERE i.id_producto = p.id_producto);
INSERT INTO inventarios (id_producto, cantidad_actual, cantidad_minima, cantidad_maxima, ubicacion, estado)
SELECT p.id_producto, p.stock, 10, 80, 'REFRIGERADOR-1', 'ACTIVO'
FROM productos p WHERE p.nombre = 'Gaseosa 600ml'
AND NOT EXISTS (SELECT 1 FROM inventarios i WHERE i.id_producto = p.id_producto);

-- Inicializa productos heredados sin inventario desde el stock canonico.
-- No elimina ni modifica las filas de productos que tienen duplicados.
INSERT INTO inventarios (id_producto, cantidad_actual, cantidad_minima, cantidad_maxima, estado)
SELECT p.id_producto, p.stock, 0, 0, 'ACTIVO'
FROM productos p
WHERE NOT EXISTS (SELECT 1 FROM inventarios i WHERE i.id_producto = p.id_producto);

-- Sincroniza un inventario unico; los grupos duplicados quedan intactos para su conciliacion manual.
UPDATE inventarios i
JOIN (
    SELECT id_producto
    FROM inventarios
    GROUP BY id_producto
    HAVING COUNT(*) = 1
) unico ON unico.id_producto = i.id_producto
JOIN productos p ON p.id_producto = i.id_producto
SET i.cantidad_actual = p.stock,
    i.fecha_actualizacion = CURRENT_TIMESTAMP
WHERE i.cantidad_actual <> p.stock;
