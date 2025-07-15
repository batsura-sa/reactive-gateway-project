package com.example.service;

import com.example.dto.CreateUserRequest;
import com.example.dto.UpdateUserRequest;
import com.example.dto.UserDto;
import com.example.grpc.*;
import com.example.mapper.UserMapper;
import com.example.metrics.CustomMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserGatewayService
 */
@ExtendWith(MockitoExtension.class)
class UserGatewayServiceTest {
    
    @Mock
    private ReactorUserServiceGrpc.ReactorUserServiceStub userServiceStub;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private CustomMetrics customMetrics;
    
    private UserGatewayService userGatewayService;
    
    @BeforeEach
    void setUp() {
        userGatewayService = new UserGatewayService(userServiceStub, userMapper, customMetrics);
    }
    
    @Test
    void testGetUserById_Success() {
        // Given
        String userId = "1";
        User grpcUser = User.newBuilder()
                .setId("1")
                .setName("John Doe")
                .setEmail("john@example.com")
                .setAge(30)
                .build();
        
        GetUserResponse grpcResponse = GetUserResponse.newBuilder()
                .setUser(grpcUser)
                .setFound(true)
                .build();
        
        UserDto expectedDto = new UserDto("1", "John Doe", "john@example.com", 30, 
                Instant.now(), Instant.now());
        
        when(userServiceStub.getUser(any(GetUserRequest.class)))
                .thenReturn(Mono.just(grpcResponse));
        when(userMapper.toDto(grpcUser)).thenReturn(expectedDto);
        
        // When & Then
        StepVerifier.create(userGatewayService.getUserById(userId))
                .expectNext(expectedDto)
                .verifyComplete();
    }
    
    @Test
    void testGetUserById_NotFound() {
        // Given
        String userId = "999";
        GetUserResponse grpcResponse = GetUserResponse.newBuilder()
                .setFound(false)
                .build();
        
        when(userServiceStub.getUser(any(GetUserRequest.class)))
                .thenReturn(Mono.just(grpcResponse));
        
        // When & Then
        StepVerifier.create(userGatewayService.getUserById(userId))
                .verifyComplete();
    }
    
    @Test
    void testCreateUser_Success() {
        // Given
        CreateUserRequest request = new CreateUserRequest("Jane Doe", "jane@example.com", 25);
        
        User grpcUser = User.newBuilder()
                .setId("2")
                .setName("Jane Doe")
                .setEmail("jane@example.com")
                .setAge(25)
                .build();
        
        com.example.grpc.CreateUserResponse grpcResponse = com.example.grpc.CreateUserResponse.newBuilder()
                .setUser(grpcUser)
                .setSuccess(true)
                .build();
        
        UserDto expectedDto = new UserDto("2", "Jane Doe", "jane@example.com", 25, 
                Instant.now(), Instant.now());
        
        when(userServiceStub.createUser(any(com.example.grpc.CreateUserRequest.class)))
                .thenReturn(Mono.just(grpcResponse));
        when(userMapper.toDto(grpcUser)).thenReturn(expectedDto);
        
        // When & Then
        StepVerifier.create(userGatewayService.createUser(request))
                .expectNext(expectedDto)
                .verifyComplete();
    }
    
    @Test
    void testUpdateUser_Success() {
        // Given
        String userId = "1";
        UpdateUserRequest request = new UpdateUserRequest("John Updated", "john.updated@example.com", 31);
        
        User grpcUser = User.newBuilder()
                .setId("1")
                .setName("John Updated")
                .setEmail("john.updated@example.com")
                .setAge(31)
                .build();
        
        com.example.grpc.UpdateUserResponse grpcResponse = com.example.grpc.UpdateUserResponse.newBuilder()
                .setUser(grpcUser)
                .setSuccess(true)
                .build();
        
        UserDto expectedDto = new UserDto("1", "John Updated", "john.updated@example.com", 31, 
                Instant.now(), Instant.now());
        
        when(userServiceStub.updateUser(any(com.example.grpc.UpdateUserRequest.class)))
                .thenReturn(Mono.just(grpcResponse));
        when(userMapper.toDto(grpcUser)).thenReturn(expectedDto);
        
        // When & Then
        StepVerifier.create(userGatewayService.updateUser(userId, request))
                .expectNext(expectedDto)
                .verifyComplete();
    }
    
    @Test
    void testDeleteUser_Success() {
        // Given
        String userId = "1";
        DeleteUserResponse grpcResponse = DeleteUserResponse.newBuilder()
                .setSuccess(true)
                .build();
        
        when(userServiceStub.deleteUser(any(DeleteUserRequest.class)))
                .thenReturn(Mono.just(grpcResponse));
        
        // When & Then
        StepVerifier.create(userGatewayService.deleteUser(userId))
                .expectNext(true)
                .verifyComplete();
    }
}