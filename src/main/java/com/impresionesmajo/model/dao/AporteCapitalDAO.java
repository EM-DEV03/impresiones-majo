package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.AporteCapitalVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AporteCapitalDAO extends BaseDAO<AporteCapitalVO> {
    public AporteCapitalDAO(JdbcTemplate jdbc) {
        super(jdbc, "aporte_capital", AporteCapitalVO.class, "id,usuario_id,origen,valor,metodo,fecha,observaciones,estado,motivo_anulacion,fecha_anulacion");
    }
}