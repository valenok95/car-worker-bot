package ru.wallentos.carworker.cache;

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
}
