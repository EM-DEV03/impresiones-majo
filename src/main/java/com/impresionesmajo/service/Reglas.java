package com.impresionesmajo.service;

import java.math.*;
import java.util.Set;

/** Validaciones compartidas para entradas HTTP y operaciones de negocio. */
public final class Reglas {
    private Reglas() {}
    /** Rechaza texto vacío y limita su longitud después de aplicar trim. */
    public static String texto(String value, int max, String nombre) {
        if (value == null || value.isBlank() || value.trim().length()>max)
            throw new IllegalArgumentException(nombre + ": obligatorio, máximo " + max + " caracteres.");
        return value.trim();
    }
    /** Valida signo, escala y precisión sin redondear silenciosamente la entrada. */
    public static BigDecimal decimal(BigDecimal value, int scale, boolean positivo, String nombre) {
        if(value==null || value.signum()<0 || (positivo && value.signum()==0))
            throw new IllegalArgumentException(nombre + ": valor " + (positivo ? "mayor que cero." : "no negativo."));
        try {
            value=value.setScale(scale, RoundingMode.UNNECESSARY);
            if(value.precision()>14) throw new ArithmeticException();
            return value;
        } catch(ArithmeticException ex) { throw new IllegalArgumentException(nombre + ": máximo " + scale + " decimales y 14 dígitos."); }
    }
    /** Valida un importe COP con dos decimales como máximo. */
    public static BigDecimal dinero(BigDecimal value) { return decimal(value,2,false,"Importe"); }
    /** Valida cantidades enteras almacenadas en columnas DECIMAL(14,3). */
    public static BigDecimal entero(BigDecimal value,boolean positivo,String nombre) {
        value=decimal(value,3,positivo,nombre);
        if(value.stripTrailingZeros().scale()>0)
            throw new IllegalArgumentException(nombre+": debe ser un número entero "+(positivo?"mayor que cero":"no negativo")+".");
        return value;
    }
    /** Valida una cantidad positiva para compras, ventas y ajustes. */
    public static BigDecimal cantidad(BigDecimal value) { return entero(value,true,"Cantidad"); }
    /** Punto de extensión para las cantidades de venta; hoy comparte la misma regla. */
    public static BigDecimal cantidadVenta(BigDecimal value) { return cantidad(value); }
    /** Entrada COP: 1300 y 1.300 son mil trescientos; la coma separa centavos. */
    public static BigDecimal importe(String texto) {
        if(texto==null || texto.isBlank()) throw new IllegalArgumentException("El importe es obligatorio.");
        String raw=texto.trim();
        if(raw.length()>32) throw new IllegalArgumentException("Importe demasiado largo.");
        String normalizado;
        if(raw.matches("(?:[0-9]+|[1-9][0-9]{0,2}(?:\\.[0-9]{3})+)(?:,[0-9]{1,2})?"))
            normalizado=raw.replace(".","").replace(',','.');
        else if(raw.matches("[0-9]+\\.[0-9]{1,2}"))
            normalizado=raw; // Compatibilidad con valores decimales sin agrupar.
        else throw new IllegalArgumentException("Importe no válido. Escribe 1300, 1.300 o 1.300,50.");
        return dinero(new BigDecimal(normalizado));
    }
    /** Redondea valores calculados, nunca valores recibidos del formulario. */
    public static BigDecimal redondear(BigDecimal value) { return dinero(value.setScale(2,RoundingMode.HALF_UP)); }
    /** Valida que un valor pertenezca a un conjunto cerrado de opciones. */
    public static String opcion(String value, String... allowed) {
        if(value==null || !Set.of(allowed).contains(value)) throw new IllegalArgumentException("Opción no válida.");
        return value;
    }
    /** Valida los medios de pago definidos por el modelo de datos. */
    public static String pago(String value) { return opcion(value,"EFECTIVO","TRANSFERENCIA","TARJETA","OTRO"); }
}
