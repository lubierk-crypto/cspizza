package com.uniremington;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion de gestion CSPizza.
 * <p>
 * Spring Boot configura el contexto a partir del paquete {@code com.uniremington}
 * e inicia el servidor web embebido.
 */
@SpringBootApplication
public class CspizzaApplication {

	/** Inicia el contexto de Spring y el servidor web de la aplicacion. */
	public static void main(String[] args) {
		SpringApplication.run(CspizzaApplication.class, args);
	}

}
