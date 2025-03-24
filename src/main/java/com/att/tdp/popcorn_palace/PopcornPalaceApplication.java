package com.att.tdp.popcorn_palace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.att.tdp.popcorn_palace.model") // Explicitly scan the model package for entities
@EnableJpaRepositories(basePackages = "com.att.tdp.popcorn_palace.repository")  // Enable repository scanning
public class PopcornPalaceApplication {
	public static void main(String[] args) {
		SpringApplication.run(PopcornPalaceApplication.class, args);
	}
}
