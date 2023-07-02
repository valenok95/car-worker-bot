package ru.wallentos.carworker.cache;


import ru.wallentos.carworker.model.EncarDto;

public interface RedisCache {
    void saveEncarDto(String carId, EncarDto encarDto);

    EncarDto getEncarDtoByCarId(String carId);
}
