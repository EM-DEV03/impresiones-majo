# Reglas de negocio

Este documento describe las invariantes que deben mantenerse al cambiar servicios, controllers o el esquema. Las validaciones de entrada están centralizadas en `Reglas`; las reglas que dependen de la base se ejecutan dentro de los servicios transaccionales.

## Dinero y catálogos

- BigDecimal para dinero y cantidades, conservando el esquema SQL. Importes: hasta dos decimales; las cantidades nuevas y el stock mínimo deben ser enteros. Se respeta la precisión de las columnas originales.
- Cantidades positivas, costos/precios/descuentos no negativos. Subtotal = cantidad × precio redondeado HALF_UP a dos decimales. Total = suma de subtotales − descuento.
- Ventas, compras y ajustes requieren cantidades enteras positivas; el stock mínimo admite cero. La base conserva sus columnas DECIMAL; los detalles muestran 1 en lugar de 1.000. No se redondean ni modifican fracciones históricas; al anular una venta anterior se devuelve exactamente lo vendido.
- Los campos monetarios no tienen flechas de incremento. Admiten 1300 o 1.300 para mil trescientos pesos, y 1.300,50 para incluir centavos. La interfaz agrupa miles con punto y separa centavos con coma. Se mantienen compatibles los valores sin agrupación con punto decimal, como 1300.50; tres cifras después de un punto se interpretan como agrupación de miles.
- El descuento no supera el subtotal. En efectivo, el recibido cubre el total; cambio = recibido − total. Otros medios no generan cambio.
- Productos y servicios distintos por línea; repetir artículo exige reunir las cantidades.
- Productos, categorías, proveedores y servicios se pueden activar/desactivar desde su listado. La desactivación conserva sus existencias y referencias históricas.
- Eliminar requiere permiso de gestión y solo se permite para registros sin referencias, incluidos borradores. Los productos además deben tener stock cero. Las claves foráneas impiden borrar registros usados; no se eliminan movimientos ni documentos en cascada.
- Nuevos productos físicos empiezan en stock/costo cero según defaults. El alta permite existencias iniciales con costo y permiso STOCK_AJUSTAR: se registran mediante un ajuste de entrada auditado dentro de la misma transacción. El mínimo de reposición no agrega stock; las ediciones del catálogo no modifican directamente el saldo.
- MERCANCIA y SERVICIO activos se venden directamente. INSUMO se compra y consume mediante ajustes. Los registros SERVICIO no tienen proveedor, existencias iniciales ni mínimo de reposición; se excluyen de compras, ajustes y alertas físicas.
- No se puede cambiar entre producto físico y servicio si hay existencias, movimientos, compras o ventas, incluidos borradores. Tampoco se puede alternar UNIDAD y PAQUETE con existencias o historial. Debe crearse otro registro.

## Compras

El borrador exige proveedor y productos físicos activos y no altera inventario. Los registros SERVICIO se rechazan. Confirmar bloquea cabecera y productos, valida de nuevo y registra ENTRADA_COMPRA por línea.

Costo promedio = (stock anterior × costo anterior + cantidad × costo neto de entrada) / stock posterior. El descuento global se distribuye proporcionalmente en el costo de entrada; el promedio se conserva a dos decimales.

Stock, movimientos, estado CONFIRMADA y fecha se guardan en una transacción. Repetir confirmación se rechaza.

El bloqueo de la compra ocurre antes de leer sus líneas. Cada producto se bloquea antes de calcular el nuevo saldo, por lo que dos confirmaciones concurrentes no pueden sobrescribir el mismo stock.

**El SQL solo permite BORRADOR y CONFIRMADA.** No admite anulación ni devolución de compra. Los borradores se conservan. Un ajuste corrige existencias, pero no revierte el valor financiero de la compra. Esa función requiere ampliar explícitamente el modelo.

## Ventas

El borrador obtiene precios del catálogo y guarda descripción y precio históricos. No reserva stock. El precio queda fijado al crear el borrador; el costo histórico se actualiza al costo vigente cuando se confirma.

Cada línea identifica producto XOR servicio. Los nuevos servicios usan producto_id con producto.tipo=SERVICIO; el catálogo anterior mantiene servicio_id. Confirmar bloquea cabecera y productos, comprueba estado y disponibilidad física, descuenta mercancía y registra SALIDA_VENTA solo para artículos físicos. Los servicios no requieren stock ni generan movimientos al confirmar o anular. Los nuevos servicios tienen costo directo cero; los del catálogo anterior conservan su costo estimado. Ninguno consume insumos automáticamente.

Si falla una línea, la transacción revierte todos los cambios, incluidos movimientos y detalles de costo. Las ventas concurrentes compiten por los mismos bloqueos; no pueden consumir la misma última unidad.

El costo de cada línea se actualiza al confirmar la venta para conservar el costo vigente en el momento de la salida, sin cambiar el precio histórico cobrado.

## Anulación

- Venta: requiere VENTAS_ANULAR y motivo de hasta 470 caracteres. Una venta confirmada devuelve mercancía mediante REVERSION_VENTA, incluso si el producto ahora está inactivo. Reincorpora su costo histórico al promedio ponderado.
- Borrador de venta: se anula sin movimientos.
- Se conserva cabecera y detalle, estado ANULADA, fecha y motivo. El usuario anulador se incluye en el motivo porque no existe columna para él.
- Las segundas confirmaciones/anulaciones se rechazan. Bloqueos evitan efectos duplicados.
- Gastos, capital e inversiones: anulación lógica con motivo, usuario y fecha, mediante actualización condicional de estado.

## Ajustes

Requieren STOCK_AJUSTAR, producto activo, cantidad positiva y motivo. Las entradas también requieren costo unitario. Las salidas cubren pérdidas, conteos y consumo de insumos. Nunca se permite stock negativo.

No existe receta servicio-insumo ni conversión resma-hoja. El consumo de tinta y papel debe registrarse manualmente con una salida auditada. Todas las operaciones conservan usuario y stock anterior/posterior.

## Finanzas y reportes

Gastos, aportes e inversiones requieren importe positivo y fecha no futura. La vida útil, cuando se informa, es un entero positivo. Aportes son financiación; inversiones son equipos, no gastos operativos. No se calcula depreciación contable.

Los reportes aceptan intervalos inclusivos de hasta cinco años. Ventas y compras se agrupan por confirmación; finanzas por fecha del registro/adquisición. Se excluyen borradores y anulados por su **estado actual**: una anulación posterior puede cambiar un reporte de meses anteriores.

- Ventas netas = totales confirmados.
- Costo vendido estimado = cantidad × costo histórico de detalle.
- Margen bruto estimado = ventas netas − costo vendido.
- Resultado operativo estimado = margen bruto − gastos vigentes.
- Flujo neto de operaciones vigentes = ventas + aportes − compras − gastos − inversiones.
- Valor del inventario = stock actual × costo promedio actual; no es valoración histórica.

El costo de los servicios del catálogo anterior es estimado; el de los servicios creados desde Productos es cero porque su formulario no solicita costo. Esto limita la interpretación del margen. Mermas y ajustes no se incorporan automáticamente como gasto adicional. Estos indicadores sirven para gestión, no sustituyen contabilidad fiscal ni un libro histórico de caja.

## Seguridad

BCrypt factor 12, CSRF en formularios, cierre por POST, sesión de 30 minutos, cookies HttpOnly/SameSite, texto escapado por Thymeleaf y consultas parametrizadas. Identificadores SQL dinámicos solo proceden de metadatos internos.

No se puede desactivar la propia cuenta administradora ni quitarse su rol. Cambios de usuarios se serializan y deben conservar al menos un administrador activo. VO.toString no expone datos personales, contraseñas ni hashes.

Las operaciones sensibles se protegen en dos niveles: la cadena de filtros de Spring Security exige una sesión y el servicio consulta el permiso vigente con `Acceso`. Por eso una modificación de roles o estado de cuenta afecta también a una sesión ya abierta.
