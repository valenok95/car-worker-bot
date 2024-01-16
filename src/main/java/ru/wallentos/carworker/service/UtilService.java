package ru.wallentos.carworker.service;

import static ru.wallentos.carworker.configuration.ConfigDataPool.CNY;
import static ru.wallentos.carworker.configuration.ConfigDataPool.EUR;
import static ru.wallentos.carworker.configuration.ConfigDataPool.KRW;
import static ru.wallentos.carworker.configuration.ConfigDataPool.RUB;
import static ru.wallentos.carworker.configuration.ConfigDataPool.USD;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.wallentos.carworker.exceptions.GetCarDetailException;

@Service
@Slf4j
public class UtilService {
    private static final String VALUE = "Value";
    @Autowired
    private ObjectMapper mapper;

    /**
     * Преобразовываем из JSON ЦБ курса в формат валют относительно EUR (старый формат)
     */
    protected Map<String, Double> backRatesToConversionRatesMap(String jsonString) {
        double rubRateTmp;
        double usdRate;
        double krwRate;
        double cnyRate;
        try {
            var valutes = mapper.readTree(jsonString).get("Valute");

            rubRateTmp = valutes.get(EUR).get(VALUE).asDouble();
            usdRate = rubRateTmp / valutes.get(USD).get(VALUE).asDouble();
            cnyRate = rubRateTmp / valutes.get(CNY).get(VALUE).asDouble();
            krwRate = rubRateTmp / valutes.get(KRW).get(VALUE).asDouble() * 1000;

        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Map<String, Double> conversionRatesMap = new HashMap<>();
        conversionRatesMap.put(RUB, rubRateTmp);
        conversionRatesMap.put(USD, usdRate);
        conversionRatesMap.put(CNY, cnyRate);
        conversionRatesMap.put(KRW, krwRate);
        return conversionRatesMap;
    }

    /**
     * Вытащить из ссылки carId.
     */
    public String parseLinkToCarId(String link) throws GetCarDetailException {
        try {
            if (link.contains("fem.encar.com")) {
                return parseFemEncarLinkToCarId(link);
            } else if (link.contains("encar.com")) {
                return parseEncarLinkToCarId(link);
            } else if (link.contains("pcm.che168.com")) {
                return parseMobileCheCarLinkToCarId(link);
            } else if (link.contains("che168.com")) {
                return parseCheCarLinkToCarId(link);
            } else {
                throw new RuntimeException("неопознанная ссылка " + link);
            }
        } catch (Exception e) {
            String errorMessage = "Ошибка при обработке ссылки";
            log.error(errorMessage);
            throw new GetCarDetailException(errorMessage);
        }
    }

    /**
     * Определить валюту по ссылке.
     *
     * @param link
     * @return
     */
    public String defineCurrencyByLink(String link) throws GetCarDetailException {
        if (link.contains("encar")) {
            return KRW;
        } else if (link.contains("che168")) {
            return CNY;
        } else {
            String errorMessage = "Ошибка при обработке ссылки";
            log.error(errorMessage);
            throw new GetCarDetailException(errorMessage);
        }
    }

    /**
     * Вытащить из ссылки fem.encar.com.
     */
    public String parseFemEncarLinkToCarId(String link) {
        Pattern pattern = Pattern.compile("detail\\/(\\d{8})");
        Matcher matcher = pattern.matcher(link);
        matcher.find();
        return matcher.group(1);
    }

    /**
     * Вытащить из ссылки encar.com.
     */
    public String parseEncarLinkToCarId(String link) {
        Pattern pattern = Pattern.compile("carid=(\\d{8})");
        Matcher matcher = pattern.matcher(link);
        matcher.find();
        return matcher.group(1);
    }

    /**
     * Вытащить id из ссылки che168.com.
     */
    public String parseCheCarLinkToCarId(String link) {
        Pattern pattern = Pattern.compile("\\/(\\d+)\\.html");
        Matcher matcher = pattern.matcher(link);
        matcher.find();
        return matcher.group(1);
    }

    /**
     * Вытащить id из ссылки pem.che168.com.
     */
    public String parseMobileCheCarLinkToCarId(String link) {
        Pattern pattern = Pattern.compile("infoid=(\\d{8})");
        Matcher matcher = pattern.matcher(link);
        matcher.find();
        return matcher.group(1);
    }

    /**
     * Вытащить siteKey со строки каптчи.
     */
    public String parseCaptchaKey(String string) {
        Pattern pattern = Pattern.compile("execute\\(\\'(.+?(?=\\'))");
        Matcher matcher = pattern.matcher(string);
        matcher.find();
        return matcher.group(1);
    }

    /**
     * Вытащить action со строки каптчи.
     */
    public String parseCaptchaAction(String string) {
        Pattern pattern = Pattern.compile("action: \\'(.+?(?=\\'))");
        Matcher matcher = pattern.matcher(string);
        matcher.find();
        return matcher.group(1);
    }

    /**
     * Вытащить объём двигателя из строки che168.
     */
    public String parseCheCarPower(String string) {
        Pattern pattern = Pattern.compile("排量\\(mL\\)\\\", \"id\": \\d+, \"value\\\": \\\"(\\d+)");
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            return matcher.group(1);
        } else return "0";
    }


    /**
     * Получаем ссылку на автомобиль
     */
    public String getEncarLinkStringByCarId(int carId) {
        return carId != 0 ? String.format("""
                                
                <a href="https://fem.encar.com/cars/detail/%d">🔗Ссылка на автомобиль</a>
                """, carId) : "";
    }

    /**
     * Получаем ссылку на автомобиль
     *
     * @param carId
     * @return
     */
    public String getEncarInspectLinkStringByCarId(int carId) {
        return carId != 0 ? String.format("""
                                
                <a href="https://fem.encar.com/cars/report/inspect/%d">🔗Ссылка на схему повреждений кузовных элементов🔗</a>
                """, carId) : "";
    }

    public String getProvinceStringByProvinceNameAndPrice(String provinceName,
                                                          double provincePriceInRub) {
        return Objects.nonNull(provinceName) ? String.format("""
                                
                Стоимость доставки автомобиля из %s до Суйфыньхэ: %,.0f ₽
                """, provinceName, provincePriceInRub) : """
                                
                Актуальную стоимость доставки до Суйфыньхэ интересующего вас авто вы можете уточнить у консультанта.
                """;
    }

    public String getCheCarLinkStringByCarId(int carId) {
        return carId != 0 ? String.format("""
                                
                <a href="https://www.che168.com/dealer/416034/%d.html">🔗Ссылка на автомобиль</a>
                """, carId) : "";
    }
}
