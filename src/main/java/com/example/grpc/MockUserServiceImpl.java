package com.example.grpc;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock implementation of UserService for testing purposes
 * This would normally be a separate gRPC server
 */
public class MockUserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    
    private static final Logger logger = LoggerFactory.getLogger(MockUserServiceImpl.class);
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    public MockUserServiceImpl() {
        // Add some sample data
        createSampleUsers();
    }
    
    private void createSampleUsers() {
        User user1 = User.newBuilder()
                .setId("1")
                .setName("John Doe")
                .setEmail("john.doe@example.com")
                .setAge(30)
                .setCreatedAt(Instant.now().getEpochSecond())
                .setUpdatedAt(Instant.now().getEpochSecond())
                .build();
        
        User user2 = User.newBuilder()
                .setId("2")
                .setName("Jane Smith")
                .setEmail("jane.smith@example.com")
                .setAge(25)
                .setCreatedAt(Instant.now().getEpochSecond())
                .setUpdatedAt(Instant.now().getEpochSecond())
                .build();
        
        users.put("1", user1);
        users.put("2", user2);
    }
    
    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        logger.info("Mock gRPC: Getting user by ID: {}", request.getUserId());
        
        User user = users.get(request.getUserId());
        GetUserResponse response = GetUserResponse.newBuilder()
                .setUser(user != null ? user : User.getDefaultInstance())
                .setFound(user != null)
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {
        logger.info("Mock gRPC: Creating user: {}", request.getName());
        
        String userId = UUID.randomUUID().toString();
        long now = Instant.now().getEpochSecond();
        
        User user = User.newBuilder()
                .setId(userId)
                .setName(request.getName())
                .setEmail(request.getEmail())
                .setAge(request.getAge())
                .setCreatedAt(now)
                .setUpdatedAt(now)
                .build();
        
        users.put(userId, user);
        
        CreateUserResponse response = CreateUserResponse.newBuilder()
                .setUser(user)
                .setSuccess(true)
                .setMessage("User created successfully")
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UpdateUserResponse> responseObserver) {
        logger.info("Mock gRPC: Updating user: {}", request.getUserId());
        
        User existingUser = users.get(request.getUserId());
        if (existingUser == null) {
            UpdateUserResponse response = UpdateUserResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("User not found")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }
        
        User.Builder userBuilder = existingUser.toBuilder()
                .setUpdatedAt(Instant.now().getEpochSecond());
        
        if (!request.getName().isEmpty()) {
            userBuilder.setName(request.getName());
        }
        if (!request.getEmail().isEmpty()) {
            userBuilder.setEmail(request.getEmail());
        }
        if (request.getAge() > 0) {
            userBuilder.setAge(request.getAge());
        }
        
        User updatedUser = userBuilder.build();
        users.put(request.getUserId(), updatedUser);
        
        UpdateUserResponse response = UpdateUserResponse.newBuilder()
                .setUser(updatedUser)
                .setSuccess(true)
                .setMessage("User updated successfully")
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        logger.info("Mock gRPC: Deleting user: {}", request.getUserId());
        
        boolean existed = users.remove(request.getUserId()) != null;
        
        DeleteUserResponse response = DeleteUserResponse.newBuilder()
                .setSuccess(existed)
                .setMessage(existed ? "User deleted successfully" : "User not found")
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<ListUsersResponse> responseObserver) {
        logger.info("Mock gRPC: Listing users - page: {}, size: {}", request.getPage(), request.getSize());
        
        ListUsersResponse.Builder responseBuilder = ListUsersResponse.newBuilder()
                .setTotalCount(users.size())
                .setPage(request.getPage())
                .setSize(request.getSize());
        
        users.values().forEach(responseBuilder::addUsers);
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}