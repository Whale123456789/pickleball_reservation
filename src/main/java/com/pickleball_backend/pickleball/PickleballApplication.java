// PickleballApplication.java
package com.pickleball_backend.pickleball;

import com.pickleball_backend.pickleball.service.FileStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import java.io.File;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.pickleball_backend.pickleball.config",
		"com.pickleball_backend.pickleball.controller",
		"com.pickleball_backend.pickleball.service",
		"com.pickleball_backend.pickleball.security",
		"com.pickleball_backend.pickleball.repository"
})
public class PickleballApplication {
	public static void main(String[] args) {
		SpringApplication.run(PickleballApplication.class, args);
	}


	// Add initialization for uploads directory
	@Bean
	CommandLineRunner init(FileStorageService storageService) {
		return (args) -> {
			storageService.init();
			new File("uploads").mkdirs(); // Ensure directory exists
		};
	}
}