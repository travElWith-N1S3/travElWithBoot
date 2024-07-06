package com.tour;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TourApplication {
	public static void main(String[] args) {
		SpringApplication.run(TourApplication.class, args);
	}
}
