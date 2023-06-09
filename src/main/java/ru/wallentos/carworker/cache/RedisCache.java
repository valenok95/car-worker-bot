package ru.wallentos.carworker.cache;


import java.util.List;
import ru.wallentos.carworker.model.EncarDto;

public interface RedisCache {
    void saveEncarDto(String carId, EncarDto encarDto);

    EncarDto getEncarDtoByCarId(String carId);

    void deleteEncarDtoByCarId(String carId);
    
    List<String> getAllKeys();
}
