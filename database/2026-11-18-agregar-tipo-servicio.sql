-- Impresiones Majo: admitir servicios en el catálogo de productos.
-- Ejecutar este archivo en la base existente antes de usar la nueva versión.
-- No importar nuevamente impresiones-majo.sql sobre el negocio en uso.
-- Solo amplía el ENUM conservando su orden, nulabilidad y valor por defecto.
-- No inserta servicios, no cambia cantidades ni elimina registros existentes.
-- Puede ejecutarse nuevamente: la definición resultante es la misma.
USE `impresiones-majo`;

ALTER TABLE producto
  MODIFY COLUMN tipo ENUM('MERCANCIA','INSUMO','SERVICIO') NOT NULL DEFAULT 'MERCANCIA';
