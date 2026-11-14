package com.impresionesmajo.web;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** Helpers de Thymeleaf para importes COP, cantidades y estados activos. */
@Component("formato")
public class WebConfig {
    /** Formatea un valor como moneda colombiana con sufijo COP. */
    public String cop(Object value) {
        var f=NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        f.setMinimumFractionDigits(0);f.setMaximumFractionDigits(2);
        return f.format(value==null?BigDecimal.ZERO:value)+" COP";
    }
    /** Normaliza las representaciones booleanas de MySQL y H2. */
    public boolean activo(Object value) {return Boolean.TRUE.equals(value) || "1".equals(String.valueOf(value));}
    /** Quita ceros decimales de cantidades mostradas en tablas. */
    public String cantidad(BigDecimal value) {
        return value==null?"":value.stripTrailingZeros().toPlainString();
    }
    /** Formatea importes sin modificar el valor almacenado. */
    public String importe(Object value) {
        var f=NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"));
        f.setMinimumFractionDigits(0);f.setMaximumFractionDigits(2);
        return f.format(value==null?BigDecimal.ZERO:value);
    }
}
