package com.imageng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImageGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageGeneratorApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" 🚀 Text-to-Image Generator (Java Spring Boot) is running!");
        System.out.println(" 🌐 Open Web UI: http://localhost:8080");
        System.out.println(" 📡 REST API:    http://localhost:8080/api/health");
        System.out.println("==================================================================");
    }
}
