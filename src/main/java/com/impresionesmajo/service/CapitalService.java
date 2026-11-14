package com.impresionesmajo.service;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
@Service
public class CapitalService extends FinanzasService {
    public CapitalService(JdbcTemplate jdbc,Acceso acceso){super(jdbc,acceso,new Definicion("capital","aporte_capital","Aportes de capital","CAPITAL_GESTIONAR","origen","valor","fecha","metodo","REGISTRADO","ANULADO"));}
}

