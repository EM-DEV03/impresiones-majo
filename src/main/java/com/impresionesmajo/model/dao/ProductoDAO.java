package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.ProductoVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductoDAO extends BaseDAO<ProductoVO> {
    public ProductoDAO(JdbcTemplate jdbc) {
        super(jdbc, "producto", ProductoVO.class, "id,categoria_id,proveedor_preferido_id,codigo,nombre,descripcion,tipo,unidad_medida,costo_promedio,precio_venta,stock_actual,stock_minimo,activo,fecha_creacion,fecha_actualizacion");
    }
}