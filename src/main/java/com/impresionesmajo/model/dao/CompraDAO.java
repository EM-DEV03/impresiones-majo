package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.CompraVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CompraDAO extends BaseDAO<CompraVO> {
    public CompraDAO(JdbcTemplate jdbc) {
        super(jdbc, "compra", CompraVO.class, "id,proveedor_id,usuario_id,fecha,estado,metodo_pago,subtotal,descuento,total,observaciones,fecha_confirmacion");
    }
}