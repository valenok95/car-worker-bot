package ru.wallentos.carworker.cache;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.wallentos.carworker.model.EncarDto;

@Repository
public class EncarCache implements RedisCache {

    private final String carPrefix = "car_";
    @Autowired
    private RedisTemplate<String, EncarDto> redisTemplate;

    @Override
    public void save(String id, Object value) {
        redisTemplate.opsForValue().set(carPrefix + id, (EncarDto) value);
    }

    @Override
    public EncarDto getById(String id) {
        return redisTemplate.opsForValue().get(carPrefix + id);
    }

    @Override
    public void deleteById(String id) {
        redisTemplate.delete(carPrefix + id);
    }

    @Override
    public List<String> getAllKeys() {
        return Objects.requireNonNull(redisTemplate.keys(carPrefix + "*")).stream().toList();
    }
}
