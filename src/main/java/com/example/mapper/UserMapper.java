package com.example.mapper;

import com.example.dto.UserDto;
import com.example.grpc.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mapper to convert between gRPC User and UserDto
 */
@Component
public class UserMapper {
    
    /**
     * Convert gRPC User to UserDto
     */
    public UserDto toDto(User grpcUser) {
        return new UserDto(
                grpcUser.getId(),
                grpcUser.getName(),
                grpcUser.getEmail(),
                grpcUser.getAge(),
                grpcUser.getCreatedAt() > 0 ? Instant.ofEpochSecond(grpcUser.getCreatedAt()) : null,
                grpcUser.getUpdatedAt() > 0 ? Instant.ofEpochSecond(grpcUser.getUpdatedAt()) : null
        );
    }
    
    /**
     * Convert UserDto to gRPC User
     */
    public User toGrpc(UserDto userDto) {
        User.Builder builder = User.newBuilder()
                .setName(userDto.name())
                .setEmail(userDto.email())
                .setAge(userDto.age());
        
        if (userDto.id() != null) {
            builder.setId(userDto.id());
        }
        
        if (userDto.createdAt() != null) {
            builder.setCreatedAt(userDto.createdAt().getEpochSecond());
        }
        
        if (userDto.updatedAt() != null) {
            builder.setUpdatedAt(userDto.updatedAt().getEpochSecond());
        }
        
        return builder.build();
    }
}