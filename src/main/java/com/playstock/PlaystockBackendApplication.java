package com.playstock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlaystockBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlaystockBackendApplication.class, args);
	}

}
