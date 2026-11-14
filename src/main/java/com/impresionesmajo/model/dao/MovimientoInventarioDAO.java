package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.MovimientoInventarioVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MovimientoInventarioDAO extends BaseDAO<MovimientoInventarioVO> {
    public MovimientoInventarioDAO(JdbcTemplate jdbc) {
        super(jdbc, "movimiento_inventario", MovimientoInventarioVO.class, "id,producto_id,usuario_id,compra_id,venta_id,tipo,cantidad,stock_anterior,stock_posterior,motivo,fecha");
    }
}