package com.impresionesmajo.web;

import java.beans.PropertyEditorSupport;
import com.impresionesmajo.service.Reglas;

/** Solo para campos monetarios; las cantidades no interpretan puntos como miles. */
public class ImporteEditor extends PropertyEditorSupport {
    @Override public void setAsText(String text) {
        setValue(text==null || text.isBlank()?null:Reglas.importe(text));
    }
}
