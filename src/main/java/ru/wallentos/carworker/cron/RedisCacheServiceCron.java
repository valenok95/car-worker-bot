package ru.wallentos.carworker.cron;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.wallentos.carworker.service.RedisCacheService;

@Component
@Slf4j
public class RedisCacheServiceCron {
    @Value("${ru.wallentos.carworker.update-cache-job.master-bot}")
    private boolean isMasterBot;

    @Autowired
    RedisCacheService redisCacheService;

    /**
     * Задание на обновление КЭШа для мастер-бота.
     */
    @Scheduled(cron = "${ru.wallentos.carworker.update-cache-job.cron}")
    public void updateCache() {
        if (isMasterBot) {
            log.info("Начинаем обновление КЭШа");
            redisCacheService.updateEncarCache();
        }
    }
}
