package ru.wallentos.carworker.cache;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.wallentos.carworker.model.EncarDto;

@Repository
public class EncarCache implements RedisCache {
    @Autowired
    private RedisTemplate<String, EncarDto> redisTemplate;

    @Override
    public void saveEncarDto(String carId, EncarDto encarDto) {
        redisTemplate.opsForValue().set(carId, encarDto);
    }

    @Override
    public EncarDto getEncarDtoByCarId(String carId) {
        return redisTemplate.opsForValue().get(carId);
    }

    @Override
    public void deleteEncarDtoByCarId(String carId) {
        redisTemplate.delete(carId);
    }
    
    @Override
    public List<String> getAllKeys() {
        return Objects.requireNonNull(redisTemplate.keys("*")).stream().toList();
    }
}
