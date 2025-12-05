package com.example.revHubBack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class RevHubBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(RevHubBackApplication.class, args);
	}

}
