package ru.wallentos.carworker.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Data
public class SubscribeService {
    @Autowired
    private RedisCacheService redisCacheService;
    
    @Value("${ru.wallentos.carworker.user-prefix}")
    private String userPrefix;


    public boolean isSubscriber(){
        return true;
    }
}
