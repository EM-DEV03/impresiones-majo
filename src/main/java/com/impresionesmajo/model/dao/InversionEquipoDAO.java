package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.InversionEquipoVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InversionEquipoDAO extends BaseDAO<InversionEquipoVO> {
    public InversionEquipoDAO(JdbcTemplate jdbc) {
        super(jdbc, "inversion_equipo", InversionEquipoVO.class, "id,usuario_id,proveedor_id,equipo,descripcion,fecha_adquisicion,costo,metodo_pago,vida_util_meses,estado,motivo_anulacion,fecha_anulacion");
    }
}