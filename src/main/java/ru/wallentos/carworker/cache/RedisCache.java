package ru.wallentos.carworker.cache;


import java.util.List;

public interface RedisCache {
    void save(String id, Object value);

    Object getById(String id);

    void deleteById(String id);

    List<String> getAllKeys();
}
