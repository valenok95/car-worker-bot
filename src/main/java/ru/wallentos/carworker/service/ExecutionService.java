package ru.wallentos.carworker.service;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.wallentos.carworker.model.CarPriceResultData;
import ru.wallentos.carworker.model.UserCarData;

@Service
@RequiredArgsConstructor
public class ExecutionService {
    @Value("${ru.wallentos.carworker.exchange-coefficient}")
    private double coefficient;
    private final RestService restService;
    @Value("${ru.wallentos.carworker.extra-pay-china.cny}")
    private int EXTRA_PAY_AMOUNT_CHINA_CNY;
    @Value("${ru.wallentos.carworker.extra-pay-china.rub}")
    private int EXTRA_PAY_AMOUNT_CHINA_RUB;
    @Value("${ru.wallentos.carworker.extra-pay-corea.krw}")
    private int EXTRA_PAY_AMOUNT_KOREA_KRW;
    @Value("${ru.wallentos.carworker.extra-pay-corea.rub}")
    private int EXTRA_PAY_AMOUNT_KOREA_RUB;
    private static final int NEW_CAR_RECYCLING_FEE = 3400;
    private static final int OLD_CAR_RECYCLING_FEE = 5200;
    private static final int CUSTOMS_VALUE_1 = 200_000;
    private static final String RUB = "RUB";
    private static final String USD = "USD";
    private static final String KRW = "KRW";
    private static final String CNY = "CNY";
    private static final int FEE_RATE_1 = 775;
    private static final int CUSTOMS_VALUE_2 = 450_000;
    private static final int FEE_RATE_2 = 1550;
    private static final int CUSTOMS_VALUE_3 = 1_200_000;
    private static final int FEE_RATE_3 = 3100;
    private static final int CUSTOMS_VALUE_4 = 2_700_000;
    private static final int FEE_RATE_4 = 8530;
    private static final int CUSTOMS_VALUE_5 = 4_200_000;
    private static final int FEE_RATE_5 = 12000;
    private static final int CUSTOMS_VALUE_6 = 5_500_000;
    private static final int FEE_RATE_6 = 15500;
    private static final int CUSTOMS_VALUE_7 = 7_000_000;
    private static final int FEE_RATE_7 = 20000;
    private static final int CUSTOMS_VALUE_8 = 8_000_000;
    private static final int FEE_RATE_8 = 23000;
    private static final int CUSTOMS_VALUE_9 = 9_000_000;
    private static final int FEE_RATE_9 = 25000;
    private static final int CUSTOMS_VALUE_10 = 10_000_000;
    private static final int FEE_RATE_10 = 27000;
    private static final int LAST_FEE_RATE = 30000;
    private static final int NEW_CAR_PRICE_DUTY_1 = 8500;
    private static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_1 =
            new AbstractMap.SimpleEntry<>(0.54, 2.5);
    private static final int NEW_CAR_PRICE_DUTY_2 = 16700;
    private static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_2 =
            new AbstractMap.SimpleEntry<>(0.48, 3.5);
    private static final int NEW_CAR_PRICE_DUTY_3 = 42300;
    private static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_3 =
            new AbstractMap.SimpleEntry<>(0.48, 5.5);
    private static final int NEW_CAR_PRICE_DUTY_4 = 84500;
    private static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_4 =
            new AbstractMap.SimpleEntry<>(0.48, 7.5);
    private static final int NEW_CAR_PRICE_DUTY_5 = 169000;
    private static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_5 =
            new AbstractMap.SimpleEntry<>(0.48, 15d);
    private static final Map.Entry<Double, Double> NEW_CAR_PRICE_MAX_FLAT_RATE =
            new AbstractMap.SimpleEntry<>(0.48, 20d);
    private static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_1 = 1000;
    private static final double NORMAL_CAR_PRICE_FLAT_RATE_1 = 1.5;
    private static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_2 = 1500;
    private static final double NORMAL_CAR_PRICE_FLAT_RATE_2 = 1.7;
    private static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_3 = 1800;
    private static final double NORMAL_CAR_PRICE_FLAT_RATE_3 = 2.5;
    private static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_4 = 2300;
    private static final double NORMAL_CAR_PRICE_FLAT_RATE_4 = 2.7;
    private static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_5 = 3000;
    private static final double NORMAL_CAR_PRICE_FLAT_RATE_5 = 3;
    private static final double NORMAL_CAR_PRICE_FLAT_RATE_MAX = 3.6;
    private static final int OLD_CAR_ENGINE_VOLUME_DUTY_1 = 1000;
    private static final double OLD_CAR_PRICE_FLAT_RATE_1 = 3;
    private static final int OLD_CAR_ENGINE_VOLUME_DUTY_2 = 1500;
    private static final double OLD_CAR_PRICE_FLAT_RATE_2 = 3.2;
    private static final int OLD_CAR_ENGINE_VOLUME_DUTY_3 = 1800;
    private static final double OLD_CAR_PRICE_FLAT_RATE_3 = 3.5;
    private static final int OLD_CAR_ENGINE_VOLUME_DUTY_4 = 2300;
    private static final double OLD_CAR_PRICE_FLAT_RATE_4 = 4.8;
    private static final int OLD_CAR_ENGINE_VOLUME_DUTY_5 = 3000;
    private static final double OLD_CAR_PRICE_FLAT_RATE_5 = 5;
    private static final double OLD_CAR_PRICE_FLAT_RATE_MAX = 5.7;

    public static final String NEW_CAR = "До 3 лет";
    public static final String NORMAL_CAR = "От 3 до 5 лет";
    public static final String OLD_CAR = "От 5 лет";
    public static final String RESET_MESSAGE = "Рассчитать ещё один автомобиль";
    public static final String TO_START_MESSAGE = "Рассчитать автомобиль";
    public static final String MANAGER_MESSAGE = "Связаться с менеджером";

    /**
     * Карта рассчёта таможенной стоимости.
     */
    private static final Map<Integer, Integer> feeRateMap = new LinkedHashMap<>() {
        {
            put(CUSTOMS_VALUE_1, FEE_RATE_1);
            put(CUSTOMS_VALUE_2, FEE_RATE_2);
            put(CUSTOMS_VALUE_3, FEE_RATE_3);
            put(CUSTOMS_VALUE_4, FEE_RATE_4);
            put(CUSTOMS_VALUE_5, FEE_RATE_5);
            put(CUSTOMS_VALUE_6, FEE_RATE_6);
            put(CUSTOMS_VALUE_7, FEE_RATE_7);
            put(CUSTOMS_VALUE_8, FEE_RATE_8);
            put(CUSTOMS_VALUE_9, FEE_RATE_9);
            put(CUSTOMS_VALUE_10, FEE_RATE_10);
        }
    };

    /**
     * Карта рассчёта размера пошлины для нового автомобиля.
     */
    private static final Map<Integer, Map.Entry<Double, Double>> newCarCustomsMap = new LinkedHashMap<>() {
        {
            put(NEW_CAR_PRICE_DUTY_1, NEW_CAR_PRICE_FLAT_RATE_1);
            put(NEW_CAR_PRICE_DUTY_2, NEW_CAR_PRICE_FLAT_RATE_2);
            put(NEW_CAR_PRICE_DUTY_3, NEW_CAR_PRICE_FLAT_RATE_3);
            put(NEW_CAR_PRICE_DUTY_4, NEW_CAR_PRICE_FLAT_RATE_4);
            put(NEW_CAR_PRICE_DUTY_5, NEW_CAR_PRICE_FLAT_RATE_5);
        }
    };
    /**
     * Карта рассчёта размера пошлины для автомобиля от 3 до 5 лет.
     */
    private static final Map<Integer, Double> normalCarCustomsMap =
            new LinkedHashMap<>() {
                {
                    put(NORMAL_CAR_ENGINE_VOLUME_DUTY_1, NORMAL_CAR_PRICE_FLAT_RATE_1);
                    put(NORMAL_CAR_ENGINE_VOLUME_DUTY_2, NORMAL_CAR_PRICE_FLAT_RATE_2);
                    put(NORMAL_CAR_ENGINE_VOLUME_DUTY_3, NORMAL_CAR_PRICE_FLAT_RATE_3);
                    put(NORMAL_CAR_ENGINE_VOLUME_DUTY_4, NORMAL_CAR_PRICE_FLAT_RATE_4);
                    put(NORMAL_CAR_ENGINE_VOLUME_DUTY_5, NORMAL_CAR_PRICE_FLAT_RATE_5);
                }
            };
    /**
     * Карта рассчёта размера пошлины для автомобиля от 5 лет.
     */
    private static final Map<Integer, Double> oldCarCustomsMap =
            new LinkedHashMap<>() {
                {
                    put(OLD_CAR_ENGINE_VOLUME_DUTY_1, OLD_CAR_PRICE_FLAT_RATE_1);
                    put(OLD_CAR_ENGINE_VOLUME_DUTY_2, OLD_CAR_PRICE_FLAT_RATE_2);
                    put(OLD_CAR_ENGINE_VOLUME_DUTY_3, OLD_CAR_PRICE_FLAT_RATE_3);
                    put(OLD_CAR_ENGINE_VOLUME_DUTY_4, OLD_CAR_PRICE_FLAT_RATE_4);
                    put(OLD_CAR_ENGINE_VOLUME_DUTY_5, OLD_CAR_PRICE_FLAT_RATE_5);
                }
            };


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
            System.out.println(pair.getKey() + " " + pair.getValue());
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
        resultData.setDuty(calculateDutyInRubles(userCarData.getPriceInEuro(),
                getCarCategory(userCarData.getAge()), userCarData.getVolume()));
        resultData.setRecyclingFee(calculateRecyclingFeeInRubles(getCarCategory(userCarData.getAge())));
        resultData.setFirstPriceInRubles(convertMoneyFromEuro(userCarData.getPriceInEuro(), RUB),
                coefficient);
        resultData.setExtraPayAmount(executeExtraPayAmountInRubles(userCarData.getConcurrency()));
        resultData.setStock(executeStock(userCarData.getConcurrency()));
        resultData.setLocation(executeLocation(userCarData.getConcurrency()));

        return resultData;
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

    private double executeExtraPayAmountInRubles(String councurrency) {
        switch (councurrency) {
            case KRW:
            case USD:
                return convertMoneyFromEuro(convertMoneyToEuro(EXTRA_PAY_AMOUNT_KOREA_KRW, KRW),
                        RUB) * coefficient + EXTRA_PAY_AMOUNT_KOREA_RUB;
            default:
                return convertMoneyFromEuro(convertMoneyToEuro(EXTRA_PAY_AMOUNT_CHINA_CNY, CNY),
                        RUB) * coefficient + EXTRA_PAY_AMOUNT_CHINA_RUB;
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
        double resultCarDuty = getMaxFromPair(NEW_CAR_PRICE_MAX_FLAT_RATE, carPriceInEuro, carVolume);
        for (Map.Entry<Integer, Map.Entry<Double, Double>> pair : newCarCustomsMap.entrySet()) {
            System.out.println(pair.getKey() + " " + pair.getValue());
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
            System.out.println(pair.getKey() + " " + pair.getValue());
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
            System.out.println(pair.getKey() + " " + pair.getValue());
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

    /**
     * Курс CNY к рублю.
     */
    public double getCnyRub() {
        return coefficient * restService.getConversionRatesMap().get(RUB) / restService.getConversionRatesMap().get(CNY);
    }

    /**
     * Курс USD к рублю.
     */
    public double getUsdRub() {
        return coefficient * restService.getConversionRatesMap().get(RUB) / restService.getConversionRatesMap().get(USD);
    }

    /**
     * Курс KRW к рублю.
     */
    public double getKrwRub() {
        return coefficient * restService.getConversionRatesMap().get(RUB) / restService.getConversionRatesMap().get(KRW);
    }


}
