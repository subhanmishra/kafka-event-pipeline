package com.subhanmishra.course;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisIdempotencyService implements IdempotencyService {

    //private final StringRedisTemplate redisTemplate;
    private static final long TTL_SECONDS = 24 * 60 * 60; // 24 hours


    private final RedisTemplate<String, Object> redisTemplate;

    public RedisIdempotencyService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isDone(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void markDone(String key) {
        redisTemplate.opsForValue()
                .set(key, "DONE", TTL_SECONDS, TimeUnit.SECONDS);
    }
}
