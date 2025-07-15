package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;

/**
 * Request DTO for updating a user
 */
public record UpdateUserRequest(
        @JsonProperty("name")
        String name,
        
        @Email(message = "Email should be valid")
        @JsonProperty("email")
        String email,
        
        @Min(value = 0, message = "Age must be non-negative")
        @JsonProperty("age")
        Integer age
) {
}