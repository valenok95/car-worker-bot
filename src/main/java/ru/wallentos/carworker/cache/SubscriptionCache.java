package ru.wallentos.carworker.cache;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SubscriptionCache implements RedisCache {

    @Value("${ru.wallentos.carworker.user-prefix}")
    private String userPrefix;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(String id, Object value) {
        redisTemplate.opsForValue().set(userPrefix + id, value);

    }

    @Override
    public Object getById(String id) {
        return redisTemplate.opsForValue().get(userPrefix + id);
    }

    @Override
    public void deleteById(String id) {
        redisTemplate.delete(userPrefix + id);
    }

    @Override
    public List<String> getAllKeys() {
        return Objects.requireNonNull(redisTemplate.keys(userPrefix + "*")).stream().map(key -> key.replace(userPrefix, "")).toList();
    }
}
