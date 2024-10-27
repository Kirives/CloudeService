package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;

@SpringBootApplication
public class Demo13Application {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Demo13Application.class);
        app.addInitializers((ConfigurableApplicationContext ctx) -> {
            ConfigurableEnvironment env = ctx.getEnvironment();
            try {
                // Указываем путь к нужному файлу properties
                env.getPropertySources().addFirst(
                        new ResourcePropertySource(new ClassPathResource("application.properties")));
            } catch (Exception e) {
                throw new RuntimeException("Failed to load custom properties file", e);
            }
        });
        app.run(args);
    }
}
