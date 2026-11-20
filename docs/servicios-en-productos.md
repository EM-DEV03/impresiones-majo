# Servicios desde el catálogo de productos

## Activar el cambio en la instalación existente

1. Detén la aplicación.
2. Abre la base `impresiones-majo` en tu administrador de MySQL/MariaDB.
3. Ejecuta únicamente [2026-11-18-agregar-tipo-servicio.sql](../database/2026-11-18-agregar-tipo-servicio.sql).
4. Inicia la versión actualizada y recarga la página del navegador.

El script amplía el campo `producto.tipo` para admitir `SERVICIO`. Conserva `MERCANCIA`, `INSUMO`, el valor por defecto y todos los registros. No crea servicios, cambia cantidades ni modifica precios. Es repetible. No se ejecuta automáticamente al iniciar la aplicación.

**No reimportes [impresiones-majo.sql](../database/impresiones-majo.sql) sobre la base existente.** Ese archivo es la referencia completa del esquema para instalaciones nuevas; el cambio de una instalación en uso se aplica con el script anterior.

## Registrar y vender un servicio

1. En Productos, selecciona Nuevo registro y elige el tipo **SERVICIO**.
2. Completa código único, nombre, categoría, presentación, precio de venta y estado.
3. Usa **Unidad** para cobrar un trabajo individual o **Paquete** para cobrar un conjunto completo. La cantidad de la venta cuenta esas presentaciones.
4. Guarda. Proveedor, stock inicial y mínimo de reposición no se solicitan para servicios.
5. En Nueva venta, selecciónalo en **Producto o servicio**, indica la cantidad, revisa el borrador y confirma.

Los servicios se editan y desactivan desde Productos. Se muestran como **Servicio · sin inventario**. Se pueden vender sin existencias y no aparecen en compras, ajustes, inventario físico ni alertas de reposición. La confirmación y anulación de sus ventas no generan movimientos de inventario.

El catálogo anterior de Servicios de impresión sigue disponible, con sus registros e historial. Sus opciones de venta se conservan. No es necesario trasladar o volver a crear esos registros.

## Historial y resultados

Las ventas nuevas de servicios se incluyen en ingresos, comprobantes, historial, estadísticas diarias, artículos destacados y exportación CSV. El panel cuenta los servicios nuevos junto con los del catálogo anterior.

No hay descuento automático de tinta o papel. El costo directo de los servicios nuevos se registra como cero porque este formulario no solicita un costo estimado; los márgenes del reporte deben interpretarse con esa limitación. Los servicios anteriores mantienen sus costos estimados.

No se puede cambiar entre artículo físico y servicio cuando el registro tiene stock, movimientos, compras o ventas, incluidos borradores. Esta protección impide reinterpretar el historial y las devoluciones. Se debe crear un registro diferente para la nueva finalidad. Las ediciones habituales de nombre, categoría, precio y estado siguen disponibles.

## Archivos del cambio

| Archivos | Cambio |
|---|---|
| [ProductoService.java](../src/main/java/com/impresionesmajo/service/ProductoService.java) | Nuevo tipo, campos físicos no aplicables y protección de cambios de tipo con historial. |
| [VentaService.java](../src/main/java/com/impresionesmajo/service/VentaService.java) | Venta y anulación de servicios sin stock, con ingresos e historial normales. |
| [CompraService.java](../src/main/java/com/impresionesmajo/service/CompraService.java) | Rechazo de servicios en compras y bloqueo consistente durante la creación. |
| [InventarioService.java](../src/main/java/com/impresionesmajo/service/InventarioService.java) | Exclusión del inventario físico y rechazo de movimientos para servicios. |
| [ReporteService.java](../src/main/java/com/impresionesmajo/service/ReporteService.java) | Exclusión de alertas y valoración física; conteo de servicios en el panel. |
| [CatalogoController.java](../src/main/java/com/impresionesmajo/web/CatalogoController.java), [OperacionController.java](../src/main/java/com/impresionesmajo/web/OperacionController.java), [InventarioController.java](../src/main/java/com/impresionesmajo/web/InventarioController.java) | Mensajes y opciones de venta, compra y actualización del catálogo. |
| [Formulario de producto](../src/main/resources/templates/catalogo/form.html), [lista del catálogo](../src/main/resources/templates/catalogo/lista.html), [formulario de operación](../src/main/resources/templates/operacion/form.html) | Tipo SERVICIO, campos físicos ocultos y etiquetas sin existencias. |
| [app.js](../src/main/resources/static/js/app.js), [app.css](../src/main/resources/static/css/app.css) | Activación de campos según tipo y actualización de servicios sin mostrar stock negativo. |
| [Migración SQL](../database/2026-11-18-agregar-tipo-servicio.sql), [esquema de referencia](../database/impresiones-majo.sql), [esquema H2](../src/test/resources/schema-test.sql) | Ampliación del ENUM sin nuevos registros ni tablas. |
| [NegocioIntegrationTest.java](../src/test/java/com/impresionesmajo/NegocioIntegrationTest.java), [ProductoTipoMigracionTest.java](../src/test/java/com/impresionesmajo/ProductoTipoMigracionTest.java) | Regresiones del nuevo flujo y conservación de datos al ampliar el ENUM. |
| Este documento | Instrucciones de actualización y uso. |

Las verificaciones automatizadas usan H2 en memoria y no acceden a la base del negocio. El ensayo del ALTER con H2 comprueba conservación de datos y reejecución, pero no sustituye una ejecución sobre MySQL/MariaDB.

Comando de verificación y empaquetado: `./mvnw.cmd -B verify`.
