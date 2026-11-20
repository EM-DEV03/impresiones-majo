# Verificación

## Entorno

Windows, JDK 24.0.2 instalado, objetivo de compilación Java 17. Spring Boot 4.1.2 y Maven Wrapper 3.10.0 existentes, conservados. Paquete principal confirmado: com.impresionesmajo.

La implementación utiliza las 17 tablas existentes. El esquema de referencia y el esquema H2 admiten ahora SERVICIO en producto.tipo. La actualización de una base existente se realiza con la [migración manual](../database/2026-11-18-agregar-tipo-servicio.sql), sin reimportar el esquema completo.

## Pruebas

H2 en memoria, modo MySQL y esquema extraído del SQL. La preparación de datos verifica el nombre impresiones_majo_test antes de limpiar tablas de prueba. La prueba HTTP usa otra base efímera, impresiones_majo_http_test.

La suite verifica:

- Compra, descuento, costo ponderado, venta y reversión.
- Doble confirmación/anulación y rollback por stock insuficiente.
- Borradores sin movimientos y venta mixta con precios históricos.
- Precisión, cantidades, descuentos, pagos e inyección de campos.
- Ajustes sin stock negativo y ventas concurrentes por la última unidad.
- Finanzas/reportes con exclusión de anulados.
- Renderizado de pantallas y recursos Bootstrap.
- Formularios MVC, permisos, ventas propias, CSRF y BCrypt.
- Protección del último administrador, también ante cambios simultáneos, y datos sensibles en toString.
- Servidor HTTP real, cookies, tokens renderizados, login y logout.

## Límites de verificación

No están configuradas las credenciales de MySQL en el entorno de desarrollo. No se ha verificado la conexión real ni las particularidades de collation/bloqueos de InnoDB. H2 no sustituye esa comprobación.

Los listados de operaciones, movimientos y finanzas muestran los últimos 500 registros; los catálogos permiten buscar hasta 500 coincidencias. Los reportes agregan todo el intervalo seleccionado.

Consumo automático por servicio, anulación de compras, contabilidad fiscal y devoluciones parciales requieren ampliar el modelo y no se simulan.

## Resultado registrado

### Actualización del 20 de noviembre de 2026

Comando ejecutado: `.\mvnw.cmd -B verify`, con JAVA_HOME apuntando al JDK 24 instalado y objetivo de compilación Java 17. Resultado: **BUILD SUCCESS** y JAR ejecutable generado.

| Suite | Pruebas | Fallos | Errores | Omitidas |
|---|---:|---:|---:|---:|
| NegocioIntegrationTest | 38 | 0 | 0 | 0 |
| HttpSmokeTest | 1 | 0 | 0 | 0 |
| ImpresionesMajoApplicationTests | 1 | 0 | 0 | 0 |
| ProductoTipoMigracionTest | 1 | 0 | 0 | 0 |
| Total | 41 | 0 | 0 | 0 |

La suite incorpora las regresiones de presentación UNIDAD/PAQUETE, stock inicial, servicios desde Productos, venta y anulación sin movimientos físicos, exclusión de compras/ajustes y protección del historial al cambiar tipos. La prueba de migración comprueba en H2 la conservación de registros, el valor por defecto y la reejecución del ALTER. El editor no reportó errores.

Esta ejecución no conectó con MySQL/MariaDB ni aplicó la migración a la base del negocio. Su aplicación y la validación con el motor real siguen siendo pasos de la instalación descritos en [Servicios desde Productos](servicios-en-productos.md).

### Verificación anterior: 14 de noviembre de 2026

Verificación ejecutada el 14 de noviembre de 2026 después de documentar las capas, servicios y reglas de inventario. También se aisló la referencia del proveedor demo en la preparación de cada prueba para evitar dependencia del orden de ejecución.

Comando: `.\mvnw.cmd -B verify`. Resultado: **BUILD SUCCESS**.

| Suite | Pruebas | Fallos | Errores | Omitidas |
|---|---:|---:|---:|---:|
| NegocioIntegrationTest | 29 | 0 | 0 | 0 |
| HttpSmokeTest | 1 | 0 | 0 | 0 |
| ImpresionesMajoApplicationTests | 1 | 0 | 0 | 0 |
| Total | 31 | 0 | 0 | 0 |

Ventas, compras, ajustes y stock mínimo rechazan nuevas cantidades fraccionarias. Se comprobó que los detalles y existencias enteras se presentan sin ceros decimales, conservando las fracciones históricas sin redondearlas. Los formularios procesan correctamente 1300, 1.300 y 1.300,50 en precios, costos de varias líneas de compra, descuentos, efectivo recibido, ajustes y gastos. Los campos monetarios son de texto con teclado decimal, sin flechas de incremento.

Las pruebas adicionales verifican eliminación sin uso en los cuatro catálogos, rechazo de eliminación con referencias o existencias, conservación del inventario al desactivar/reactivar, renderizado de botones, permisos de gestión y CSRF. Solo se borraron registros de las bases de prueba en memoria; no se ejecutaron eliminaciones en MySQL.

Se generó el JAR ejecutable `target/impresiones-majo-0.0.1-SNAPSHOT.jar`. Se inspeccionó su contenido: incluye Bootstrap y MySQL Connector/J; no incluye JPA, Hibernate, H2 ni recursos de pruebas. El editor no reportó errores y el script de inicio pasó la comprobación sintáctica de PowerShell.

La prueba HTTP inició Tomcat en un puerto temporal y verificó rechazo de contraseña incorrecta, login correcto, páginas protegidas, recursos estáticos, cookies, CSRF y logout. No se dejó un servidor de prueba ejecutándose.

El pendiente de entorno es conectar con la base MySQL real mediante las credenciales locales; no es una prueba cubierta por H2.
