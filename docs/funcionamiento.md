# Funcionamiento y ciclo de ejecución

## Inicio y conexión

La aplicación se conecta a la base existente `impresiones-majo`. El archivo `impresiones-majo.sql` contiene la definición del esquema. No hay que crear la base ni importar de nuevo el SQL para ejecutar el entorno actual.

Inicia MySQL y comprueba que `impresiones-majo` contiene las 17 tablas y los roles/permisos del SQL.

Desde PowerShell, ejecuta [Start-ImpresionesMajo.ps1](../scripts/Start-ImpresionesMajo.ps1). Solicita credenciales de forma oculta y las pasa temporalmente al proceso Java. Si tu cuenta MySQL no tiene contraseña, pulsa Enter en esa pregunta.

El primer administrador se crea solo si usuario está vacío y se proporcionan APP_ADMIN_USERNAME/PASSWORD. Si ya hay cuentas, el inicio no cambia sus contraseñas. Después del primer uso puedes omitir esas dos variables.

Abre http://localhost:8080. Detén el servidor con Ctrl+C. Para otro puerto, configura PORT. Las fechas se trabajan en America/Bogota; la sesión MySQL usa desplazamiento -05:00 sin requerir tablas de zonas horarias.

El arranque no ejecuta DDL ni carga migraciones. Si la conexión o una tabla requerida falla, la aplicación debe detenerse y el diagnóstico corresponde a la configuración de la base, no a una creación automática del esquema.

## Flujo de una operación

1. El controller recibe el formulario y limita los campos aceptados mediante `WebDataBinder` cuando corresponde.
2. El servicio comprueba permisos con `Acceso` y valida los valores con `Reglas`.
3. El servicio obtiene snapshots de catálogo para conservar precios y descripciones históricas.
4. Las operaciones transaccionales bloquean la cabecera y los productos necesarios antes de recalcular stock.
5. Se actualizan los documentos y movimientos de inventario en una sola transacción.
6. El controller responde con POST/Redirect/GET para evitar reenvíos del formulario.

## Preparar el negocio

1. Revisa categorías y proveedores; reemplaza los datos demostrativos.
2. Configura códigos, unidades, precios y mínimos de los productos.
3. Revisa las tarifas y costos estimados de impresión/copia/escaneo.
4. En Compras, agrega proveedor, productos, cantidades y costos.
5. Guarda el borrador, revisa y confirma: se incorporarán las existencias.
6. Registra aportes y equipos en sus módulos independientes.
7. Crea cuentas VENDEDOR en Usuarios.

## Activar, desactivar o eliminar catálogos

En Productos, Categorías, Proveedores y Servicios aparecen las acciones Editar, Activar/Desactivar y Eliminar para el administrador.

Desactivar conserva el registro y su historial; puedes activarlo nuevamente cuando lo necesites. Eliminar pide confirmación y borra definitivamente solo registros sin relaciones ni historial. Un producto con existencias tampoco se puede eliminar. Si la aplicación rechaza la eliminación por estar en uso, utiliza Desactivar.

## Vender productos y servicios

En Ventas → Nueva operación, agrega una línea por producto o servicio. Usa la tarifa/unidad configurada; una impresión de doble cara no multiplica caras automáticamente.

Selecciona pago, descuento y efectivo recibido cuando corresponda. Guarda el borrador y confirma después de revisarlo. Si falta stock, toda la confirmación se revierte. Puedes imprimir el comprobante interno.

El administrador puede anular con motivo, devolviendo la mercancía al inventario. Los vendedores solo consultan sus ventas y no pueden anular.

## Inventario y finanzas

Las cantidades de ventas, compras y ajustes se escriben en enteros: 1, 2, 3. El stock mínimo también es entero y puede ser cero. Los valores monetarios se escriben sin flechas: 1300 o 1.300 representan $1.300; para centavos utiliza 1.300,50. Al salir del campo se muestran los separadores de miles.

Consulta mínimos y movimientos en Inventario. Registra tinta/papel consumido, pérdidas y conteos como ajustes de salida con motivo. Las entradas requieren costo unitario. No cambies stock_actual directamente en HeidiSQL: perderías trazabilidad.

Una compra confirmada no se anula con el esquema actual. Un ajuste de stock tampoco revierte su impacto financiero.

Gastos registra costos operativos; Capital registra financiación; Inversiones registra equipos. En Reportes elige fechas y exporta CSV para Excel. Se utilizan estados actuales: anular hoy puede modificar el resumen de un mes anterior.

## Integración para desarrollo

La base oficial y el esquema H2 de pruebas deben mantenerse sincronizados. Si cambia [impresiones-majo.sql](../database/impresiones-majo.sql), ejecutar [Generate-TestSchema.ps1](../scripts/Generate-TestSchema.ps1), revisar el resultado y actualizar los VO/DAO con [Generate-Persistence.ps1](../scripts/Generate-Persistence.ps1) cuando aplique.

Las reglas de negocio se implementan en `service`; los controllers no deben duplicar validaciones ni escribir SQL de dominio. Para cambiar permisos, actualizar los datos de `rol_permiso` del SQL y agregar pruebas de acceso.

## Problemas habituales

| Mensaje / situación | Qué revisar |
|---|---|
| Java no disponible | JDK 17+, JAVA_HOME y nueva terminal |
| Descarga Maven falla | Conexión, proxy y Maven Central |
| Falta DB_USERNAME | Script de inicio o variables del proceso Java |
| Access denied MySQL | Usuario, contraseña y permisos |
| Communications link failure | MySQL iniciado, host y puerto |
| Unknown database / tabla faltante | Importación original; la app no crea el esquema |
| No existe cuenta para entrar | Aprovisionar primer administrador con tabla usuario vacía |
| Variable de admin no cambia contraseña existente | Otro administrador debe restablecerla en Usuarios |
| Duplicado | Corrige código, nombre o correo |
| Existencias insuficientes | Registra mercancía real por compra o ajuste justificado |
| Puerto 8080 ocupado | Cambia PORT antes del inicio |

Realiza respaldos con HeidiSQL antes de cambios administrativos en la base. No se implementan respaldos automáticos.
