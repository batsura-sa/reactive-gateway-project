package com.example.service;

import com.example.dto.CreateUserRequest;
import com.example.dto.UpdateUserRequest;
import com.example.dto.UserDto;
import com.example.grpc.*;
import com.example.mapper.UserMapper;
import com.example.metrics.CustomMetrics;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service layer that handles business logic and calls gRPC services
 */
@Service
public class UserGatewayService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserGatewayService.class);
    
    private final ReactorUserServiceGrpc.ReactorUserServiceStub userServiceStub;
    private final UserMapper userMapper;
    private final CustomMetrics customMetrics;
    
    public UserGatewayService(ReactorUserServiceGrpc.ReactorUserServiceStub userServiceStub, 
                             UserMapper userMapper,
                             CustomMetrics customMetrics) {
        this.userServiceStub = userServiceStub;
        this.userMapper = userMapper;
        this.customMetrics = customMetrics;
    }
    
    /**
     * Get user by ID
     */
    @Timed(value = "user.service.get", description = "Time taken to get user by ID")
    public Mono<UserDto> getUserById(String userId) {
        logger.info("Getting user by ID: {}", userId);
        
        Timer.Sample sample = customMetrics.startGrpcTimer();
        
        GetUserRequest request = GetUserRequest.newBuilder()
                .setUserId(userId)
                .build();
        
        return userServiceStub.getUser(request)
                .doOnNext(response -> {
                    logger.debug("Received gRPC response: {}", response);
                    customMetrics.stopGrpcTimer(sample);
                })
                .filter(GetUserResponse::getFound)
                .map(GetUserResponse::getUser)
                .map(userMapper::toDto)
                .doOnError(error -> {
                    logger.error("Error getting user by ID: {}", userId, error);
                    customMetrics.incrementGrpcError();
                    customMetrics.stopGrpcTimer(sample);
                });
    }
    
    /**
     * Create a new user
     */
    @Timed(value = "user.service.create", description = "Time taken to create user")
    public Mono<UserDto> createUser(CreateUserRequest createRequest) {
        logger.info("Creating user: {}", createRequest.name());
        
        Timer.Sample sample = customMetrics.startGrpcTimer();
        
        com.example.grpc.CreateUserRequest grpcRequest = com.example.grpc.CreateUserRequest.newBuilder()
                .setName(createRequest.name())
                .setEmail(createRequest.email())
                .setAge(createRequest.age())
                .build();
        
        return userServiceStub.createUser(grpcRequest)
                .doOnNext(response -> {
                    logger.debug("Received gRPC response: {}", response);
                    customMetrics.stopGrpcTimer(sample);
                    if (response.getSuccess()) {
                        customMetrics.incrementUserCreated();
                    }
                })
                .filter(com.example.grpc.CreateUserResponse::getSuccess)
                .map(com.example.grpc.CreateUserResponse::getUser)
                .map(userMapper::toDto)
                .doOnError(error -> {
                    logger.error("Error creating user: {}", createRequest.name(), error);
                    customMetrics.incrementGrpcError();
                    customMetrics.stopGrpcTimer(sample);
                });
    }
    
    /**
     * Update an existing user
     */
    public Mono<UserDto> updateUser(String userId, UpdateUserRequest updateRequest) {
        logger.info("Updating user: {}", userId);
        
        com.example.grpc.UpdateUserRequest.Builder builder = com.example.grpc.UpdateUserRequest.newBuilder()
                .setUserId(userId);
        
        if (updateRequest.name() != null) {
            builder.setName(updateRequest.name());
        }
        if (updateRequest.email() != null) {
            builder.setEmail(updateRequest.email());
        }
        if (updateRequest.age() != null) {
            builder.setAge(updateRequest.age());
        }
        
        return userServiceStub.updateUser(builder.build())
                .doOnNext(response -> logger.debug("Received gRPC response: {}", response))
                .filter(com.example.grpc.UpdateUserResponse::getSuccess)
                .map(com.example.grpc.UpdateUserResponse::getUser)
                .map(userMapper::toDto)
                .doOnError(error -> logger.error("Error updating user: {}", userId, error));
    }
    
    /**
     * Delete a user
     */
    @Timed(value = "user.service.delete", description = "Time taken to delete user")
    public Mono<Boolean> deleteUser(String userId) {
        logger.info("Deleting user: {}", userId);
        
        Timer.Sample sample = customMetrics.startGrpcTimer();
        
        DeleteUserRequest request = DeleteUserRequest.newBuilder()
                .setUserId(userId)
                .build();
        
        return userServiceStub.deleteUser(request)
                .doOnNext(response -> {
                    logger.debug("Received gRPC response: {}", response);
                    customMetrics.stopGrpcTimer(sample);
                    if (response.getSuccess()) {
                        customMetrics.incrementUserDeleted();
                    }
                })
                .map(DeleteUserResponse::getSuccess)
                .doOnError(error -> {
                    logger.error("Error deleting user: {}", userId, error);
                    customMetrics.incrementGrpcError();
                    customMetrics.stopGrpcTimer(sample);
                });
    }
    
    /**
     * List users with pagination
     */
    @Timed(value = "user.service.list", description = "Time taken to list users")
    public Flux<UserDto> listUsers(int page, int size) {
        logger.info("Listing users - page: {}, size: {}", page, size);
        
        Timer.Sample sample = customMetrics.startGrpcTimer();
        
        ListUsersRequest request = ListUsersRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .build();
        
        return userServiceStub.listUsers(request)
                .doOnNext(response -> {
                    logger.debug("Received gRPC response with {} users", response.getUsersCount());
                    customMetrics.stopGrpcTimer(sample);
                })
                .flatMapMany(response -> Flux.fromIterable(response.getUsersList()))
                .map(userMapper::toDto)
                .doOnError(error -> {
                    logger.error("Error listing users", error);
                    customMetrics.incrementGrpcError();
                    customMetrics.stopGrpcTimer(sample);
                });
    }
}