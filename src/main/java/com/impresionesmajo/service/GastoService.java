package com.impresionesmajo.service;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
@Service
public class GastoService extends FinanzasService {
    public GastoService(JdbcTemplate jdbc,Acceso acceso){super(jdbc,acceso,new Definicion("gastos","gasto","Gastos","GASTOS_GESTIONAR","concepto","valor","fecha","metodo_pago","REGISTRADO","ANULADO"));}
}

