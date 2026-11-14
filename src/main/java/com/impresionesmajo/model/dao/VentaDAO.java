package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.VentaVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VentaDAO extends BaseDAO<VentaVO> {
    public VentaDAO(JdbcTemplate jdbc) {
        super(jdbc, "venta", VentaVO.class, "id,usuario_id,fecha,estado,metodo_pago,subtotal,descuento,total,valor_recibido,cambio,observaciones,fecha_confirmacion,fecha_anulacion,motivo_anulacion");
    }
}