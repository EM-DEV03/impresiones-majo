package com.impresionesmajo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/** Comprueba el ALTER sobre datos previos en H2; nunca conecta con la base real. */
class ProductoTipoMigracionTest {
    @Test void ampliacionDelEnumConservaDatosYAdmiteReejecucion() throws Exception {
        String script=Files.readString(Path.of("database/2026-11-18-agregar-tipo-servicio.sql"));
        String alter=script.substring(script.indexOf("ALTER TABLE producto"));
        try(var connection=DriverManager.getConnection("jdbc:h2:mem:migracion_tipo;MODE=MySQL","sa","");
            var statement=connection.createStatement()) {
            statement.execute("CREATE TABLE producto (id BIGINT PRIMARY KEY,codigo VARCHAR(60),tipo ENUM('MERCANCIA','INSUMO') NOT NULL DEFAULT 'MERCANCIA',stock_actual DECIMAL(14,3),precio_venta DECIMAL(14,2))");
            statement.execute("INSERT INTO producto VALUES (1,'MERCANCIA-PREVIA','MERCANCIA',3,1600),(2,'INSUMO-PREVIO','INSUMO',9,200)");
            statement.execute(alter);
            statement.execute(alter);
            try(var result=statement.executeQuery("SELECT codigo,tipo,stock_actual,precio_venta FROM producto ORDER BY id")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString("codigo")).isEqualTo("MERCANCIA-PREVIA");
                assertThat(result.getString("tipo")).isEqualTo("MERCANCIA");
                assertThat(result.getBigDecimal("stock_actual")).isEqualByComparingTo("3");
                assertThat(result.getBigDecimal("precio_venta")).isEqualByComparingTo("1600");
                assertThat(result.next()).isTrue();
                assertThat(result.getString("codigo")).isEqualTo("INSUMO-PREVIO");
                assertThat(result.getString("tipo")).isEqualTo("INSUMO");
                assertThat(result.getBigDecimal("stock_actual")).isEqualByComparingTo("9");
                assertThat(result.getBigDecimal("precio_venta")).isEqualByComparingTo("200");
                assertThat(result.next()).isFalse();
            }
            statement.execute("INSERT INTO producto VALUES (3,'SERVICIO-PRUEBA','SERVICIO',0,500)");
            statement.execute("INSERT INTO producto(id,codigo,stock_actual,precio_venta) VALUES (4,'TIPO-POR-DEFECTO',0,100)");
            try(var result=statement.executeQuery("SELECT tipo FROM producto WHERE id=4")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString(1)).isEqualTo("MERCANCIA");
            }
        }
    }
}
