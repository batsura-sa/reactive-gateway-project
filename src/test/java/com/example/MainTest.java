package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ReactiveGatewayApplication
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(com.example.config.IntegrationTestConfig.class)
class ReactiveGatewayApplicationTest {
    
    @Test
    @DisplayName("Test Spring Boot application context loads")
    void contextLoads() {
        // This test ensures the Spring Boot application context loads successfully
        // If the context fails to load, this test will fail
    }
    
    @Test
    @DisplayName("Test Java version compatibility")
    void testJavaVersion() {
        // Verify we're running on Java 21 or later
        String javaVersion = System.getProperty("java.version");
        assertNotNull(javaVersion);
        
        // Extract major version number
        String[] versionParts = javaVersion.split("\\.");
        int majorVersion = Integer.parseInt(versionParts[0]);
        
        assertTrue(majorVersion >= 21, 
            "This project requires Java 21 or later. Current version: " + javaVersion);
    }
}