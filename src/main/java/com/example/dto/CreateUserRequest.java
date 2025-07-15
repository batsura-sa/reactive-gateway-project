package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a user
 */
public record CreateUserRequest(
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
        Integer age
) {
}