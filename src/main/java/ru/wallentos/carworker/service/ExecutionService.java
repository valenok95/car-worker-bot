package ru.wallentos.carworker.service;

import static ru.wallentos.carworker.configuration.ConfigDataPool.CNY;
import static ru.wallentos.carworker.configuration.ConfigDataPool.FEE_RATE_MAP;
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
import static ru.wallentos.carworker.configuration.ConfigDataPool.conversionRatesMap;
import static ru.wallentos.carworker.configuration.ConfigDataPool.manualConversionRatesMapInRubles;
import static ru.wallentos.carworker.configuration.ConfigDataPool.newCarCustomsMap;
import static ru.wallentos.carworker.configuration.ConfigDataPool.normalCarCustomsMap;
import static ru.wallentos.carworker.configuration.ConfigDataPool.oldCarCustomsMap;

import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.wallentos.carworker.configuration.ConfigDataPool;
import ru.wallentos.carworker.model.CarPriceResultData;
import ru.wallentos.carworker.model.CarTotalResultData;
import ru.wallentos.carworker.model.Province;
import ru.wallentos.carworker.model.UserCarInputData;

@Service
@Slf4j
public class ExecutionService {
    private RestService restService;
    private ConfigDataPool configDataPool;

    @Autowired
    public ExecutionService(RestService restService, ConfigDataPool configDataPool) {
        this.restService = restService;
        this.configDataPool = configDataPool;
        restService.refreshExchangeRates();
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
        for (Map.Entry<Integer, Integer> pair : FEE_RATE_MAP.entrySet()) {
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
        if (KRW.equals(userCarInputData.getCurrency()) && configDataPool.isEnableDoubleConvertation()) {
            userCarInputData.setSanctionCar(isSanctionCar(userCarInputData));
            resultData.setSanctionCar(userCarInputData.isSanctionCar());
        }
        resultData.setAge(userCarInputData.getAge());
        resultData.setFeeRate(getFeeRateFromCarPriceInRubles(userCarInputData.getPriceInEuro()));
        resultData.setDuty(calculateDutyInRubles(userCarInputData.getPriceInEuro(), getCarCategory(userCarInputData.getAge()), userCarInputData.getVolume()));
        resultData.setRecyclingFee(calculateRecyclingFeeInRubles(getCarCategory(userCarInputData.getAge()), userCarInputData.getVolume()));
        resultData.setFirstPriceInRubles(calculateFirstCarPriceInRublesByUserCarData(userCarInputData));
//теперь считаем валютную надбавку в зависимости от настройки (динамичная либо конфиг)
        userCarInputData.setInputExtraPayAmountKoreaKrw(getValutePartInKrw(resultData.getFirstPriceInRubles()));
//валютная надбавка  и рублёвая надбавка (Брокерские расходы, СВХ, СБКТС)
        double extraPayAmountRublePart = executeRubExtraPayAmountInRublesByUserCarData(userCarInputData.getCurrency());
        double extraPayAmountCurrencyPart =
                executeValuteExtraPayAmountInRublesByUserCarData(userCarInputData); // в рублях
        resultData.setExtraPayAmountRublePart(extraPayAmountRublePart);
        resultData.setExtraPayAmountValutePart(extraPayAmountCurrencyPart); // валютная надбавка
        // Стоимость логистики из провинции Китая
        if (Objects.nonNull(userCarInputData.getProvince())) {
            resultData.setProvincePriceInRubles(executeProvincePriceInRublesToSuyfynkhe(userCarInputData.getCurrency(), userCarInputData.getProvince()));
            resultData.setProvinceName(userCarInputData.getProvince().getProvinceFullName());
        }
        resultData.setStock(executeStock(userCarInputData.getCurrency()));
        resultData.setLocation(executeLocation(userCarInputData.getCurrency()));
        return resultData;
    }

    /**
     * Гибкий расчет валютной надбавки KRW в зависимости от конфигов.
     *
     * @param priceInRubles - первичная цена в рублях
     * @return
     */
    private int getValutePartInKrw(double priceInRubles) {
        if (configDataPool.isEnableDymanicValutePart()) {
            double manualUsdRate = manualConversionRatesMapInRubles.get(USD);
            double priceInUsd = priceInRubles / manualUsdRate;
            int extraPayAmountKoreaKrwResult =
                    getDynamicValutePartByPriceInUsd(priceInUsd);
            log.info("""
                    Устанавливаем динамическую валютную надбавку для KRW:
                    Цена в $ - это Цена в рублях {} поделить на ручной курс USD {} = {}$
                    Соответствующая валютная надбавка - {} KRW.
                    """, priceInRubles, manualUsdRate, priceInUsd, extraPayAmountKoreaKrwResult);
            return extraPayAmountKoreaKrwResult;
        } else {
            return configDataPool.EXTRA_PAY_AMOUNT_KOREA_KRW;
        }
    }

    /**
     * Определить динамическую валютную надбавку исходя из стоимости авто в $.
     *
     * @param priceInUsd
     * @return
     */
    public int getDynamicValutePartByPriceInUsd(double priceInUsd) {
        int dynamicValutePartResult = 0;
        for (Map.Entry<Integer, Integer> pair : configDataPool.getDynamicKrwValutePartMap().entrySet()) {
            if (priceInUsd < pair.getKey()) {
                dynamicValutePartResult = pair.getValue();
                break;
            }
        }
        return dynamicValutePartResult;
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
        resultData.setProvince(userCarInputData.getProvince());
        // Стоимость логистики из провинции Китая
        System.out.println("курс расчёта USDCNY " + restService.getManagerCnyUsdRate());
        return resultData;
    }

    /**
     * Расчитываем логистику для доставки по провинциям Китая до Суйфынхэ.
     *
     * @param currency
     * @param province
     * @return
     */
    private double executeProvincePriceInRublesToSuyfynkhe(String currency, Province province) {
        return manualConversionRatesMapInRubles.get(currency) * province.getProvincePriceInCurrencyToSuyfynkhe();
    }


    /**
     * Расчёт ставки аукциона исходя из данных пользователя.
     *
     * @param userCarInputData
     * @return
     */
    public int executeAuctionResultInKrw(UserCarInputData userCarInputData) {
        return 0;
    }

    /**
     * Округлить число
     *
     * @param resultAuctionPriceInKrw
     */
    private int roundDoubleToInt(double resultAuctionPriceInKrw, int round) {
        return ((int) resultAuctionPriceInKrw / round) * round;
    }

    /**
     * Вычислить первичную стоимость автомобиля в рублях.
     *
     * @param userCarInputData
     * @return
     */
    private double calculateFirstCarPriceInRublesByUserCarData(UserCarInputData userCarInputData) {
        String currentCurrency = userCarInputData.getCurrency();
        int price = userCarInputData.getPrice();
        double result;
        log.info("Вычисляем первичную стоимость автомобиля для валюты {}:", currentCurrency);
        // добавить двойную конвертацию
        if (currentCurrency.equals(KRW) && configDataPool.enableDoubleConvertation) {
            result = (price / restService.getCbrUsdKrwMinusCorrection()) * ConfigDataPool.manualConversionRatesMapInRubles.get(USD);
            log.info("""
                            Режим двойной конвертации:
                            Стоимость автомобиля {} {} поделённая на курс USD/KRW-коррекция {} и умноженная на ручной курс USD/RUB {} = {} RUB""", price
                    , currentCurrency,
                    restService.getCbrUsdKrwMinusCorrection(), ConfigDataPool.manualConversionRatesMapInRubles.get(USD), result);
        } else {
            double manualConversionRate =
                    ConfigDataPool.manualConversionRatesMapInRubles.get(currentCurrency);
            result = price * manualConversionRate;
            log.info("Режим стандартной конвертации:" +
                            "Стоимость автомобиля {} {} * {} = {} RUB", price, currentCurrency,
                    manualConversionRate, result);
        }
        return result;
    }

    /**
     * Определение санкционности авто: либо она дороже 50К$, либо она объемнее 2К.
     *
     * @param userCarInputData исходные данные о тачке.
     * @return
     */
    private boolean isSanctionCar(UserCarInputData userCarInputData) {
        int priceInUsd = (int) (userCarInputData.getPrice() / restService.getCbrUsdKrwMinusCorrection());
        boolean isSanctionCarResult =
                priceInUsd > 50_000 || userCarInputData.getVolume() >= configDataPool.getSanctionCarVolumeLimit();
        log.info("""
                Определяем санкционность авто.
                Стоимость: {} $
                Объём двигателя: {} cc.
                Результат:  {}
                """, priceInUsd, userCarInputData.getVolume(), isSanctionCarResult ? "Авто - санкционный!" :
                "Авто - НЕ санкционный!");
        return isSanctionCarResult;
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
     * Рассчитываем доп взносы. Валютная часть. если тачка дороже 50 000 USD - то двойная
     * конвертация. Иначе необходим объём двигателя >2000 чтобы тачка считалась санкционной.
     */
    private double executeValuteExtraPayAmountInRublesByUserCarData(UserCarInputData userCarInputData) {
        String currency = userCarInputData.getCurrency();
        boolean isSanctionCar = userCarInputData.isSanctionCar();
        switch (currency) {
            case KRW:
                int inputExtraPayInKrw = userCarInputData.getInputExtraPayAmountKoreaKrw();
                log.info("Расчёт валютной надбавки:");
                boolean ifWeNeedDoubleConvertation =
                        configDataPool.enableDoubleConvertation;
                return ifWeNeedDoubleConvertation ?
                        getExtraKrwPayAmountDoubleConvertation(inputExtraPayInKrw) :
                        getExtraPayKrwAmountNormalConvertationInRub(inputExtraPayInKrw);
            case CNY:
                return getExtraPayAmountNormalConvertationInRub(CNY);
            default:
                return 0;
        }
    }

    /**
     * Считаем доп взносы переведенные в рубли по нормальной конвертации ДЛЯ КОРЕИ.
     *
     * @param inputExtraPayAmountInKrw - исходная валютная надбавка в KRW
     * @return
     */
    private double getExtraPayKrwAmountNormalConvertationInRub(int inputExtraPayAmountInKrw) {
        double rate = ConfigDataPool.manualConversionRatesMapInRubles.get(KRW);
        double result;
        result = inputExtraPayAmountInKrw * rate;
        log.info("""
                В режиме нормальной конвертации в рублях:
                Надбавка {} KRW * ручной курс {} = {} RUB
                """, inputExtraPayAmountInKrw, rate, result);
        return result;
    }

    /**
     * Считаем доп взносы переведенные в рубли по нормальной конвертации.
     *
     * @param currency
     * @return
     */
    private double getExtraPayAmountNormalConvertationInRub(String currency) {
        double rate = ConfigDataPool.manualConversionRatesMapInRubles.get(currency);
        double result;
        switch (currency) {
            case KRW:
                result = configDataPool.EXTRA_PAY_AMOUNT_KOREA_KRW * rate;
                log.info("""
                        В режиме нормальной конвертации в рублях:
                        Надбавка {} KRW * ручной курс {} = {} RUB
                        """, configDataPool.EXTRA_PAY_AMOUNT_KOREA_KRW, rate, result);
                return result;
            case CNY:
                result = configDataPool.EXTRA_PAY_AMOUNT_CHINA_CNY * rate;
                log.info("""
                        В режиме нормальной конвертации в рублях:
                        Надбавка {} CNY * ручной курс {} = {} RUB
                        """, configDataPool.EXTRA_PAY_AMOUNT_CHINA_CNY, rate, result);
                return result;
            default:
                return 0;
        }
    }

    /**
     * Если эквивалент тачки стоит меньше, чем 50 000$ то (KRW для взносов делим на (курс KRW/USD по
     * ЦБ минус коррекция) и умножаем на ручной курс USD.
     */
    private double getExtraKrwPayAmountDoubleConvertation(int extraPayInKrw) {
        double manualRate = ConfigDataPool.manualConversionRatesMapInRubles.get(USD);
        double usdKrwMinusCorrectionRate = restService.getCbrUsdKrwMinusCorrection();
        double usdAmount = extraPayInKrw / usdKrwMinusCorrectionRate;
        double result = usdAmount * manualRate;
        log.info("""
                        В режиме двойной конвертации.
                        Валютная надбавка в USD: надбавка {} KRW * курс-коррекция {} = {} USD
                        Валютная надбавка в рублях: {} USD * ручной курс {} = {} RUB
                        """, extraPayInKrw, usdKrwMinusCorrectionRate, usdAmount,
                usdAmount, manualRate, result);
        return result;
    }


    private double convertMoneyFromEuro(double count, String toCurrency) {
        return count * conversionRatesMap.get(toCurrency);
    }

    public double convertMoneyToEuro(double count, String fromCurrency) {
        return count / conversionRatesMap.get(fromCurrency);
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
