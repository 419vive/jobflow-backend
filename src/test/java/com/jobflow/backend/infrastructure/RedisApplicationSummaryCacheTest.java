package com.jobflow.backend.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.backend.application.ApplicationSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
@SuppressWarnings("unchecked")
class RedisApplicationSummaryCacheTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final RedisApplicationSummaryCache cache = new RedisApplicationSummaryCache(
            redisTemplate,
            new ObjectMapper()
    );

    @Test
    void logsAndFallsBackWhenRedisReadFails(CapturedOutput output) {
        when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("redis down"));

        assertThat(cache.get()).isEmpty();
        assertThat(output.getOut()).contains("Redis summary cache read failed");
    }

    @Test
    void logsAndContinuesWhenRedisWriteFails(CapturedOutput output) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RedisConnectionFailureException("redis down")).when(valueOperations).set(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()
        );

        cache.put(new ApplicationSummary(1L, 1L, 1L, 0L, 0L, 0L));

        assertThat(output.getOut()).contains("Redis summary cache write failed");
    }

    @Test
    void logsAndContinuesWhenRedisEvictFails(CapturedOutput output) {
        when(redisTemplate.delete(org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new RedisConnectionFailureException("redis down"));

        cache.evict();

        verify(redisTemplate).delete(org.mockito.ArgumentMatchers.anyString());
        assertThat(output.getOut()).contains("Redis summary cache eviction failed");
    }
}
