package com.pi.agendamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class AgendamentoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgendamentoApplication.class, args);
	}

}
