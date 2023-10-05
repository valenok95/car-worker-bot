package ru.wallentos.carworker.service;

import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.wallentos.carworker.cache.SubscriptionCache;
import ru.wallentos.carworker.configuration.ConfigDataPool;

@Slf4j
@Service
@Data
public class SubscribeService {
    public String mailingText;
    public String photoData;
    @Autowired
    private SubscriptionCache subscriptionCache;
    @Autowired
    private ConfigDataPool configDataPool;

    public void subscribeUser(long userId) {
        subscriptionCache.save(String.valueOf(userId), null);
    }

    public void unSubscribeUser(long userId) {
        subscriptionCache.deleteById(String.valueOf(userId));
    }

    public List<Long> getSubscribers() {
        return subscriptionCache.getAllKeys().stream().map(Long::parseLong).toList();
    }

    public void cleanData() {
        mailingText = null;
        photoData = null;
    }

}
