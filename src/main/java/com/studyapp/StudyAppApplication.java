package com.studyapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class StudyAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyAppApplication.class, args);
        System.out.println("=================================");
        System.out.println("🚀 Study App Server Started!");
        System.out.println("📍 Server: http://localhost:8080");
        System.out.println("🗄️ H2 Console: http://localhost:8080/h2-console");
        System.out.println("📚 API Base URL: http://localhost:8080/api");
        System.out.println("=================================");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("*")  // allowedOrigins("*") 대신 사용
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);    // credentials를 false로 변경
            }
        };
    }
}
