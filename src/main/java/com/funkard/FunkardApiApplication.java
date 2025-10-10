package com.funkard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FunkardApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FunkardApiApplication.class, args);
	}

}
