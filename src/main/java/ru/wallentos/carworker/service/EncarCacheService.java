package ru.wallentos.carworker.service;

import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.wallentos.carworker.cache.EncarCache;
import ru.wallentos.carworker.exceptions.GetCarDetailException;
import ru.wallentos.carworker.exceptions.RecaptchaException;
import ru.wallentos.carworker.model.CarDto;

/**
 * Сервис по работе с КЭШом данных encar
 */
@Service
@Slf4j
public class EncarCacheService {
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
    public CarDto fetchAndUpdateEncarDtoByCarId(String carId) throws GetCarDetailException, RecaptchaException { // сделать приватным
        CarDto result = encarCache.getById(carId);
        if (Objects.nonNull(result)) {
            log.info("Получили в КЭШе авто id {}", carId);
            return result;
        }
        log.info("Попробуем найти и добавить в КЭШ автомобиль с id {}", carId);
        try {
            result = restService.getEncarDataByJsoup(carId);
        } catch (GetCarDetailException e) {
            encarCache.deleteById(carId);
            throw new GetCarDetailException(e.getMessage());
        }
        encarCache.save(carId, result);
        return result;
    }

    /**
     * Запускаем обновление КЭШа.
     */
    public void updateEncarCache() {
        List<String> allKeys = encarCache.getAllKeys();
        log.info("Received {} keys to update", allKeys.size());
        allKeys.forEach(this::updateEncarCacheByCarId);
    }

    /**
     * Запускаем обновление КЭШа.
     */
    private void updateEncarCacheByCarId(String carId) { // 
        CarDto carDto;
        try {
            carDto = restService.getEncarDataByJsoup(carId);
            encarCache.save(carId, carDto);
            log.info("Обновлены данные для тачки:{}", carDto);
        } catch (GetCarDetailException e) {
            log.error("can not update car ");
            encarCache.deleteById(carId);
        } catch (RecaptchaException e) {
            throw new RuntimeException(e);
        }
    }
}
