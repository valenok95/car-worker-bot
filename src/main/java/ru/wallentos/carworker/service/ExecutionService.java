package ru.wallentos.carworker.service;

import static ru.wallentos.carworker.configuration.ConfigDataPool.CNY;
import static ru.wallentos.carworker.configuration.ConfigDataPool.KRW;
import static ru.wallentos.carworker.configuration.ConfigDataPool.LAST_FEE_RATE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NEW_BIG_CAR_RECYCLING_FEE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NEW_CAR;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NEW_CAR_RECYCLING_FEE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NEW_MID_CAR_RECYCLING_FEE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NORMAL_BIG_CAR_RECYCLING_FEE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NORMAL_CAR;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NORMAL_CAR_PRICE_FLAT_RATE_MAX;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NORMAL_MID_CAR_RECYCLING_FEE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.OLD_CAR;
import static ru.wallentos.carworker.configuration.ConfigDataPool.OLD_CAR_PRICE_FLAT_RATE_MAX;
import static ru.wallentos.carworker.configuration.ConfigDataPool.OLD_CAR_RECYCLING_FEE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.RUB;
import static ru.wallentos.carworker.configuration.ConfigDataPool.USD;
import static ru.wallentos.carworker.configuration.ConfigDataPool.feeRateMap;
import static ru.wallentos.carworker.configuration.ConfigDataPool.manualConversionRatesMapInRubles;
import static ru.wallentos.carworker.configuration.ConfigDataPool.newCarCustomsMap;
import static ru.wallentos.carworker.configuration.ConfigDataPool.normalCarCustomsMap;
import static ru.wallentos.carworker.configuration.ConfigDataPool.oldCarCustomsMap;

import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.wallentos.carworker.configuration.ConfigDataPool;
import ru.wallentos.carworker.model.CarPriceResultData;
import ru.wallentos.carworker.model.CarTotalResultData;
import ru.wallentos.carworker.model.Province;
import ru.wallentos.carworker.model.UserCarInputData;

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
            ConfigDataPool.manualConversionRatesMapInRubles.put(key, rub * configDataPool.coefficient / value);
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

    /**
     * Расчёт стоимости автомобиля под ключ с учётом всех расходов.
     *
     * @param userCarInputData
     * @return
     */
    public CarPriceResultData executeCarPriceResultData(UserCarInputData userCarInputData) {
        CarPriceResultData resultData = new CarPriceResultData();
        resultData.setCurrency(userCarInputData.getCurrency());
        resultData.setCarId(userCarInputData.getCarId());
        resultData.setCarCategory(getCarCategory(userCarInputData.getAge()));
        resultData.setAge(userCarInputData.getAge());
        if (KRW.equals(userCarInputData.getCurrency())) {
            userCarInputData.setSanctionCar(isSanctionCar(userCarInputData.getPrice()));
        }
        resultData.setAge(userCarInputData.getAge());
        resultData.setFeeRate(getFeeRateFromCarPriceInRubles(userCarInputData.getPriceInEuro()));
        resultData.setDuty(calculateDutyInRubles(userCarInputData.getPriceInEuro(), getCarCategory(userCarInputData.getAge()), userCarInputData.getVolume()));
        resultData.setRecyclingFee(calculateRecyclingFeeInRubles(getCarCategory(userCarInputData.getAge()), userCarInputData.getVolume()));
        resultData.setFirstPriceInRubles(calculateFirstCarPriceInRublesByUserCarData(userCarInputData));
//валютная надбавка  и рублёвая надбавка (Брокерские расходы, СВХ, СБКТС)
        double extraPayAmountRublePart = executeRubExtraPayAmountInRublesByUserCarData(userCarInputData.getCurrency());
        double extraPayAmountCurrencyPart = executeValuteExtraPayAmountInRublesByUserCarData(userCarInputData.getCurrency(), userCarInputData.isSanctionCar());
        resultData.setExtraPayAmountRublePart(extraPayAmountRublePart);
        resultData.setExtraPayAmountValutePart(extraPayAmountCurrencyPart);
        // Стоимость логистики из провинции Китая
        if (Objects.nonNull(userCarInputData.getProvince())) {
            resultData.setProvincePriceInRubles(executeProvincePriceInRubles(userCarInputData.getCurrency(), userCarInputData.getProvince()));
            resultData.setProvinceName(userCarInputData.getProvince().getProvinceFullName());
        }

        resultData.setExtraPayAmountValutePart(extraPayAmountRublePart + extraPayAmountCurrencyPart);
        resultData.setStock(executeStock(userCarInputData.getCurrency()));
        resultData.setLocation(executeLocation(userCarInputData.getCurrency()));
        return resultData;
    }

    /**
     * Расчёт для менеджеров без рублей в ЮАНЯХ.
     *
     * @param userCarInputData
     * @return
     */
    public CarTotalResultData executeCarTotalResultData(UserCarInputData userCarInputData) {
        CarTotalResultData resultData = new CarTotalResultData();
        resultData.setCarId(userCarInputData.getCarId());
        resultData.setCnyPrice(userCarInputData.getPrice());
        resultData.setDollarPrice(userCarInputData.getPrice()/ conversionRatesMap.get(USD));
        // Стоимость логистики из провинции Китая
            resultData.setProvinceName(userCarInputData.getProvince().getProvinceFullName());
        return resultData;
    }

    /**
     * Расчитываем логистику для доставки по провинциям Китая.
     *
     * @param currency
     * @param province
     * @return
     */
    private double executeProvincePriceInRubles(String currency, Province province) {
        return manualConversionRatesMapInRubles.get(currency) * province.getProvincePriceInCurrency();
    }


    /**
     * Расчёт ставки аукциона исходя из данных пользователя.
     *
     * @param userCarInputData
     * @return
     */
    public int executeAuctionResultInKrw(UserCarInputData userCarInputData) {
        double resultAuctionPriceInKrw;
        double auctionCleanPriceInRub = userCarInputData.getUserAuctionStartPrice()
                - executeRubExtraPayAmountInRublesByUserCarData(KRW)
                - 100_000 // Брокерские расходы 100 тыс. руб.
                - executeValuteExtraPayAmountInRublesByUserCarData(KRW, true);
        double dutyAuctionInRubles;
        // Для новой тачки считаем таможню в евро от 70% чистой стоимости (без комиссий)
        // Для авто от 3 лет считаем таможню как для цены 10 000EUR
        if (userCarInputData.getAge().equals(NEW_CAR)) {
            dutyAuctionInRubles =
                    calculateDutyInRubles(convertMoneyToEuro(auctionCleanPriceInRub * (1 - configDataPool.auctionCoefficient), RUB),
                            getCarCategory(userCarInputData.getAge()), userCarInputData.getVolume());
        } else {
            dutyAuctionInRubles = calculateDutyInRubles(10_000, getCarCategory(userCarInputData.getAge()), userCarInputData.getVolume());
        }
        double resultAuctionPriceInRub = auctionCleanPriceInRub - dutyAuctionInRubles;
        resultAuctionPriceInKrw =
                (resultAuctionPriceInRub) / ConfigDataPool.manualConversionRatesMapInRubles.get(KRW);

        return roundDoubleToInt(resultAuctionPriceInKrw, 50_000); // округляем в меньшую сторону
    }

    /**
     * Округлить число
     *
     * @param resultAuctionPriceInKrw
     */
    private int roundDoubleToInt(double resultAuctionPriceInKrw, int round) {
        return ((int) resultAuctionPriceInKrw / round) * round;
    }

    private double calculateFirstCarPriceInRublesByUserCarData(UserCarInputData userCarInputData) {
        String currentCurrency = userCarInputData.getCurrency();
        if (currentCurrency.equals(KRW) && !userCarInputData.isSanctionCar()) {
            return (userCarInputData.getPrice() / restService.getCbrUsdKrwMinus20()) * ConfigDataPool.manualConversionRatesMapInRubles.get(USD);
        } else {
            return userCarInputData.getPrice() * ConfigDataPool.manualConversionRatesMapInRubles.get(currentCurrency);
        }
    }

    private boolean isSanctionCar(double priceInKrw) {
        return priceInKrw / restService.getCbrUsdKrwMinus20() > 50_000;
    }

//стоимость которую ввел пользователь + extra pay 

    //пошлина

    /**
     * Определяем рынок по валюте
     *
     * @param currency
     * @return
     */
    public String executeStock(String currency) {
        switch (currency) {
            case KRW, USD:
                return "Корея";
            case CNY:
                return "Китай";
            default:
                return "Неизвестный регион";
        }
    }

    /**
     * Опрежеляем до куда доставка
     *
     * @param currency
     * @return
     */
    public String executeLocation(String currency) {
        switch (currency) {
            case KRW, USD:
                return "до Владивостока";
            case CNY:
                return "до Уссурийска";
            default:
                return "неизвестно до куда";
        }
    }

    /**
     * Рассчитываем доп взносы. Рублёвая часть. Брокерские расходы, СВХ, СБКТС.
     */
    private double executeRubExtraPayAmountInRublesByUserCarData(String currency) {
        switch (currency) {
            case KRW, USD:
                return configDataPool.EXTRA_PAY_AMOUNT_KOREA_RUB;
            case CNY:
                return configDataPool.EXTRA_PAY_AMOUNT_CHINA_RUB;
            default:
                return 0;
        }
    }


    /**
     * Рассчитываем доп взносы. Валютная часть. если тачка дешевле 50 000 USD - то двойная
     * конвертация.
     */
    private double executeValuteExtraPayAmountInRublesByUserCarData(String currency, boolean isSanctionCar) {
        switch (currency) {
            case KRW:
                return (configDataPool.disableDoubleConvertation || isSanctionCar ? getExtraKrwPayAmountNormalConvertation() : getExtraKrwPayAmountDoubleConvertation());
            case USD:
                return getExtraKrwPayAmountNormalConvertation();
            case CNY:
                return configDataPool.EXTRA_PAY_AMOUNT_CHINA_CNY * ConfigDataPool.manualConversionRatesMapInRubles.get(CNY);
            default:
                return 0;
        }
    }

    private double getExtraKrwPayAmountNormalConvertation() {
        return configDataPool.EXTRA_PAY_AMOUNT_KOREA_KRW * ConfigDataPool.manualConversionRatesMapInRubles.get(KRW);
    }

    /**
     * Если эквивалент тачки стоит меньше, чем 50 000$ то (KRW для взносов делим на (курс KRW/USD по
     * ЦБ минус 20) и умножаем на ручной курс USD.
     */
    private double getExtraKrwPayAmountDoubleConvertation() {
        double usdAmount = configDataPool.EXTRA_PAY_AMOUNT_KOREA_KRW / restService.getCbrUsdKrwMinus20();
        return usdAmount * ConfigDataPool.manualConversionRatesMapInRubles.get(USD);
    }


    private double convertMoneyFromEuro(double count, String toCurrency) {
        return count * restService.getConversionRatesMap().get(toCurrency);
    }

    public double convertMoneyToEuro(double count, String fromCurrency) {
        return count / restService.getConversionRatesMap().get(fromCurrency);
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
     * <p>
     * На автомобили до трех лет, объемом двигателя от 3001 до 3500 включительно утилизационный
     * сбор составляет теперь 970 000₽
     * Так же до трех лет объемом от 3501 утилизационный сбор составляет 1 235 200₽
     * На автомобили от 3 до 5 лет
     * Объемом двигателя от 3001 до 3500 включительно утилизационный сбор составляет 1 485 000₽
     * Так же от 3 до 5 объемом от 3501 утилизационный сбор составляет 1 623 800₽
     *
     * @param carCategory категория авто
     * @return стоимость утилизационного сбора
     */
    private int calculateRecyclingFeeInRubles(int carCategory, int volume) {
        if (volume <= 3000) {
            return carCategory > 1 ? OLD_CAR_RECYCLING_FEE : NEW_CAR_RECYCLING_FEE;
        } else if (volume <= 3500) {
            return carCategory > 1 ? NORMAL_MID_CAR_RECYCLING_FEE : NEW_MID_CAR_RECYCLING_FEE;
        } else {
            return carCategory > 1 ? NORMAL_BIG_CAR_RECYCLING_FEE : NEW_BIG_CAR_RECYCLING_FEE;
        }
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
     * Вычисляем категорию по дате автомобиля.
     * 1- до трех лет
     * 2- от трех до пяти лет
     * 3- свыше пяти лет
     *
     * @return возрастная категория
     */
    private String calculateCarAgeByLocalDate(LocalDate localDate) {
        Period period = Period.between(localDate, LocalDate.now());
        int carYearsOld = period.getYears();
        int carMonthOld = period.getMonths();
        if (carYearsOld >= 5) {
            return OLD_CAR;
        } else if (carYearsOld > 2 || (carYearsOld == 2 && carMonthOld == 11)) {
            return NORMAL_CAR;
        } else {
            return NEW_CAR;
        }
    }

    /**
     * Вычисляем категорию по месяцу и году.
     * 1- до трех лет
     * 2- от трех до пяти лет (не включительно)
     * 3- начиная с 5 лет
     *
     * @return возрастная категория
     */
    public String calculateCarAgeByRawDate(int year, int month) {
        LocalDate oldDate = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth());
        return calculateCarAgeByLocalDate(oldDate);
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
     * Проверка, включен ли МОД для определённой валюты.
     * когда добавится расчёт по ссылке для других валют, пополним.
     *
     * @param currency
     * @return
     */
    public boolean isLinkModeEnabled(String currency) {
        switch (currency) {
            case KRW:
                return configDataPool.isEnableKrwLinkMode();
            case CNY:
                return configDataPool.isEnableCnyLinkMode();
            default:
                return false;
        }
    }

    /**
     * Проверка, включен ли МОД для расчёта ставки на аукционе.
     *
     * @param currency
     * @return
     */
    public boolean isAuctionModeEnabled(String currency) {
        switch (currency) {
            case KRW:
                return configDataPool.isEnableKrwAuctionMode();
            default:
                return false;
        }
    }
}
