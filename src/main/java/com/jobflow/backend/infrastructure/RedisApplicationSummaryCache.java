package com.jobflow.backend.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.backend.application.ApplicationSummary;
import com.jobflow.backend.application.ApplicationSummaryCache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisApplicationSummaryCache implements ApplicationSummaryCache {

    private static final String KEY = "jobflow:application-summary:v1";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisApplicationSummaryCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<ApplicationSummary> get() {
        try {
            String json = redisTemplate.opsForValue().get(KEY);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, ApplicationSummary.class));
        } catch (RuntimeException | JsonProcessingException ex) {
            return Optional.empty();
        }
    }

    @Override
    public void put(ApplicationSummary summary) {
        try {
            redisTemplate.opsForValue().set(KEY, objectMapper.writeValueAsString(summary), TTL);
        } catch (RuntimeException | JsonProcessingException ignored) {
            // The database remains authoritative if Redis is temporarily unavailable.
        }
    }

    @Override
    public void evict() {
        try {
            redisTemplate.delete(KEY);
        } catch (RuntimeException ignored) {
            // Cache eviction should never block the application workflow.
        }
    }
}
