package com.example.ratelimit;

import com.example.ReactiveGatewayApplication;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

/**
 * Integration tests for rate limiting functionality
 */
@SpringBootTest(classes = ReactiveGatewayApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "rate-limit.enabled=true",
        "rate-limit.use-redis=false",
        "rate-limit.get-user-by-id.capacity=3",
        "rate-limit.get-user-by-id.refill-tokens=3",
        "rate-limit.get-user-by-id.refill-period=PT1S",
        "spring.profiles.active=test"
})
@Import(com.example.config.IntegrationTestConfig.class)
class RateLimitIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @Order(1)
    void shouldAllowRequestsWithinLimit() {
        sleep(Duration.ofSeconds(2));
        // First 3 requests should be allowed
        for (int i = 0; i < 3; i++) {
            webTestClient.get()
                    .uri("/api/users/test-user-" + i)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().exists("X-RateLimit-Remaining");
        }
    }

    @Test
    @Order(2)
    void shouldRejectRequestsExceedingLimit() {
        sleep(Duration.ofSeconds(2));
        // Make requests up to the limit
        for (int i = 0; i < 3; i++) {
            webTestClient.get()
                    .uri("/api/users/test-user-limit-" + i)
                    .exchange()
                    .expectStatus().isOk();
        }

        // Next request should be rate limited
        webTestClient.get()
                .uri("/api/users/test-user-limit-exceeded")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectHeader().exists("X-RateLimit-Remaining")
                .expectHeader().exists("Retry-After")
                .expectBody()
                .jsonPath("$.error").isEqualTo("Rate limit exceeded")
                .jsonPath("$.status").isEqualTo(429);
    }

    @Test
    @Order(3)
    void shouldAddRateLimitHeaders() {
        sleep(Duration.ofSeconds(2));
        webTestClient.get()
                .uri("/api/users/test-headers")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-RateLimit-Remaining");
    }

    @Test
    @Disabled
    void shouldNotRateLimitActuatorEndpoints() {
        // Actuator endpoints should not be rate limited
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().doesNotExist("X-RateLimit-Remaining");
    }

    void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

        @Test
        @Disabled
        void shouldHandleDifferentEndpointsSeparately () {
            // Test that different endpoints have separate rate limits

            // Use up getUserById limit
            for (int i = 0; i < 3; i++) {
                webTestClient.get()
                        .uri("/api/users/test-separate-" + i)
                        .exchange()
                        .expectStatus().isOk();
            }

            // getUserById should be rate limited
            webTestClient.get()
                    .uri("/api/users/test-separate-exceeded")
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

            // But listUsers should still work (different endpoint, different limit)
            webTestClient.get()
                    .uri("/api/users")
                    .exchange()
                    .expectStatus().isOk();
        }
    }