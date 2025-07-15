package com.example.controller;

import com.example.dto.CreateUserRequest;
import com.example.dto.UpdateUserRequest;
import com.example.dto.UserDto;
import com.example.metrics.CustomMetrics;
import com.example.service.UserGatewayService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive REST Controller for User operations
 * Receives HTTP requests and delegates to gRPC services
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserGatewayService userGatewayService;
    private final CustomMetrics customMetrics;
    
    public UserController(UserGatewayService userGatewayService, CustomMetrics customMetrics) {
        this.userGatewayService = userGatewayService;
        this.customMetrics = customMetrics;
    }
    
    /**
     * GET /api/users/{id} - Get user by ID
     */
    @GetMapping("/{id}")
    @Timed(value = "http.requests", description = "Time taken for HTTP requests", extraTags = {"endpoint", "getUserById"})
    public Mono<ResponseEntity<UserDto>> getUserById(@PathVariable String id) {
        logger.info("REST: Getting user by ID: {}", id);
        
        Timer.Sample sample = customMetrics.startHttpTimer();
        customMetrics.incrementActiveConnections();
        
        return userGatewayService.getUserById(id)
                .map(user -> ResponseEntity.ok(user))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnError(error -> logger.error("REST: Error getting user by ID: {}", id, error))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .doFinally(signalType -> {
                    customMetrics.stopHttpTimer(sample);
                    customMetrics.decrementActiveConnections();
                });
    }
    
    /**
     * POST /api/users - Create a new user
     */
    @PostMapping
    public Mono<ResponseEntity<UserDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        logger.info("REST: Creating user: {}", request.name());
        
        return userGatewayService.createUser(request)
                .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user))
                .doOnError(error -> logger.error("REST: Error creating user: {}", request.name(), error))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
    
    /**
     * PUT /api/users/{id} - Update an existing user
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserDto>> updateUser(@PathVariable String id, 
                                                   @Valid @RequestBody UpdateUserRequest request) {
        logger.info("REST: Updating user: {}", id);
        
        return userGatewayService.updateUser(id, request)
                .map(user -> ResponseEntity.ok(user))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnError(error -> logger.error("REST: Error updating user: {}", id, error))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
    
    /**
     * DELETE /api/users/{id} - Delete a user
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String id) {
        logger.info("REST: Deleting user: {}", id);
        
        return userGatewayService.deleteUser(id)
                .map(success -> success ? 
                    ResponseEntity.noContent().<Void>build() : 
                    ResponseEntity.notFound().<Void>build())
                .doOnError(error -> logger.error("REST: Error deleting user: {}", id, error))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
    
    /**
     * GET /api/users - List users with pagination
     */
    @GetMapping
    public Flux<UserDto> listUsers(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        logger.info("REST: Listing users - page: {}, size: {}", page, size);
        
        return userGatewayService.listUsers(page, size)
                .doOnError(error -> logger.error("REST: Error listing users", error));
    }
    
    /**
     * GET /api/users/health - Health check endpoint
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<String>> healthCheck() {
        return Mono.just(ResponseEntity.ok("User Gateway is healthy"));
    }
}