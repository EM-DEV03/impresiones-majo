package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.DetalleCompraVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DetalleCompraDAO extends BaseDAO<DetalleCompraVO> {
    public DetalleCompraDAO(JdbcTemplate jdbc) {
        super(jdbc, "detalle_compra", DetalleCompraVO.class, "id,compra_id,producto_id,cantidad,costo_unitario,subtotal");
    }
}