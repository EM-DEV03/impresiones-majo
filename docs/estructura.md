# Estructura y arquitectura

```text
database/impresiones-majo.sql                 Referencia oficial existente
src/main/java/com/impresionesmajo/
  ImpresionesMajoApplication.java            Arranque y zona horaria
  model/vo/                            17 objetos, uno por tabla
  model/dao/                           17 DAO concretos y BaseDAO
  service/                             Reglas, validación y transacciones
  security/                            Sesiones, permisos y primer administrador
  web/                                 Controllers, errores y formato COP
src/main/resources/
  application.properties               Configuración por entorno
  templates/                           Thymeleaf y fragmentos
  static/css/app.css                    Diseño adaptable e impresión
  static/js/app.js                      Formularios, líneas y estimaciones
src/test/java/.../                     Pruebas de negocio, MVC y servidor HTTP
src/test/resources/                    Configuración y esquema H2 aislado
scripts/                               Inicio y generadores reproducibles
docs/                                  Documentación
```

Navegador → Spring Security → Controller → Service → JdbcTemplate/DAO → MySQL.

## Responsabilidades por capa

- `web`: recibe parámetros HTTP, prepara el modelo Thymeleaf y redirige después de una operación POST. No calcula totales ni modifica tablas de dominio.
- `service`: aplica permisos, valida entradas, calcula importes, decide transiciones de estado y delimita las transacciones con `@Transactional`.
- `model.dao`: encapsula las operaciones repetitivas de persistencia. `BaseDAO` cubre tablas con `id`; las tablas puente tienen DAO específicos.
- `model.vo`: representa columnas del esquema con tipos Java. No contiene reglas de negocio.
- `security`: carga credenciales, roles y permisos desde MySQL. `Acceso` vuelve a consultar el permiso vigente para que una desactivación tenga efecto en sesiones existentes.

Compras, ventas e inventario utilizan DAO tipados. BaseDAO comparte consulta, inserción, actualización y bloqueo. Las tablas de relaciones compuestas tienen DAO específicos. No hay método general de borrado de registros de negocio.

Los catálogos utilizan `CatalogoService` y servicios concretos para cada módulo; finanzas comparte `FinanzasService` con `GastoService`, `CapitalService` e `InversionService`. Su SQL utiliza columnas explícitas definidas por metadatos internos. Los reportes usan agregaciones parametrizadas.

Las operaciones atómicas se marcan `@Transactional`. Controllers llaman servicios inyectados para pasar por los proxies. `InventarioService.mover` es interno del paquete y requiere que su llamador ya tenga transacción y el producto bloqueado. Los métodos con reglas de negocio tienen Javadoc; los getters/setters generados no repiten comentarios sin lógica.

## Rutas

| Ruta | Función |
|---|---|
| /login y /logout | Acceso y cierre por POST |
| / | Dashboard |
| /catalogos/categorias, proveedores, productos, servicios | Listas, búsqueda, alta y edición |
| /compras y /ventas | Lista, nueva, detalle y confirmación |
| /ventas/{id}/anular | Anulación por POST |
| /inventario | Saldos y movimientos; /ajustar para POST |
| /finanzas/gastos, capital, inversiones | Registro y anulación |
| /reportes y /reportes/exportar | Indicadores y CSV |
| /usuarios | Gestión de acceso |

Se usa POST/Redirect/GET. Los errores de validación muestran una explicación y retorno al formulario. Las confirmaciones y anulaciones son transiciones protegidas contra repeticiones.

## Convenciones de desarrollo

- Las consultas usan parámetros JDBC; los nombres de tabla y columna dinámicos solo salen de definiciones internas.
- Las nuevas reglas deben validarse en el servicio y cubrirse con una prueba de integración H2.
- Las transiciones que modifican stock deben actualizar producto y movimiento dentro de la misma transacción.
- No se debe introducir una migración automática ni editar `stock_actual` desde un controller.

## Generadores

[Generate-Persistence.ps1](../scripts/Generate-Persistence.ps1) lee el SQL y genera VO y DAO concretos; no modifica BaseDAO ni la base. Revisar antes de ejecutarlo si se editaron archivos generados.

[Generate-TestSchema.ps1](../scripts/Generate-TestSchema.ps1) extrae CREATE TABLE e INSERT y elimina opciones físicas de motor/charset/collation para H2. Solo produce un recurso de pruebas; no ejecuta SQL contra MySQL.
