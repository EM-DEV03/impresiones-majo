# Requisitos y permisos

## Funcionalidad

| Módulo | Operaciones |
|---|---|
| Acceso | Inicio/cierre de sesión, BCrypt, estado y permisos |
| Dashboard | Ventas del día, catálogo activo, alertas y ventas recientes |
| Categorías y proveedores | Crear, editar, activar/desactivar y eliminar si no tienen relaciones |
| Productos | Categoría, proveedor, código, unidad, tipo, precio y mínimo |
| Servicios | Impresión, copia, escaneo, ampliación y otros; tarifas y costo estimado |
| Compras | Borrador con varias líneas, revisión y confirmación |
| Ventas | Mercancía y servicios, descuento, pago, cambio, confirmación y anulación |
| Inventario | Existencias, mínimos, movimientos y ajustes auditados |
| Gastos | Registrar y anular con motivo |
| Capital | Registrar aportes y origen; anular con motivo |
| Inversiones | Equipos, proveedor, costo, fecha y vida útil; anular |
| Reportes | Resumen financiero, ventas diarias, artículos por ingresos y CSV |
| Usuarios | Crear cuentas, asignar roles, activar/desactivar y restablecer contraseña |

## Roles y permisos

**ADMINISTRADOR:** todos los permisos definidos por los datos de referencia del SQL. Gestiona catálogos, stock, compras, ventas, anulaciones, finanzas, reportes y usuarios.

**VENDEDOR:** DASHBOARD_VER, PRODUCTOS_VER, SERVICIOS_VER, VENTAS_CREAR y VENTAS_VER_PROPIAS. Solo ve sus ventas y su facturación; no modifica catálogos, ajusta stock, anula ni administra finanzas.

Los permisos se leen de usuario_rol y rol_permiso. Se comprueban contra la base al atender operaciones: la revocación de un rol o desactivación afecta también a sesiones abiertas. La interfaz asigna roles existentes; no modifica la matriz de permisos del SQL.

## Contrato de permisos

Los permisos se validan por código dentro de los servicios. Los nombres no deben cambiarse sin actualizar el SQL de referencia y las pruebas:

| Permiso | Uso principal |
|---|---|
| `DASHBOARD_VER` | Dashboard |
| `PRODUCTOS_VER`, `MOVIMIENTOS_VER` | Catálogo e inventario |
| `CATEGORIAS_GESTIONAR`, `PROVEEDORES_GESTIONAR`, `PRODUCTOS_GESTIONAR`, `SERVICIOS_GESTIONAR` | Catálogos |
| `COMPRAS_GESTIONAR` | Compras y confirmaciones |
| `VENTAS_CREAR`, `VENTAS_VER_PROPIAS`, `VENTAS_VER_TODAS`, `VENTAS_ANULAR` | Ventas |
| `STOCK_AJUSTAR` | Ajustes auditados |
| `GASTOS_GESTIONAR`, `CAPITAL_GESTIONAR`, `INVERSIONES_GESTIONAR` | Finanzas |
| `REPORTES_VER` | Reportes y CSV |
| `USUARIOS_GESTIONAR` | Usuarios, roles y contraseñas |

## Requisitos técnicos

- Java 17+; Maven Wrapper incluido.
- MySQL con InnoDB y el esquema ya importado. El SQL declara MySQL 8.0.16+ y MariaDB 10.4+; la aplicación usa MySQL Connector/J.
- Navegador actual con JavaScript; validación de negocio siempre en servidor.
- Usuario MySQL con SELECT, INSERT y UPDATE; DELETE sobre usuario_rol para cambiar roles y sobre producto, categoria, proveedor y servicio para eliminar registros sin uso. No se necesitan permisos DDL.
- UTF-8, interfaz española, COP y zona horaria America/Bogota.
- Bootstrap local y diseño adaptable.

El comprobante es interno; no implementa facturación electrónica DIAN. El esquema no modela clientes, impuestos, créditos, devoluciones parciales ni sucursales.

Las pruebas de autorización deben usar tanto el rol de Spring Security como una consulta real de permisos, porque `Acceso` comprueba el estado actual en la base en cada operación protegida.
