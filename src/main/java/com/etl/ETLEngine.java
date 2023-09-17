package com.etl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.storage")
@EnableJpaRepositories(basePackages = { "com.storage" })
public class ETLEngine {

	public static void main(String[] args) {
		SpringApplication.run(ETLEngine.class, args);
	}

}
