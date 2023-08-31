package ru.wallentos.carworker.cache;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.wallentos.carworker.model.CarDto;

@Repository
public class CheCarCache implements RedisCache {

    private final String cheCarPrefix = "che-car_";
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(String id, Object value) {
        redisTemplate.opsForValue().set(cheCarPrefix + id, value);
    }

    @Override
    public CarDto getById(String id) {
        return (CarDto) redisTemplate.opsForValue().get(cheCarPrefix + id);
    }

    @Override
    public void deleteById(String id) {
        redisTemplate.delete(cheCarPrefix + id);
    }

    @Override
    public List<String> getAllKeys() {
        return Objects.requireNonNull(redisTemplate.keys(cheCarPrefix + "*")).stream().toList();
    }
}
