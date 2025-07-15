package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Data Transfer Object for User
 */
public record UserDto(
        @JsonProperty("id")
        String id,
        
        @NotBlank(message = "Name is required")
        @JsonProperty("name")
        String name,
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @JsonProperty("email")
        String email,
        
        @NotNull(message = "Age is required")
        @Min(value = 0, message = "Age must be non-negative")
        @JsonProperty("age")
        Integer age,
        
        @JsonProperty("created_at")
        Instant createdAt,
        
        @JsonProperty("updated_at")
        Instant updatedAt
) {
    
    /**
     * Constructor for creating new users (without ID and timestamps)
     */
    public UserDto(String name, String email, Integer age) {
        this(null, name, email, age, null, null);
    }
}