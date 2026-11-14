package com.impresionesmajo.service;
import com.impresionesmajo.security.Acceso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
@Service
public class InversionService extends FinanzasService {
    public InversionService(JdbcTemplate jdbc,Acceso acceso){super(jdbc,acceso,new Definicion("inversiones","inversion_equipo","Inversiones en equipos","INVERSIONES_GESTIONAR","equipo","costo","fecha_adquisicion","metodo_pago","REGISTRADA","ANULADA"));}
}

