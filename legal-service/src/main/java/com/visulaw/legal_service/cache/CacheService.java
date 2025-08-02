package com.visulaw.legal_service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration defaultTtl = Duration.ofMinutes(5);

    public <T> void put(String key, T value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public <T> void put(String key, T value) {
        put(key, value, defaultTtl);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object val = redisTemplate.opsForValue().get(key);

        if (val != null && clazz.isInstance(val)) {
            return (T) val;
        }
        return null;
    }

    public boolean contains(String key) {
        return redisTemplate.hasKey(key);
    }

    public void evict(String key) {
        redisTemplate.delete(key);
    }

}
