package ru.wallentos.carworker.service;

import static ru.wallentos.carworker.configuration.ConfigDataPool.CNY;
import static ru.wallentos.carworker.configuration.ConfigDataPool.KRW;
import static ru.wallentos.carworker.configuration.ConfigDataPool.LAST_FEE_RATE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NEW_CAR;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NEW_CAR_RECYCLING_FEE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NORMAL_CAR;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NORMAL_CAR_PRICE_FLAT_RATE_MAX;
import static ru.wallentos.carworker.configuration.ConfigDataPool.OLD_CAR_PRICE_FLAT_RATE_MAX;
import static ru.wallentos.carworker.configuration.ConfigDataPool.OLD_CAR_RECYCLING_FEE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.RUB;
import static ru.wallentos.carworker.configuration.ConfigDataPool.USD;
import static ru.wallentos.carworker.configuration.ConfigDataPool.feeRateMap;
import static ru.wallentos.carworker.configuration.ConfigDataPool.newCarCustomsMap;
import static ru.wallentos.carworker.configuration.ConfigDataPool.normalCarCustomsMap;
import static ru.wallentos.carworker.configuration.ConfigDataPool.oldCarCustomsMap;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.wallentos.carworker.configuration.ConfigDataPool;
import ru.wallentos.carworker.model.CarPriceResultData;
import ru.wallentos.carworker.model.UserCarData;

@Service
public class ExecutionService {
    private RestService restService;
    private ConfigDataPool configDataPool;

    @Autowired
    public ExecutionService(RestService restService, ConfigDataPool configDataPool) {
        this.restService = restService;
        this.configDataPool = configDataPool;
        restService.refreshExchangeRates();
        double rub = restService.getConversionRatesMap().get("RUB");
        restService.getConversionRatesMap().forEach((key, value) -> {
            configDataPool.manualConversionRatesMapInRubles.put(key, rub * configDataPool.coefficient / value);
        });
    }

    /**
     * Сбор за таможенные операции.
     * Первая составляющая для конечного рассчёта.
     *
     * @param rawCarPriceInEuro
     * @return
     */
    private double getFeeRateFromCarPriceInRubles(double rawCarPriceInEuro) {
        double carPriceInRubles = convertMoneyFromEuro(rawCarPriceInEuro, RUB);
        int resultFeeRate = LAST_FEE_RATE;
        for (Map.Entry<Integer, Integer> pair : feeRateMap.entrySet()) {
            if (carPriceInRubles < pair.getKey()) {
                resultFeeRate = pair.getValue();
                break;
            }
        }
        return resultFeeRate;
    }

    public CarPriceResultData executeCarPriceResultData(UserCarData userCarData) {
        CarPriceResultData resultData = new CarPriceResultData();
        resultData.setCarCategory(getCarCategory(userCarData.getAge()));
        resultData.setAge(userCarData.getAge());
        resultData.setFeeRate(getFeeRateFromCarPriceInRubles(userCarData.getPriceInEuro()));
        resultData.setDuty(calculateDutyInRubles(userCarData.getPriceInEuro(), getCarCategory(userCarData.getAge()), userCarData.getVolume()));
        resultData.setRecyclingFee(calculateRecyclingFeeInRubles(getCarCategory(userCarData.getAge())));
        resultData.setFirstPriceInRubles(calculateFirstCarPriceInRublesByUserCarData(userCarData));
        resultData.setExtraPayAmount(executeExtraPayAmountInRublesByUserCarData(userCarData));
        resultData.setStock(executeStock(userCarData.getConcurrency()));
        resultData.setLocation(executeLocation(userCarData.getConcurrency()));

        return resultData;
    }

    private double calculateFirstCarPriceInRublesByUserCarData(UserCarData userCarData) {
        return userCarData.getPrice() * configDataPool.manualConversionRatesMapInRubles.get(userCarData.getConcurrency());
    }

//стоимость которую ввел пользователь + extra pay 

    //пошлина


    public String executeStock(String concurrency) {
        switch (concurrency) {
            case KRW:
            case USD:
                return "Корея";
            default:
                return "Китай";
        }
    }

    public String executeLocation(String concurrency) {
        switch (concurrency) {
            case KRW:
            case USD:
                return "до Владивостока";
            default:
                return "до Уссурийска";
        }
    }

    private double executeExtraPayAmountInRublesByUserCarData(UserCarData userCarData) {
        switch (userCarData.getConcurrency()) {
            case KRW:
            case USD:
                return configDataPool.EXTRA_PAY_AMOUNT_KOREA_KRW * configDataPool.manualConversionRatesMapInRubles.get(KRW)
                        + configDataPool.EXTRA_PAY_AMOUNT_KOREA_RUB;
            default:
                return configDataPool.EXTRA_PAY_AMOUNT_CHINA_CNY * configDataPool.manualConversionRatesMapInRubles.get(CNY)
                        + configDataPool.EXTRA_PAY_AMOUNT_CHINA_RUB;
        }
    }


    private double convertMoneyFromEuro(double count, String toConcurrency) {
        return count * restService.getConversionRatesMap().get(toConcurrency);
    }

    public double convertMoneyToEuro(double count, String fromConcurrency) {
        return count / restService.getConversionRatesMap().get(fromConcurrency);
    }

    /**
     * Вычисляем пошлину авто.
     *
     * @param rawCarPriceInEuro стоимость авто
     * @param carCategory       возврастная категория авто
     * @return стоимость пошлины
     */
    private double calculateDutyInRubles(double rawCarPriceInEuro, int carCategory, int carVolume) {
        if (carCategory == 1) {
            return calculateNewCarDutyInRubles(rawCarPriceInEuro, carVolume);
        } else if (carCategory == 2) {
            return calculateNormalCarDutyInRubles(carVolume);
        } else {
            return calculateOldCarDutyInRubles(carVolume);
        }
    }

    /**
     * Вычисляем пошлину авто до 3 лет.
     *
     * @param carPriceInEuro стоимость авто
     * @return стоимость пошлины
     */
    private double calculateNewCarDutyInRubles(double carPriceInEuro, int carVolume) {
        double resultCarDuty = getMaxFromPair(ConfigDataPool.NEW_CAR_PRICE_MAX_FLAT_RATE, carPriceInEuro, carVolume);
        for (Map.Entry<Integer, Map.Entry<Double, Double>> pair : newCarCustomsMap.entrySet()) {
            if (carPriceInEuro <= pair.getKey()) {
                resultCarDuty = getMaxFromPair(pair.getValue(), carPriceInEuro, carVolume);
                break;
            }
        }
        return convertMoneyFromEuro(resultCarDuty, RUB);
    }

    /**
     * Вычисляем пошлину авто от 3 до 5 лет.
     *
     * @param carVolume объем двигателя
     * @return стоимость пошлины
     */
    private double calculateNormalCarDutyInRubles(int carVolume) {
        double resultCarDuty = carVolume * NORMAL_CAR_PRICE_FLAT_RATE_MAX;
        for (Map.Entry<Integer, Double> pair : normalCarCustomsMap.entrySet()) {
            if (carVolume <= pair.getKey()) {
                resultCarDuty = carVolume * pair.getValue();
                break;
            }
        }
        return convertMoneyFromEuro(resultCarDuty, RUB);
    }

    /**
     * Вычисляем пошлину авто от 5 лет.
     *
     * @param carVolume объем двигателя
     * @return стоимость пошлины
     */
    private double calculateOldCarDutyInRubles(int carVolume) {
        double resultCarDuty = carVolume * OLD_CAR_PRICE_FLAT_RATE_MAX;
        for (Map.Entry<Integer, Double> pair : oldCarCustomsMap.entrySet()) {
            if (carVolume <= pair.getKey()) {
                resultCarDuty = carVolume * pair.getValue();
                break;
            }
        }
        return convertMoneyFromEuro(resultCarDuty, RUB);
    }

    /**
     * Рассчёт утилизационного сбора.
     *
     * @param carCategory категория авто
     * @return стоимость утилизационного сбора
     */
    private int calculateRecyclingFeeInRubles(int carCategory) {
        return carCategory > 1 ? OLD_CAR_RECYCLING_FEE : NEW_CAR_RECYCLING_FEE;
    }

    /**
     * Вычисляем возрастную категорию авто.
     * 1- до трех лет
     * 2- от трех до пяти лет
     * 3- свыше пяти лет
     *
     * @param carAge
     * @return возрастная категория
     */
    private int getCarCategory(String carAge) {
        switch (carAge) {
            case NEW_CAR:
                return 1;
            case NORMAL_CAR:
                return 2;
            default:
                return 3;
        }
    }

    /**
     * Считаем наибольшее значение пошлины, либо по кубам либо по умножению на процент.
     */
    private double getMaxFromPair(Map.Entry<Double, Double> pair, double carPriceInEuro, int carVolume) {
        double priceByPercent = pair.getKey() * carPriceInEuro;
        double priceByVolume = pair.getValue() * carVolume;
        return Math.max(priceByVolume, priceByPercent);
    }
}
