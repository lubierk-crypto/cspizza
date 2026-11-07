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
    CONSTRAINT fk_inventario_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

INSERT INTO categorias (nombre, descripcion, estado) VALUES
('Pizzas', 'Pizzas tradicionales y especiales', 'ACTIVO'),
('Bebidas', 'Bebidas frías y calientes', 'ACTIVO'),
('Complementos', 'Complementos y acompañamientos', 'ACTIVO');

INSERT INTO clientes (nombre, apellido, telefono, correo, direccion, estado) VALUES
('Ana', 'García', '3001112233', 'ana@correo.com', 'Calle 10 #20-30', 'ACTIVO'),
('Luis', 'Pérez', '3002223344', 'luis@correo.com', 'Carrera 11 #25-15', 'ACTIVO');

INSERT INTO usuarios (nombre_usuario, nombre, apellido, rol, correo, password, estado) VALUES
('admin', 'Administrador', 'Sistema', 'ADMIN', 'admin@cspizza.com', 'admin123', 'ACTIVO'),
('gerente', 'Gerente', 'General', 'GERENTE', 'gerente@cspizza.com', 'gerente123', 'ACTIVO'),
('empleado', 'Empleado', 'Caja', 'EMPLEADO', 'empleado@cspizza.com', 'empleado123', 'ACTIVO');

INSERT INTO productos (id_categoria, nombre, descripcion, precio_unitario, stock, estado, imagen_url) VALUES
(1, 'Pizza Margarita', 'Pizza con salsa de tomate, mozzarella y albahaca', 26000.00, 20, 'ACTIVO', 'images/pizza-margarita.jpg'),
(1, 'Pizza Pepperoni', 'Pizza con pepperoni y queso mozzarella', 30000.00, 18, 'ACTIVO', 'images/pizza-pepperoni.jpg'),
(2, 'Gaseosa 600ml', 'Gaseosa fría de diferentes sabores', 4500.00, 40, 'ACTIVO', 'images/gaseosa.jpg');

INSERT INTO pedidos (id_cliente, id_usuario, estado, total_pedido, observacion, tipo_entrega, direccion_entrega) VALUES
(1, 3, 'PENDIENTE', 30500.00, 'Sin cebolla', 'DOMICILIO', 'Calle 10 #20-30'),
(2, 3, 'ENTREGADO', 26000.00, 'Sin comentarios', 'LOCAL', 'Sucursal principal');

INSERT INTO detalle_pedidos (id_pedido, id_producto, cantidad, precio_unitario, subtotal, observacion) VALUES
(1, 1, 1, 26000.00, 26000.00, 'Base normal'),
(1, 3, 1, 4500.00, 4500.00, 'Sin hielo');

INSERT INTO facturas (id_pedido, numero_factura, subtotal, iva, total, estado) VALUES
(1, 'FAC-000001', 30500.00, 0.00, 30500.00, 'EMITIDA'),
(2, 'FAC-000002', 26000.00, 0.00, 26000.00, 'PAGADA');

INSERT INTO inventarios (id_producto, cantidad_actual, cantidad_minima, cantidad_maxima, ubicacion, estado) VALUES
(1, 20, 5, 50, 'BODEGA-A', 'ACTIVO'),
(2, 18, 5, 40, 'BODEGA-A', 'ACTIVO'),
(3, 40, 10, 80, 'REFRIGERADOR-1', 'ACTIVO');
