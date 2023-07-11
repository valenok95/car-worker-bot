package ru.wallentos.carworker.service;

import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.wallentos.carworker.cache.EncarCache;
import ru.wallentos.carworker.exceptions.GetCarDetailException;
import ru.wallentos.carworker.model.EncarDto;

@Service
@Slf4j
public class RedisCacheService {
    @Autowired
    private EncarCache encarCache;
    @Autowired
    private RestService restService;

    /**
     * Пытаемся получить encarDto из КЭШа. Если в КЭШе пусто - получаем с сайта и обновляем КЭШ.
     *
     * @param carId
     * @return
     */
    public EncarDto fetchAndUpdateEncarDtoByCarId(String carId) throws GetCarDetailException { // сделать приватным
        EncarDto result = encarCache.getEncarDtoByCarId(carId);
        if (Objects.nonNull(result)) {
            log.info("Получили в КЭШе авто id {}", carId);
            return result;
        }
        log.info("Попробуем найти и добавить в КЭШ автомобиль с id {}", carId);
        result = restService.getEncarDataByJsoup(carId);
        encarCache.saveEncarDto(carId, result);
        return result;
    }

    /**
     * Запускаем обновление КЭШа.
     */
    public long updateEncarCache() {
        List<String> allKeys = encarCache.getAllKeys();
        allKeys.forEach(carId -> {
            try {
                updateEncarCacheByCarId(carId);
            } catch (GetCarDetailException e) {
                throw new RuntimeException(e);
            }
        });
        return allKeys.size();
    }

    /**
     * Запускаем обновление КЭШа.
     */
    private void updateEncarCacheByCarId(String carId) throws GetCarDetailException { // 
        var encarDto = restService.getEncarDataByJsoup(carId);
        encarCache.saveEncarDto(carId, encarDto);
        log.info("Обновлены данные для тачки:{}", encarDto);
    }
}
