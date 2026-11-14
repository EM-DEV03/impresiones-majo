package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.GastoVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GastoDAO extends BaseDAO<GastoVO> {
    public GastoDAO(JdbcTemplate jdbc) {
        super(jdbc, "gasto", GastoVO.class, "id,usuario_id,categoria,concepto,descripcion,valor,metodo_pago,fecha,estado,motivo_anulacion,fecha_anulacion");
    }
}