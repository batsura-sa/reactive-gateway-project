package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Reactive Spring Boot Gateway Application
 * Receives HTTP REST requests and calls gRPC services
 */
@SpringBootApplication
public class ReactiveGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ReactiveGatewayApplication.class, args);
    }
}