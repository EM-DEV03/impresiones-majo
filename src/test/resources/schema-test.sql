CREATE TABLE IF NOT EXISTS categoria (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255) NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_categoria_nombre (nombre)
);

CREATE TABLE IF NOT EXISTS proveedor (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(160) NOT NULL,
  contacto VARCHAR(120) NULL,
  telefono VARCHAR(30) NULL,
  email VARCHAR(160) NULL,
  direccion VARCHAR(255) NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_proveedor_nombre (nombre)
);

CREATE TABLE IF NOT EXISTS rol (
  id BIGINT NOT NULL AUTO_INCREMENT,
  codigo VARCHAR(50) NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255) NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uq_rol_codigo (codigo)
);

CREATE TABLE IF NOT EXISTS permiso (
  id BIGINT NOT NULL AUTO_INCREMENT,
  codigo VARCHAR(80) NOT NULL,
  descripcion VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_permiso_codigo (codigo)
);

CREATE TABLE IF NOT EXISTS usuario (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre_usuario VARCHAR(60) NOT NULL,
  nombre_completo VARCHAR(160) NOT NULL,
  email VARCHAR(160) NULL,
  password_hash VARCHAR(255) NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_usuario_nombre (nombre_usuario),
  UNIQUE KEY uq_usuario_email (email)
);

CREATE TABLE IF NOT EXISTS usuario_rol (
  usuario_id BIGINT NOT NULL,
  rol_id BIGINT NOT NULL,
  PRIMARY KEY (usuario_id, rol_id),
  KEY idx_usuario_rol_rol (rol_id),
  CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuario (id) ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT fk_usuario_rol_rol FOREIGN KEY (rol_id)
    REFERENCES rol (id) ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS rol_permiso (
  rol_id BIGINT NOT NULL,
  permiso_id BIGINT NOT NULL,
  PRIMARY KEY (rol_id, permiso_id),
  KEY idx_rol_permiso_permiso (permiso_id),
  CONSTRAINT fk_rol_permiso_rol FOREIGN KEY (rol_id)
    REFERENCES rol (id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_rol_permiso_permiso FOREIGN KEY (permiso_id)
    REFERENCES permiso (id) ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS producto (
  id BIGINT NOT NULL AUTO_INCREMENT,
  categoria_id BIGINT NOT NULL,
  proveedor_preferido_id BIGINT NULL,
  codigo VARCHAR(60) NOT NULL,
  nombre VARCHAR(160) NOT NULL,
  descripcion VARCHAR(255) NULL,
  tipo ENUM('MERCANCIA','INSUMO') NOT NULL DEFAULT 'MERCANCIA',
  unidad_medida VARCHAR(30) NOT NULL DEFAULT 'UNIDAD',
  costo_promedio DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  precio_venta DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  stock_actual DECIMAL(14,3) NOT NULL DEFAULT 0.000,
  stock_minimo DECIMAL(14,3) NOT NULL DEFAULT 0.000,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_producto_codigo (codigo),
  KEY idx_producto_categoria (categoria_id),
  KEY idx_producto_proveedor (proveedor_preferido_id),
  KEY idx_producto_nombre (nombre),
  CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id)
    REFERENCES categoria(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_producto_proveedor FOREIGN KEY (proveedor_preferido_id)
    REFERENCES proveedor(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT ck_producto_costo CHECK (costo_promedio >= 0),
  CONSTRAINT ck_producto_precio CHECK (precio_venta >= 0),
  CONSTRAINT ck_producto_stock CHECK (stock_actual >= 0 AND stock_minimo >= 0)
);

CREATE TABLE IF NOT EXISTS servicio (
  id BIGINT NOT NULL AUTO_INCREMENT,
  codigo VARCHAR(60) NOT NULL,
  nombre VARCHAR(160) NOT NULL,
  descripcion VARCHAR(255) NULL,
  tipo ENUM('IMPRESION','FOTOCOPIA','ESCANEO','AMPLIACION','OTRO') NOT NULL,
  unidad_cobro ENUM('HOJA','CARA','SERVICIO','UNIDAD') NOT NULL DEFAULT 'SERVICIO',
  precio_unitario DECIMAL(14,2) NOT NULL,
  costo_unitario_estimado DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_servicio_codigo (codigo),
  CONSTRAINT ck_servicio_precio CHECK (precio_unitario >= 0),
  CONSTRAINT ck_servicio_costo CHECK (costo_unitario_estimado >= 0)
);

CREATE TABLE IF NOT EXISTS compra (
  id BIGINT NOT NULL AUTO_INCREMENT,
  proveedor_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado ENUM('BORRADOR','CONFIRMADA') NOT NULL DEFAULT 'BORRADOR',
  metodo_pago ENUM('EFECTIVO','TRANSFERENCIA','TARJETA','OTRO') NOT NULL DEFAULT 'EFECTIVO',
  subtotal DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  descuento DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  total DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  observaciones VARCHAR(500) NULL,
  fecha_confirmacion DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_compra_proveedor (proveedor_id),
  KEY idx_compra_usuario (usuario_id),
  KEY idx_compra_fecha (fecha),
  CONSTRAINT fk_compra_proveedor FOREIGN KEY (proveedor_id)
    REFERENCES proveedor(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_compra_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuario(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT ck_compra_valores CHECK
    (subtotal >= 0 AND descuento >= 0 AND descuento <= subtotal AND total >= 0)
);

CREATE TABLE IF NOT EXISTS detalle_compra (
  id BIGINT NOT NULL AUTO_INCREMENT,
  compra_id BIGINT NOT NULL,
  producto_id BIGINT NOT NULL,
  cantidad DECIMAL(14,3) NOT NULL,
  costo_unitario DECIMAL(14,2) NOT NULL,
  subtotal DECIMAL(14,2) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_detalle_compra_producto (compra_id, producto_id),
  KEY idx_detalle_compra_producto (producto_id),
  CONSTRAINT fk_detalle_compra_compra FOREIGN KEY (compra_id)
    REFERENCES compra(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_detalle_compra_producto FOREIGN KEY (producto_id)
    REFERENCES producto(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT ck_detalle_compra_valores CHECK
    (cantidad > 0 AND costo_unitario >= 0 AND subtotal >= 0)
);

CREATE TABLE IF NOT EXISTS venta (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado ENUM('BORRADOR','CONFIRMADA','ANULADA') NOT NULL DEFAULT 'BORRADOR',
  metodo_pago ENUM('EFECTIVO','TRANSFERENCIA','TARJETA','OTRO') NOT NULL DEFAULT 'EFECTIVO',
  subtotal DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  descuento DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  total DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  valor_recibido DECIMAL(14,2) NULL,
  cambio DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  observaciones VARCHAR(500) NULL,
  fecha_confirmacion DATETIME NULL,
  fecha_anulacion DATETIME NULL,
  motivo_anulacion VARCHAR(500) NULL,
  PRIMARY KEY (id),
  KEY idx_venta_usuario (usuario_id),
  KEY idx_venta_fecha (fecha),
  KEY idx_venta_estado (estado),
  CONSTRAINT fk_venta_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuario(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT ck_venta_valores CHECK
    (subtotal >= 0 AND descuento >= 0 AND descuento <= subtotal AND total >= 0
     AND cambio >= 0 AND (valor_recibido IS NULL OR valor_recibido >= 0))
);

CREATE TABLE IF NOT EXISTS detalle_venta (
  id BIGINT NOT NULL AUTO_INCREMENT,
  venta_id BIGINT NOT NULL,
  producto_id BIGINT NULL,
  servicio_id BIGINT NULL,
  descripcion_snapshot VARCHAR(180) NOT NULL,
  cantidad DECIMAL(14,3) NOT NULL,
  precio_unitario DECIMAL(14,2) NOT NULL,
  costo_unitario_snapshot DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  subtotal DECIMAL(14,2) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_detalle_venta_venta (venta_id),
  KEY idx_detalle_venta_producto (producto_id),
  KEY idx_detalle_venta_servicio (servicio_id),
  CONSTRAINT fk_detalle_venta_venta FOREIGN KEY (venta_id)
    REFERENCES venta(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_detalle_venta_producto FOREIGN KEY (producto_id)
    REFERENCES producto(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_detalle_venta_servicio FOREIGN KEY (servicio_id)
    REFERENCES servicio(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT ck_detalle_venta_tipo CHECK
    ((producto_id IS NOT NULL AND servicio_id IS NULL)
      OR (producto_id IS NULL AND servicio_id IS NOT NULL)),
  CONSTRAINT ck_detalle_venta_valores CHECK
    (cantidad > 0 AND precio_unitario >= 0 AND costo_unitario_snapshot >= 0 AND subtotal >= 0)
);

CREATE TABLE IF NOT EXISTS movimiento_inventario (
  id BIGINT NOT NULL AUTO_INCREMENT,
  producto_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  compra_id BIGINT NULL,
  venta_id BIGINT NULL,
  tipo ENUM('ENTRADA_COMPRA','SALIDA_VENTA','REVERSION_VENTA','AJUSTE_ENTRADA','AJUSTE_SALIDA') NOT NULL,
  cantidad DECIMAL(14,3) NOT NULL,
  stock_anterior DECIMAL(14,3) NOT NULL,
  stock_posterior DECIMAL(14,3) NOT NULL,
  motivo VARCHAR(255) NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_mov_producto_fecha (producto_id, fecha),
  KEY idx_mov_usuario (usuario_id),
  KEY idx_mov_compra (compra_id),
  KEY idx_mov_venta (venta_id),
  CONSTRAINT fk_mov_producto FOREIGN KEY (producto_id)
    REFERENCES producto(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_mov_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuario(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_mov_compra FOREIGN KEY (compra_id)
    REFERENCES compra(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_mov_venta FOREIGN KEY (venta_id)
    REFERENCES venta(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT ck_mov_cantidades CHECK
    (cantidad > 0 AND stock_anterior >= 0 AND stock_posterior >= 0),
  CONSTRAINT ck_mov_referencia CHECK
    ((tipo = 'ENTRADA_COMPRA' AND compra_id IS NOT NULL AND venta_id IS NULL)
      OR (tipo IN ('SALIDA_VENTA','REVERSION_VENTA') AND compra_id IS NULL AND venta_id IS NOT NULL)
      OR (tipo IN ('AJUSTE_ENTRADA','AJUSTE_SALIDA') AND compra_id IS NULL AND venta_id IS NULL))
);

CREATE TABLE IF NOT EXISTS gasto (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  categoria ENUM('ENERGIA','INTERNET','ARRIENDO','TRANSPORTE','MANTENIMIENTO','OTRO') NOT NULL DEFAULT 'OTRO',
  concepto VARCHAR(180) NOT NULL,
  descripcion VARCHAR(500) NULL,
  valor DECIMAL(14,2) NOT NULL,
  metodo_pago ENUM('EFECTIVO','TRANSFERENCIA','TARJETA','OTRO') NOT NULL DEFAULT 'EFECTIVO',
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado ENUM('REGISTRADO','ANULADO') NOT NULL DEFAULT 'REGISTRADO',
  motivo_anulacion VARCHAR(500) NULL,
  fecha_anulacion DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_gasto_usuario (usuario_id),
  KEY idx_gasto_fecha (fecha),
  CONSTRAINT fk_gasto_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuario(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT ck_gasto_valor CHECK (valor > 0)
);

CREATE TABLE IF NOT EXISTS aporte_capital (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  origen VARCHAR(180) NOT NULL,
  valor DECIMAL(14,2) NOT NULL,
  metodo ENUM('EFECTIVO','TRANSFERENCIA','OTRO') NOT NULL DEFAULT 'EFECTIVO',
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  observaciones VARCHAR(500) NULL,
  estado ENUM('REGISTRADO','ANULADO') NOT NULL DEFAULT 'REGISTRADO',
  motivo_anulacion VARCHAR(500) NULL,
  fecha_anulacion DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_capital_usuario (usuario_id),
  KEY idx_capital_fecha (fecha),
  CONSTRAINT fk_capital_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuario(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT ck_capital_valor CHECK (valor > 0)
);

CREATE TABLE IF NOT EXISTS inversion_equipo (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  proveedor_id BIGINT NULL,
  equipo VARCHAR(180) NOT NULL,
  descripcion VARCHAR(500) NULL,
  fecha_adquisicion DATE NOT NULL,
  costo DECIMAL(14,2) NOT NULL,
  metodo_pago ENUM('EFECTIVO','TRANSFERENCIA','TARJETA','OTRO') NOT NULL DEFAULT 'EFECTIVO',
  vida_util_meses INT NULL,
  estado ENUM('REGISTRADA','ANULADA') NOT NULL DEFAULT 'REGISTRADA',
  motivo_anulacion VARCHAR(500) NULL,
  fecha_anulacion DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_inversion_usuario (usuario_id),
  KEY idx_inversion_proveedor (proveedor_id),
  KEY idx_inversion_fecha (fecha_adquisicion),
  CONSTRAINT fk_inversion_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuario(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_inversion_proveedor FOREIGN KEY (proveedor_id)
    REFERENCES proveedor(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT ck_inversion_costo CHECK (costo > 0),
  CONSTRAINT ck_inversion_vida_util CHECK (vida_util_meses IS NULL OR vida_util_meses > 0)
);

INSERT IGNORE INTO rol (codigo, nombre, descripcion) VALUES
('ADMINISTRADOR','Administrador','Gestion integral del negocio'),
('VENDEDOR','Vendedor','Venta de productos y servicios y consulta autorizada');

INSERT IGNORE INTO permiso (codigo, descripcion) VALUES
('DASHBOARD_VER','Acceder al panel principal'),
('CATEGORIAS_GESTIONAR','Administrar categorias'),
('PROVEEDORES_GESTIONAR','Administrar proveedores'),
('PRODUCTOS_VER','Consultar productos e inventario'),
('PRODUCTOS_GESTIONAR','Crear y actualizar productos'),
('STOCK_AJUSTAR','Realizar ajustes auditados de inventario'),
('MOVIMIENTOS_VER','Consultar movimientos de inventario'),
('SERVICIOS_VER','Consultar tarifas de servicios'),
('SERVICIOS_GESTIONAR','Administrar servicios y tarifas'),
('COMPRAS_GESTIONAR','Registrar y confirmar compras'),
('VENTAS_CREAR','Crear y confirmar ventas'),
('VENTAS_VER_PROPIAS','Consultar ventas propias'),
('VENTAS_VER_TODAS','Consultar todas las ventas'),
('VENTAS_ANULAR','Anular ventas confirmadas'),
('GASTOS_GESTIONAR','Registrar y anular gastos'),
('CAPITAL_GESTIONAR','Registrar y anular aportes de capital'),
('INVERSIONES_GESTIONAR','Registrar y anular inversiones en equipos'),
('REPORTES_VER','Consultar reportes y resumen financiero'),
('USUARIOS_GESTIONAR','Administrar usuarios, roles y permisos');

INSERT IGNORE INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p
WHERE r.codigo = 'ADMINISTRADOR';

INSERT IGNORE INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p
WHERE r.codigo = 'VENDEDOR'
  AND p.codigo IN ('DASHBOARD_VER','PRODUCTOS_VER','SERVICIOS_VER',
                   'VENTAS_CREAR','VENTAS_VER_PROPIAS');

INSERT IGNORE INTO categoria (nombre, descripcion) VALUES
('Papel y resmas','Hojas y papel de oficina'),
('Cartulinas','Cartulinas y materiales para manualidades'),
('Utiles escolares','Cuadernos y suministros para estudio'),
('Insumos de impresion','Tinta y consumibles utilizados internamente');

INSERT IGNORE INTO proveedor (nombre, contacto, telefono) VALUES
('Distribuidora Escolar Demo','Contacto de prueba','3000000001'),
('Proveedor de Papel Demo','Contacto de prueba','3000000002'),
('Proveedor de Tintas Demo','Contacto de prueba','3000000003');

INSERT IGNORE INTO producto
(categoria_id, proveedor_preferido_id, codigo, nombre, descripcion, tipo,
 unidad_medida, costo_promedio, precio_venta, stock_actual, stock_minimo)
VALUES
((SELECT id FROM categoria WHERE nombre='Papel y resmas'),
 (SELECT id FROM proveedor WHERE nombre='Proveedor de Papel Demo'),
 'PAP-001','Resma papel carta x500','Producto para venta','MERCANCIA',
 'RESMA',14000.00,18000.00,0.000,2.000),
((SELECT id FROM categoria WHERE nombre='Papel y resmas'),
 (SELECT id FROM proveedor WHERE nombre='Proveedor de Papel Demo'),
 'PAP-002','Hoja blanca carta','Hoja de papel por unidad','MERCANCIA',
 'UNIDAD',40.00,200.00,0.000,50.000),
((SELECT id FROM categoria WHERE nombre='Cartulinas'),
 (SELECT id FROM proveedor WHERE nombre='Distribuidora Escolar Demo'),
 'CAR-001','Octavo de cartulina negra','Precio ilustrativo','MERCANCIA',
 'UNIDAD',1000.00,1500.00,0.000,10.000),
((SELECT id FROM categoria WHERE nombre='Utiles escolares'),
 (SELECT id FROM proveedor WHERE nombre='Distribuidora Escolar Demo'),
 'ESC-001','Cuaderno pequeno','Cuaderno de ejemplo','MERCANCIA',
 'UNIDAD',2500.00,3500.00,0.000,5.000),
((SELECT id FROM categoria WHERE nombre='Insumos de impresion'),
 (SELECT id FROM proveedor WHERE nombre='Proveedor de Tintas Demo'),
 'INS-001','Tinta negra','Insumo para servicio de impresion','INSUMO',
 'FRASCO',45000.00,0.00,0.000,1.000),
((SELECT id FROM categoria WHERE nombre='Insumos de impresion'),
 (SELECT id FROM proveedor WHERE nombre='Proveedor de Tintas Demo'),
 'INS-002','Tinta color','Insumo para servicio de impresion','INSUMO',
 'FRASCO',55000.00,0.00,0.000,1.000);

INSERT IGNORE INTO servicio
(codigo, nombre, descripcion, tipo, unidad_cobro, precio_unitario, costo_unitario_estimado)
VALUES
('SER-001','Impresion blanco y negro - una cara','Precio por hoja impresa','IMPRESION','HOJA',400.00,70.00),
('SER-002','Impresion a color - una cara','Precio por hoja impresa','IMPRESION','HOJA',600.00,180.00),
('SER-003','Impresion blanco y negro - doble cara','Precio por hoja fisica','IMPRESION','HOJA',800.00,130.00),
('SER-004','Fotocopia blanco y negro','Precio por cara','FOTOCOPIA','CARA',400.00,60.00),
('SER-005','Escaneo de documento','Precio por documento','ESCANEO','SERVICIO',1000.00,50.00),
('SER-006','Ampliacion de documento','Precio por ampliacion','AMPLIACION','SERVICIO',1500.00,200.00);