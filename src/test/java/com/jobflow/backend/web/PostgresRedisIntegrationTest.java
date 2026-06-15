package com.jobflow.backend.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.backend.domain.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class PostgresRedisIntegrationTest {

    private static final String SUMMARY_CACHE_KEY = "jobflow:application-summary:v1";

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("jobflow")
            .withUsername("jobflow")
            .withPassword("jobflow");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void persistsWithPostgresAndCachesSummaryInRedis() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .header("Idempotency-Key", "postgres-redis-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateApplicationRequest(
                                "Bruce HR",
                                "Java Backend Engineer",
                                "Remote",
                                true,
                                "Postgres and Redis integration"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        mockMvc.perform(get("/api/applications/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.remote", is(1)));

        org.assertj.core.api.Assertions.assertThat(redisTemplate.hasKey(SUMMARY_CACHE_KEY)).isTrue();

        mockMvc.perform(post("/api/applications")
                        .header("Idempotency-Key", "postgres-redis-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateApplicationRequest(
                                "Another Company",
                                "Backend Engineer",
                                "Remote",
                                true,
                                "Evicts stale summary"
                        ))))
                .andExpect(status().isCreated());

        org.assertj.core.api.Assertions.assertThat(redisTemplate.hasKey(SUMMARY_CACHE_KEY)).isFalse();

        mockMvc.perform(get("/api/applications/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.remote", is(2)));
    }

    @Test
    void repeatedIdempotencyKeyReturnsExistingPostgresRow() throws Exception {
        CreateApplicationRequest request = new CreateApplicationRequest(
                "Bruce HR",
                "Java Backend Engineer",
                "Remote",
                true,
                "First insert"
        );

        String response = mockMvc.perform(post("/api/applications")
                        .header("Idempotency-Key", "postgres-duplicate-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/applications")
                        .header("Idempotency-Key", "postgres-duplicate-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateApplicationRequest(
                                "Different Company",
                                "Different Title",
                                "Remote",
                                true,
                                "Should reuse existing row"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.company", is("Bruce HR")));
    }

    @Test
    void persistsStatusHistoryInPostgres() throws Exception {
        String response = mockMvc.perform(post("/api/applications")
                        .header("Idempotency-Key", "postgres-history-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateApplicationRequest(
                                "Bruce HR",
                                "Java Backend Engineer",
                                "Remote",
                                true,
                                "Status history"
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(patch("/api/applications/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeStatusRequest(
                                ApplicationStatus.SUBMITTED,
                                "Submitted with Java proof repo"
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/applications/{id}/status-history", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].toStatus", is("SOURCED")))
                .andExpect(jsonPath("$[1].fromStatus", is("SOURCED")))
                .andExpect(jsonPath("$[1].toStatus", is("SUBMITTED")))
                .andExpect(jsonPath("$[1].reason", is("Submitted with Java proof repo")));
    }
}
