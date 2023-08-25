package ru.wallentos.carworker.cache;


import java.util.List;
import ru.wallentos.carworker.model.EncarDto;

public interface RedisCache {
    void save(String id, Object value);

    Object getById(String id);

    void deleteById(String id);
    
    List<String> getAllKeys();
}
