# Impresiones Majo

Aplicación web para la operación de una papelería con servicios de impresión. El sistema administra catálogo, proveedores, compras, ventas, existencias, ajustes de inventario, gastos, aportes de capital, inversiones y reportes básicos.

El backend está construido con Spring Boot 4.1.2, Java 17+, Spring MVC, Thymeleaf, Spring JDBC y Spring Security. La aplicación no usa JPA ni Hibernate. El paquete raíz es `com.impresionesmajo`.

## Alcance técnico

La base de datos es parte del contrato de la aplicación. [database/impresiones-majo.sql](database/impresiones-majo.sql) contiene el esquema de las 17 tablas, las claves foráneas, restricciones, roles, permisos y datos demo. No hay migraciones automáticas: al arrancar, Spring tiene `spring.sql.init.mode=never` y la aplicación no crea ni altera tablas.

La persistencia se implementa con `JdbcTemplate` y DAO explícitos. Los objetos `VO` representan las tablas y los servicios contienen las reglas de negocio y las transacciones. Las operaciones que afectan existencias bloquean los registros necesarios con `SELECT ... FOR UPDATE` antes de recalcular el saldo.

Flujo general de una petición:

```text
HTTP/Thymeleaf
    -> Spring Security
    -> Controller
    -> Service transaccional
    -> DAO/JdbcTemplate
    -> MySQL o MariaDB
```

## Módulos

- Catálogos: categorías, proveedores, productos y servicios.
- Compras: borradores con varias líneas y confirmación de entrada a inventario.
- Ventas: productos y servicios en una misma operación, descuentos, medios de pago, cambio y comprobante interno.
- Inventario: existencias actuales, mínimos, movimientos y ajustes auditados.
- Finanzas: gastos, aportes de capital e inversiones en equipos.
- Reportes: resumen financiero, ventas por día, artículos con más ingresos y exportación CSV.
- Usuarios: cuentas, roles, activación, cambio de contraseña y permisos.

Los roles definidos por el SQL son `ADMINISTRADOR` y `VENDEDOR`. Los permisos se resuelven desde `usuario_rol` y `rol_permiso` en cada autenticación y también se validan en los servicios. Un vendedor opera y consulta sus ventas; la administración de catálogo, compras, inventario, finanzas, reportes y usuarios requiere los permisos correspondientes.

## Inventario y reglas de negocio

`producto.stock_actual` y `producto.costo_promedio` son el saldo operativo. No deben editarse directamente en MySQL.

- Los productos nuevos se registran como `UNIDAD` o `PAQUETE`. El stock, los mínimos, los costos y los precios corresponden a esa presentación: vender 2 paquetes descuenta 2 paquetes completos. No hay conversión automática a piezas sueltas.
- El formulario de producto permite registrar la cantidad inicial real y su costo. El mínimo de reposición solo activa una alerta; no agrega existencias. Las descripciones antiguas se conservan, aunque ya no se solicitan en el formulario de producto.
- Productos, inventario y nueva venta consultan el stock al volver a la pestaña y cada 15 segundos mientras está visible. Si falla la consulta, la interfaz avisa que los valores pueden estar desactualizados. La confirmación siempre valida el saldo en la base de datos.
- Una presentación `UNIDAD` o `PAQUETE` con existencias o historial no puede cambiarse por la otra. Para comercializar otra presentación se crea otro producto. Los registros antiguos con otras medidas requieren elegir explícitamente su equivalente al editarlos; sus cantidades no se convierten ni se alteran automáticamente.
- Un borrador de compra no modifica existencias.
- Al confirmar una compra se registra un movimiento `ENTRADA_COMPRA` por cada línea y se recalcula el costo promedio ponderado.
- Al confirmar una venta se registra `SALIDA_VENTA` para cada producto físico. Los registros de tipo `SERVICIO` se venden sin existencias y no generan movimientos de inventario ni consumo automático de insumos.
- Una venta confirmada puede anularse con permiso y motivo; la devolución se registra como `REVERSION_VENTA`.
- Las entradas y salidas manuales se registran como `AJUSTE_ENTRADA` o `AJUSTE_SALIDA` y siempre requieren motivo. Las entradas requieren costo unitario.
- No se permite stock negativo. Las cantidades nuevas de compras, ventas, ajustes y stock mínimo son enteras.
- Los importes se manejan con `BigDecimal` y hasta dos decimales. En la interfaz, `1300` y `1.300` representan COP 1.300; `1.300,50` representa COP 1.300,50.

Las compras confirmadas no tienen anulación porque el modelo solo contempla `BORRADOR` y `CONFIRMADA`. Tampoco existe una receta que relacione servicios con papel o tinta; el consumo de insumos se registra mediante ajustes auditados.

## Requisitos

- JDK 17 o superior.
- MySQL 8.0.16+ o MariaDB 10.4+ con InnoDB.
- PowerShell en Windows para usar los scripts incluidos.
- Navegador actual con JavaScript.
- Un usuario de base de datos con permisos de lectura, inserción y actualización. Para eliminar catálogos sin referencias también se necesita `DELETE` sobre las tablas correspondientes. No se necesitan permisos DDL.

## Base de datos requerida

La base de datos del proyecto ya está creada y se llama `impresiones-majo`. El archivo `impresiones-majo.sql` contiene la definición de ese esquema; no es el nombre que se debe escribir en la conexión.

Para ejecutar el sistema no hay que crear la base ni importar el SQL otra vez. Solo se necesita:

1. Iniciar el servidor MySQL/MariaDB.
2. Comprobar que exista la base `impresiones-majo` con las 17 tablas del proyecto.
3. Usar un usuario con permisos de lectura, inserción y actualización.
4. Ejecutar el script de inicio indicado abajo.

La aplicación no crea tablas, no ejecuta migraciones y no carga el archivo SQL al arrancar. [database/impresiones-majo.sql](database/impresiones-majo.sql) se conserva como referencia del esquema y como respaldo para preparar una instalación completamente nueva fuera de este entorno. No debe importarse sobre la base existente sin revisar antes sus datos.

Para actualizar una instalación anterior al tipo `SERVICIO`, realiza un respaldo, detén la aplicación y aplica [2026-11-18-agregar-tipo-servicio.sql](database/2026-11-18-agregar-tipo-servicio.sql) con una cuenta autorizada para `ALTER`. Conserva las 17 tablas y los datos existentes. Después inicia la versión actualizada. Consulta [Servicios desde Productos](docs/servicios-en-productos.md) para el procedimiento y sus límites; publicar el código en GitHub no ejecuta esta migración.

## Ejecución local

El script recomendado solicita las credenciales sensibles de forma interactiva, las mantiene solo durante el proceso Java y restaura esas variables al terminar:

```powershell
.\scripts\Start-ImpresionesMajo.ps1
```

Si la política de ejecución de PowerShell bloquea el script, el bypass puede limitarse a esa ejecución:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Start-ImpresionesMajo.ps1
```

La aplicación queda disponible en `http://localhost:8080` o en el puerto definido por `PORT`. Se detiene con `Ctrl+C`.

## Configuración

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_USERNAME` | Usuario de MySQL/MariaDB | Obligatoria |
| `DB_PASSWORD` | Contraseña de la base | Vacía |
| `DB_HOST` | Servidor de la base | `localhost` |
| `DB_PORT` | Puerto de la base | `3306` |
| `DB_NAME` | Base existente | `impresiones-majo` |
| `APP_ADMIN_USERNAME` | Usuario del administrador inicial | Sin aprovisionamiento |
| `APP_ADMIN_PASSWORD` | Contraseña del administrador inicial | Sin aprovisionamiento |
| `APP_ADMIN_NAME` | Nombre del administrador inicial | `Administrador` |
| `PORT` | Puerto HTTP | `8080` |

Las propiedades de conexión están en [application.properties](src/main/resources/application.properties) y se alimentan de variables de entorno. No se carga ningún archivo `.env`. El script de inicio solicita temporalmente las credenciales y restaura las variables al terminar.

`APP_ADMIN_USERNAME` y `APP_ADMIN_PASSWORD` solo se usan si `usuario` está vacía. El arranque no cambia cuentas existentes. La contraseña debe tener al menos 12 caracteres y no superar 72 bytes UTF-8; se almacena con BCrypt factor 12.

Con variables ya configuradas también puedes ejecutar `.\mvnw.cmd spring-boot:run`. Para detener, usa **Ctrl+C** en la terminal.

## Pruebas y empaquetado

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

Las pruebas usan H2 en memoria en modo MySQL y un esquema derivado de [database/impresiones-majo.sql](database/impresiones-majo.sql). H2 solo se activa durante `test`/`verify`: no se usa al ejecutar la aplicación con `spring-boot:run` ni con el JAR. La aplicación normal se conecta a MySQL/MariaDB mediante `impresiones-majo` y no toca la base H2. La suite cubre reglas de compras y ventas, concurrencia de stock, anulaciones, finanzas, permisos, CSRF, BCrypt, renderizado MVC y un arranque HTTP real.

Bootstrap se sirve desde WebJars (`/webjars/bootstrap/5.3.3`), por lo que la aplicación no depende de un CDN.

El JAR ejecutable se genera en `target/impresiones-majo-0.0.1-SNAPSHOT.jar`:

```powershell
java -jar target/impresiones-majo-0.0.1-SNAPSHOT.jar
```

El JAR necesita las mismas variables de conexión que `spring-boot:run`.

## Scripts de desarrollo

- [Start-ImpresionesMajo.ps1](scripts/Start-ImpresionesMajo.ps1): inicia la aplicación y gestiona las credenciales interactivas.
- [Generate-Persistence.ps1](scripts/Generate-Persistence.ps1): genera los VO y DAO a partir de las tablas del SQL. Revisar los archivos generados antes de ejecutar el script sobre cambios de esquema.
- [Generate-TestSchema.ps1](scripts/Generate-TestSchema.ps1): genera el esquema H2 de pruebas a partir del SQL. No ejecuta nada contra MySQL.

## Estructura del código

```text
database/                         SQL de referencia
src/main/java/com/impresionesmajo/
  model/vo/                        objetos de valor por tabla
  model/dao/                       DAO y BaseDAO
  service/                         reglas, validación y transacciones
  security/                        autenticación, roles y permisos
  web/                             controllers, errores y formato
src/main/resources/
  application.properties          configuración por entorno
  templates/                      vistas Thymeleaf
  static/css/                     estilos
  static/js/                      comportamiento de formularios
src/test/java/                     pruebas de integración, MVC y HTTP
src/test/resources/                configuración y esquema H2
docs/                              documentación funcional y técnica
```

## Documentación

- [Funcionamiento y puesta en marcha](docs/funcionamiento.md)
- [Requisitos y permisos](docs/requisitos.md)
- [Reglas de negocio](docs/reglas-negocio.md)
- [Modelo de datos](docs/modelo-datos.md)
- [Arquitectura y estructura](docs/estructura.md)
- [Verificación y límites](docs/verificacion.md)
- [Servicios desde Productos y actualización de la base](docs/servicios-en-productos.md)

## Límites conocidos

El comprobante es interno y no implementa facturación electrónica DIAN. El modelo actual tampoco contempla clientes, impuestos, créditos, devoluciones parciales, sucursales, depreciación contable ni consumo automático de insumos por servicio. Los listados operativos están limitados a los últimos 500 registros; los reportes agregan el intervalo completo seleccionado.
