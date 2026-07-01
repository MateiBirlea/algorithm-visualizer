package com.example.explanationengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.explanationengine.config.OllamaProperties;

@SpringBootApplication
@EnableConfigurationProperties(OllamaProperties.class)
public class ExplanationEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExplanationEngineApplication.class, args);
	}

}
