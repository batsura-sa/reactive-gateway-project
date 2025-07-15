package com.example.config;

import com.example.grpc.ReactorUserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for gRPC clients
 */
@Configuration
public class GrpcClientConfig {
    
    @Value("${grpc.client.user-service.host:localhost}")
    private String userServiceHost;
    
    @Value("${grpc.client.user-service.port:9090}")
    private int userServicePort;
    
    @Bean
    public ManagedChannel userServiceChannel() {
        return ManagedChannelBuilder.forAddress(userServiceHost, userServicePort)
                .usePlaintext()
                .build();
    }
    
    @Bean
    public ReactorUserServiceGrpc.ReactorUserServiceStub userServiceStub(ManagedChannel userServiceChannel) {
        return ReactorUserServiceGrpc.newReactorStub(userServiceChannel);
    }
}