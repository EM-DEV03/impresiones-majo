# Modelo de datos

Fuente: [impresiones-majo.sql](../database/impresiones-majo.sql), conservado sin cambios. Cada columna se representa en camelCase en Java.

| Tabla | VO / DAO | Relaciones y función |
|---|---|---|
| categoria | CategoriaVO / CategoriaDAO | Clasifica productos; nombre único |
| proveedor | ProveedorVO / ProveedorDAO | Contacto y suministros; nombre único |
| rol | RolVO / RolDAO | Código único y estado |
| permiso | PermisoVO / PermisoDAO | Permiso con código único |
| usuario | UsuarioVO / UsuarioDAO | Usuario y correo únicos; hash BCrypt |
| usuario_rol | UsuarioRolVO / UsuarioRolDAO | PK compuesta usuario_id + rol_id |
| rol_permiso | RolPermisoVO / RolPermisoDAO | PK compuesta rol_id + permiso_id |
| producto | ProductoVO / ProductoDAO | Categoría, proveedor opcional, precio, costo y saldos |
| servicio | ServicioVO / ServicioDAO | Tarifa, unidad de cobro, costo estimado |
| compra | CompraVO / CompraDAO | Proveedor, usuario, pago, estado y totales |
| detalle_compra | DetalleCompraVO / DetalleCompraDAO | Producto único por compra y cantidad |
| venta | VentaVO / VentaDAO | Usuario, pago, totales, confirmación y anulación |
| detalle_venta | DetalleVentaVO / DetalleVentaDAO | Producto XOR servicio; descripción, precio y costo históricos |
| movimiento_inventario | MovimientoInventarioVO / MovimientoInventarioDAO | Producto, responsable, referencia y saldos |
| gasto | GastoVO / GastoDAO | Gasto operativo, categoría y estado |
| aporte_capital | AporteCapitalVO / AporteCapitalDAO | Aporte y origen |
| inversion_equipo | InversionEquipoVO / InversionEquipoDAO | Equipo, proveedor opcional y vida útil |

## Tipos

BIGINT → Long; INT → Integer; VARCHAR/ENUM → String; DECIMAL → BigDecimal; TINYINT(1) → Boolean; DATETIME → LocalDateTime; DATE → LocalDate.

Los VO incluyen constructor vacío y completo, getters, setters y `toString` limitado a identificación. No imprimen datos personales, contraseñas ni hashes. `BeanPropertyRowMapper` resuelve las columnas snake_case. Los VO son objetos de transporte; la validación y las transacciones están en `service`.

## Estados y mutaciones

| Entidad | Estados / transición | Efecto de negocio |
|---|---|---|
| `compra` | `BORRADOR` → `CONFIRMADA` | La confirmación registra entradas de inventario; no existe anulación de compra. |
| `venta` | `BORRADOR` → `CONFIRMADA` → `ANULADA` | Confirmar descuenta productos; anular registra reversiones. |
| `gasto` | `REGISTRADO` → `ANULADO` | Se conserva el registro y se excluye de reportes vigentes. |
| `aporte_capital` | `REGISTRADO` → `ANULADO` | Se conserva el historial financiero. |
| `inversion_equipo` | `REGISTRADA` → `ANULADA` | Se conserva la adquisición y se excluye de indicadores vigentes. |

`stock_actual` solo cambia mediante una compra confirmada, una venta confirmada, una anulación de venta o un ajuste auditado. `costo_promedio` se recalcula al recibir mercancía o registrar una entrada con costo.

## Relaciones

- Categoría 1:N producto; proveedor 1:N compras y opcionalmente productos/equipos.
- Compra 1:N detalles; cada detalle referencia un producto.
- Venta 1:N detalles; cada detalle referencia exactamente un producto o servicio.
- Producto 1:N movimientos; cada movimiento tiene usuario y, según su tipo, compra o venta.
- Usuario N:M rol mediante usuario_rol; rol N:M permiso mediante rol_permiso.
- Usuario 1:N compras, ventas, movimientos, gastos, aportes e inversiones.

Se respetan las claves foráneas, `CHECK`, precisión decimal y `ENUM` originales. No hay tablas de framework, migraciones automáticas ni columnas añadidas. Cualquier cambio de columna requiere actualizar el SQL, el esquema H2, los VO/DAO y las pruebas antes de modificar los servicios.
