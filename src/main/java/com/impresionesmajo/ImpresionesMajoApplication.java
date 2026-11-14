package com.impresionesmajo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Bootstrap de la aplicación y configuración de zona horaria del proceso. */
@SpringBootApplication
public class ImpresionesMajoApplication {

	/** Fija America/Bogota antes de crear el contexto de Spring. */
	public static void main(String[] args) {
		java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Bogota"));
		SpringApplication.run(ImpresionesMajoApplication.class, args);
	}

}
