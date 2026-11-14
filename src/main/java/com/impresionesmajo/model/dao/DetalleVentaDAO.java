package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.DetalleVentaVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DetalleVentaDAO extends BaseDAO<DetalleVentaVO> {
    public DetalleVentaDAO(JdbcTemplate jdbc) {
        super(jdbc, "detalle_venta", DetalleVentaVO.class, "id,venta_id,producto_id,servicio_id,descripcion_snapshot,cantidad,precio_unitario,costo_unitario_snapshot,subtotal");
    }
}