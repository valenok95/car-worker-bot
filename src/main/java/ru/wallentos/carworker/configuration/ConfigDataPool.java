package ru.wallentos.carworker.configuration;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ConfigDataPool {
    @Value(value = "${ru.wallentos.carworker.currencies}")
    private List<String> currencies;
    public static final String EUR = "EUR";
    public static final String RUB = "RUB";
    public static final String USD = "USD";
    public static final String KRW = "KRW";
    public static final String CNY = "CNY";
    public static final String LINK_BUTTON = "Расчёт по ссылке";
    public static final String AUCTION_BUTTON = "Расчёт ставки на аукционе";
    public static final String MANUAL_BUTTON = "Расчёт вручную";
    @Value("${ru.wallentos.carworker.exchange-coefficient}")
    public double coefficient;
    @Value("${ru.wallentos.carworker.auction-coefficient:0}")
    public double auctionCoefficient;
    @Value("${ru.wallentos.carworker.extra-pay-china.cny}")
    public int EXTRA_PAY_AMOUNT_CHINA_CNY;
    @Value("${ru.wallentos.carworker.extra-pay-china.rub}")
    public int EXTRA_PAY_AMOUNT_CHINA_RUB;
    @Value("${ru.wallentos.carworker.enable-krw-link-mode:false}")
    public boolean enableKrwLinkMode;
    @Value("${ru.wallentos.carworker.enable-krw-auction-mode:false}")
    public boolean enableKrwAuctionMode;
    @Value("${ru.wallentos.carworker.disable-double-convertation:false}")
    public boolean disableDoubleConvertation;
    @Value("${ru.wallentos.carworker.extra-pay-corea.krw}")
    public int EXTRA_PAY_AMOUNT_KOREA_KRW;
    @Value("${ru.wallentos.carworker.extra-pay-corea.rub}")
    public int EXTRA_PAY_AMOUNT_KOREA_RUB;
    public static Map<String, Double> manualConversionRatesMapInRubles = new HashMap<>();
    public static final int NEW_MID_CAR_RECYCLING_FEE = 970_000;
    public static final int NEW_BIG_CAR_RECYCLING_FEE = 1_235_200;
    public static final int NORMAL_MID_CAR_RECYCLING_FEE = 1_485_000;
    public static final int NORMAL_BIG_CAR_RECYCLING_FEE = 1_623_800;
    public static final int NEW_CAR_RECYCLING_FEE = 3400;
    public static final int OLD_CAR_RECYCLING_FEE = 5200;
    public static final int CUSTOMS_VALUE_1 = 200_000;
    public static final int FEE_RATE_1 = 775;
    public static final int CUSTOMS_VALUE_2 = 450_000;
    public static final int FEE_RATE_2 = 1550;
    public static final int CUSTOMS_VALUE_3 = 1_200_000;
    public static final int FEE_RATE_3 = 3100;
    public static final int CUSTOMS_VALUE_4 = 2_700_000;
    public static final int FEE_RATE_4 = 8530;
    public static final int CUSTOMS_VALUE_5 = 4_200_000;
    public static final int FEE_RATE_5 = 12000;
    public static final int CUSTOMS_VALUE_6 = 5_500_000;
    public static final int FEE_RATE_6 = 15500;
    public static final int CUSTOMS_VALUE_7 = 7_000_000;
    public static final int FEE_RATE_7 = 20000;
    public static final int CUSTOMS_VALUE_8 = 8_000_000;
    public static final int FEE_RATE_8 = 23000;
    public static final int CUSTOMS_VALUE_9 = 9_000_000;
    public static final int FEE_RATE_9 = 25000;
    public static final int CUSTOMS_VALUE_10 = 10_000_000;
    public static final int FEE_RATE_10 = 27000;
    public static final int LAST_FEE_RATE = 30000;
    public static final int NEW_CAR_PRICE_DUTY_1 = 8500;
    public static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_1 =
            new AbstractMap.SimpleEntry<>(0.54, 2.5);
    public static final int NEW_CAR_PRICE_DUTY_2 = 16700;
    public static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_2 =
            new AbstractMap.SimpleEntry<>(0.48, 3.5);
    public static final int NEW_CAR_PRICE_DUTY_3 = 42300;
    public static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_3 =
            new AbstractMap.SimpleEntry<>(0.48, 5.5);
    public static final int NEW_CAR_PRICE_DUTY_4 = 84500;
    public static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_4 =
            new AbstractMap.SimpleEntry<>(0.48, 7.5);
    public static final int NEW_CAR_PRICE_DUTY_5 = 169000;
    public static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_5 =
            new AbstractMap.SimpleEntry<>(0.48, 15d);
    public static final Map.Entry<Double, Double> NEW_CAR_PRICE_MAX_FLAT_RATE =
            new AbstractMap.SimpleEntry<>(0.48, 20d);
    public static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_1 = 1000;
    public static final double NORMAL_CAR_PRICE_FLAT_RATE_1 = 1.5;
    public static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_2 = 1500;
    public static final double NORMAL_CAR_PRICE_FLAT_RATE_2 = 1.7;
    public static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_3 = 1800;
    public static final double NORMAL_CAR_PRICE_FLAT_RATE_3 = 2.5;
    public static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_4 = 2300;
    public static final double NORMAL_CAR_PRICE_FLAT_RATE_4 = 2.7;
    public static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_5 = 3000;
    public static final double NORMAL_CAR_PRICE_FLAT_RATE_5 = 3;
    public static final double NORMAL_CAR_PRICE_FLAT_RATE_MAX = 3.6;
    public static final int OLD_CAR_ENGINE_VOLUME_DUTY_1 = 1000;
    public static final double OLD_CAR_PRICE_FLAT_RATE_1 = 3;
    public static final int OLD_CAR_ENGINE_VOLUME_DUTY_2 = 1500;
    public static final double OLD_CAR_PRICE_FLAT_RATE_2 = 3.2;
    public static final int OLD_CAR_ENGINE_VOLUME_DUTY_3 = 1800;
    public static final double OLD_CAR_PRICE_FLAT_RATE_3 = 3.5;
    public static final int OLD_CAR_ENGINE_VOLUME_DUTY_4 = 2300;
    public static final double OLD_CAR_PRICE_FLAT_RATE_4 = 4.8;
    public static final int OLD_CAR_ENGINE_VOLUME_DUTY_5 = 3000;
    public static final double OLD_CAR_PRICE_FLAT_RATE_5 = 5;
    public static final double OLD_CAR_PRICE_FLAT_RATE_MAX = 5.7;

    public static final String NEW_CAR = "До 3 лет";
    public static final String NORMAL_CAR = "От 3 до 5 лет";
    public static final String OLD_CAR = "От 5 лет";
    public static final String CANCEL_MESSAGE = "Отмена";
    public static final String RESET_MESSAGE = "Рассчитать ещё один автомобиль";
    public static final String TO_START_MESSAGE = "Рассчитать автомобиль";
    public static final String TO_SET_CURRENCY_MENU = "Меню установки валюты";
    public static final String MANAGER_MESSAGE = "Связаться с менеджером";

    /**
     * Карта рассчёта таможенной стоимости.
     */
    public static final Map<Integer, Integer> feeRateMap = new LinkedHashMap<>() {
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
    public static final Map<Integer, Map.Entry<Double, Double>> newCarCustomsMap = new LinkedHashMap<>() {
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
    public static final Map<Integer, Double> normalCarCustomsMap =
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
    public static final Map<Integer, Double> oldCarCustomsMap =
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
     * одновалютный режим
     */
    public boolean isSingleCurrencyMode() {
        return currencies.size() == 1;
    }

    /**
     * валюта одновалютного режима.
     */
    public String singleCurrency() {
        return currencies.get(0);
    }


}
