package com.visulaw.legal_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class LegalServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LegalServiceApplication.class, args);
	}

}
