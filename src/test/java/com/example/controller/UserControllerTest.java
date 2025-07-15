package com.example.controller;

import com.example.config.TestConfig;
import com.example.dto.CreateUserRequest;
import com.example.dto.UpdateUserRequest;
import com.example.dto.UserDto;
import com.example.service.UserGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserController
 */
@WebFluxTest(controllers = UserController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
    com.example.ratelimit.RateLimitFilter.class
}))
@Import(TestConfig.class)
class UserControllerTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private UserGatewayService userGatewayService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testGetUserById_Success() {
        // Given
        String userId = "1";
        UserDto userDto = new UserDto("1", "John Doe", "john@example.com", 30, 
                Instant.now(), Instant.now());
        
        when(userGatewayService.getUserById(userId)).thenReturn(Mono.just(userDto));
        
        // When & Then
        webTestClient.get()
                .uri("/api/users/{id}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .isEqualTo(userDto);
    }
    
    @Test
    void testGetUserById_NotFound() {
        // Given
        String userId = "999";
        when(userGatewayService.getUserById(userId)).thenReturn(Mono.empty());
        
        // When & Then
        webTestClient.get()
                .uri("/api/users/{id}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
    
    @Test
    void testCreateUser_Success() {
        // Given
        CreateUserRequest request = new CreateUserRequest("Jane Doe", "jane@example.com", 25);
        UserDto createdUser = new UserDto("2", "Jane Doe", "jane@example.com", 25, 
                Instant.now(), Instant.now());
        
        when(userGatewayService.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.just(createdUser));
        
        // When & Then
        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserDto.class)
                .isEqualTo(createdUser);
    }
    
    @Test
    void testCreateUser_ValidationError() {
        // Given - invalid request (missing required fields)
        CreateUserRequest invalidRequest = new CreateUserRequest("", "invalid-email", -1);
        
        // When & Then
        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }
    
    @Test
    void testUpdateUser_Success() {
        // Given
        String userId = "1";
        UpdateUserRequest request = new UpdateUserRequest("John Updated", "john.updated@example.com", 31);
        UserDto updatedUser = new UserDto("1", "John Updated", "john.updated@example.com", 31, 
                Instant.now(), Instant.now());
        
        when(userGatewayService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                .thenReturn(Mono.just(updatedUser));
        
        // When & Then
        webTestClient.put()
                .uri("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .isEqualTo(updatedUser);
    }
    
    @Test
    void testDeleteUser_Success() {
        // Given
        String userId = "1";
        when(userGatewayService.deleteUser(userId)).thenReturn(Mono.just(true));
        
        // When & Then
        webTestClient.delete()
                .uri("/api/users/{id}", userId)
                .exchange()
                .expectStatus().isNoContent();
    }
    
    @Test
    void testDeleteUser_NotFound() {
        // Given
        String userId = "999";
        when(userGatewayService.deleteUser(userId)).thenReturn(Mono.just(false));
        
        // When & Then
        webTestClient.delete()
                .uri("/api/users/{id}", userId)
                .exchange()
                .expectStatus().isNotFound();
    }
    
    @Test
    void testListUsers_Success() {
        // Given
        UserDto user1 = new UserDto("1", "John Doe", "john@example.com", 30, 
                Instant.now(), Instant.now());
        UserDto user2 = new UserDto("2", "Jane Doe", "jane@example.com", 25, 
                Instant.now(), Instant.now());
        
        when(userGatewayService.listUsers(0, 10))
                .thenReturn(Flux.just(user1, user2));
        
        // When & Then
        webTestClient.get()
                .uri("/api/users?page=0&size=10")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDto.class)
                .hasSize(2)
                .contains(user1, user2);
    }
    
    @Test
    void testHealthCheck() {
        // When & Then
        webTestClient.get()
                .uri("/api/users/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("User Gateway is healthy");
    }
}