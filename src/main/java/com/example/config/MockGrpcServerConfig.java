package com.example.config;

import com.example.grpc.MockUserServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

/**
 * Configuration to start a mock gRPC server for development/testing
 * Only active when 'mock-grpc' profile is enabled
 */
@Configuration
@Profile("mock-grpc")
public class MockGrpcServerConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(MockGrpcServerConfig.class);
    
    @Value("${grpc.client.user-service.port:9090}")
    private int grpcPort;
    
    private Server server;
    
    @PostConstruct
    public void startMockGrpcServer() {
        try {
            server = ServerBuilder.forPort(grpcPort)
                    .addService(new MockUserServiceImpl())
                    .build()
                    .start();
            
            logger.info("Mock gRPC server started on port {}", grpcPort);
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down mock gRPC server");
                if (server != null) {
                    server.shutdown();
                }
            }));
            
        } catch (IOException e) {
            logger.error("Failed to start mock gRPC server", e);
            throw new RuntimeException("Failed to start mock gRPC server", e);
        }
    }
    
    @PreDestroy
    public void stopMockGrpcServer() {
        if (server != null) {
            logger.info("Stopping mock gRPC server");
            server.shutdown();
        }
    }
}