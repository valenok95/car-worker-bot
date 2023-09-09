package ru.wallentos.carworker.service;

import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.wallentos.carworker.cache.CheCarCache;
import ru.wallentos.carworker.exceptions.GetCarDetailException;
import ru.wallentos.carworker.exceptions.RecaptchaException;
import ru.wallentos.carworker.model.CarDto;

/**
 * Сервис для работы с данными che168.com .
 */
@Service
@Slf4j
public class CheCarCacheService {
    @Autowired
    private CheCarCache cheCarCache;
    @Autowired
    private RestService restService;

    /**
     * Пытаемся получить carDto из КЭШа. Если в КЭШе пусто - получаем с сайта и обновляем КЭШ.
     *
     * @param carId
     * @return
     */
    public CarDto fetchAndUpdateCheCarDtoByCarId(String carId) throws GetCarDetailException,
            RecaptchaException { // сделать приватным
        CarDto result = cheCarCache.getById(carId);
        if (Objects.nonNull(result)) {
            log.info("Получили в КЭШе авто id {}", carId);
            return result;
        }
        log.info("Попробуем найти и добавить в КЭШ автомобиль с id {}", carId);
        result = restService.getCheDataByJsoup(carId);
        cheCarCache.save(carId, result);
        return result;
    }

    /**
     * Запускаем обновление КЭШа.
     */
    public void updateCheCarCache() {
        List<String> allKeys = cheCarCache.getAllKeys();
        log.info("Received {} keys to update", allKeys.size());
        allKeys.forEach(this::updateCheCarCacheByCarId);
    }

    /**
     * Запускаем обновление КЭШа.
     */
    private void updateCheCarCacheByCarId(String carId) {
        CarDto carDto;
        try {
            carDto = restService.getCheDataByJsoup(carId);
            cheCarCache.save(carId, carDto);
            log.info("Обновлены данные для тачки:{}", carDto);
        } catch (GetCarDetailException e) {
            log.error("can not update car ");
            cheCarCache.deleteById(carId);
        } catch (RecaptchaException e) {
            throw new RuntimeException(e);
        }
    }
}
