package ru.wallentos.carworker.cache;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.wallentos.carworker.model.CarDto;

@Repository
public class EncarCache implements RedisCache {

    private final String encarPrefix = "encar_";
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(String id, Object value) {
        redisTemplate.opsForValue().set(encarPrefix + id, (CarDto) value);
    }

    @Override
    public CarDto getById(String id) {
        return (CarDto) redisTemplate.opsForValue().get(encarPrefix + id);
    }

    @Override
    public void deleteById(String id) {
        redisTemplate.delete(encarPrefix + id);
    }

    @Override
    public List<String> getAllKeys() {
        return Objects.requireNonNull(redisTemplate.keys(encarPrefix + "*")).stream().map(key->key.replace(encarPrefix,"")).toList();
    }
}
