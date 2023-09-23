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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.wallentos.carworker.exceptions.GetCarDetailException;
import ru.wallentos.carworker.model.CarPriceResultData;

@Service
@Slf4j
public class UtilService {
    private static final String VALUE = "Value";
    @Autowired
    private ObjectMapper mapper;

    protected SendMessage prepareSendMessage(long chatId, String text, InlineKeyboardMarkup
            inlineKeyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setParseMode(ParseMode.HTML);
        message.setReplyMarkup(inlineKeyboardMarkup);
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    protected SendMessage prepareSendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    protected SendPhoto prepareSendMessage(long chatId, String file, String text) {
        SendPhoto message = SendPhoto.builder()
                .chatId(chatId)
                .caption(text)
                .photo(new InputFile(file))
                .build();
        return message;
    }

    protected SendPhoto prepareSendMessage(long chatId, String file, String text, InlineKeyboardMarkup
            inlineKeyboardMarkup) {
        SendPhoto message = SendPhoto.builder()
                .chatId(chatId)
                .caption(text)
                .photo(new InputFile(file))
                .replyMarkup(inlineKeyboardMarkup)
                .build();
        return message;
    }

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
     * Вытащить из ссылки fem.encar.com.
     */
    private String parseFemEncarLinkToCarId(String link) {
        Pattern pattern = Pattern.compile("detail\\/(\\d{8})");
        Matcher matcher = pattern.matcher(link);
        matcher.find();
        return matcher.group(1);
    }

    /**
     * Вытащить из ссылки encar.com.
     */
    private String parseEncarLinkToCarId(String link) {
        Pattern pattern = Pattern.compile("carid=(\\d{8})");
        Matcher matcher = pattern.matcher(link);
        matcher.find();
        return matcher.group(1);
    }

    /**
     * Вытащить id из ссылки che168.com.
     */
    private String parseCheCarLinkToCarId(String link) {
        Pattern pattern = Pattern.compile("\\/(\\d+)\\.html");
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
        matcher.find();
        return matcher.group(1);
    }


    protected String getResultMessageByBotNameAndCurrency(String botName, String currency, CarPriceResultData resultData) {
        if (botName.equals("KorexCalcBot")) {
            if (Objects.equals(currency, CNY)) {
                return getKorexCnyMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getKorexKrwMessageByResultData(resultData);
            }
        } else if (botName.equals("EastWayCalcBot") || botName.equals("carworkerbot")) { // 
            // определять сообщение для каждого botName + default
            if (Objects.equals(currency, CNY)) {
                return getEastWayCnyMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getEastWayKrwMessageByResultData(resultData);
            }
        } else if (botName.equals("AutoDillerBot")) {
            if (Objects.equals(currency, CNY)) {
                return getRostovCnyMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getRostovKrwMessageByResultData(resultData);
            }
        } else if (botName.equals("KorexManagerBot")) {
            if (Objects.equals(currency, CNY)) {
                return getKorexManagerCnyMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getKorexManagerKrwMessageByResultData(resultData);
            }
        }
        return String.format("""
                %s
                        
                Что бы заказать авто - вы можете обратиться к менеджеру🔻
                        """, resultData);
    }

    protected String getAuctionKrwResultMessage(double resultKrwPrice) {
        return String.format(Locale.FRANCE, """
                Ваша ставка на аукционе %,.0f KRW
                """, resultKrwPrice);
    }

    private String getKorexKrwMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        %,.0f ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽ 
                        %s               
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @KorexAdmin.
                                                
                        <a href="https://t.me/korexautotradeofficial">🔗Официальный телеграмм канал</a>
                        <a href="https://t.me/korexautotradeofficial/705">🔗Видео инструкция по сайту Encar</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountInCurrency(),
                resultData.getExtraPayAmountInRubles(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                getEncarLinkStringByCarId(resultData.getCarId()));
    }

    private String getKorexCnyMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                        %s             
                        Брокерские расходы, СВХ, СБКТС:
                        %,.0f ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽ 
                        %s               
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Китае, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @KorexAdmin.
                                                
                        <a href="https://t.me/korexautotradeofficial">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountInCurrency(),
                getProvinceStringByProvinceNameAndPrice(resultData.getProvinceName(),
                        resultData.getProvincePriceInRubles()),
                resultData.getExtraPayAmountInRubles(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                getCheCarLinkStringByCarId(resultData.getCarId()));
    }

    private String getEastWayCnyMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                        %s             
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽               
                                                
                        Комиссия компании: 50 000 ₽
                        %s 
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Китае, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @EastWayAdmin.
                                                
                        <a href="https://t.me/EastWayOfficial">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountInCurrency(),
                getProvinceStringByProvinceNameAndPrice(resultData.getProvinceName(),
                        resultData.getProvincePriceInRubles()),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                getCheCarLinkStringByCarId(resultData.getCarId()));
    }

    private String getEastWayKrwMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽ 
                                                
                        Комиссия компании: 50 000 ₽
                        %s               
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @EastWayAdmin.
                                                
                        <a href="https://t.me/EastWayOfficial">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountInCurrency(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                getEncarLinkStringByCarId(resultData.getCarId()));
    }


    private String getRostovCnyMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Новочеркасске:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Уссурийска:
                        %,.0f ₽
                        %s             
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽     
                                                
                        Доставка до Новочеркасска: %,.0f ₽             
                                                
                        Комиссия компании: 50 000 ₽
                        %s 
                        Итоговая стоимость включает в себя все расходы до г. Новочеркасск, а именно: оформление экспорта в Китае, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @Roman_autodiler.
                                                
                        <a href="https://t.me/autodiler61">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountInCurrency(),
                getProvinceStringByProvinceNameAndPrice(resultData.getProvinceName(),
                        resultData.getProvincePriceInRubles()),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                resultData.getExtraPayAmountInRubles(),
                getCheCarLinkStringByCarId(resultData.getCarId()));
    }

    private String getRostovKrwMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Новочеркасске:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽ 
                                                
                        Доставка до Новочеркасска: %,.0f ₽     
                                                
                        Комиссия компании: 50 000 ₽
                        %s               
                        Итоговая стоимость включает в себя все расходы до г. Новочеркасск, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @Roman_autodiler.
                                                
                        <a href="https://t.me/autodiler61">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountInCurrency(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                resultData.getExtraPayAmountInRubles(),
                getEncarLinkStringByCarId(resultData.getCarId()));
    }


    private String getKorexManagerCnyMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                        %s
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Китае, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountInCurrency(),
                getProvinceStringByProvinceNameAndPrice(resultData.getProvinceName(),
                        resultData.getProvincePriceInRubles()),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                getCheCarLinkStringByCarId(resultData.getCarId()));
    }

    private String getKorexManagerKrwMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Новочеркасске:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Новочеркасск, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountInCurrency(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                getEncarLinkStringByCarId(resultData.getCarId()));
    }

    /**
     * Получаем ссылку на автомобиль
     *
     * @param carId
     * @return
     */
    private String getEncarLinkStringByCarId(int carId) {
        return carId != 0 ? String.format("""
                                
                <a href="https://fem.encar.com/cars/detail/%d">🔗Ссылка на автомобиль</a>                
                """, carId) : "";
    }

    private String getProvinceStringByProvinceNameAndPrice(String provinceName,
                                                           double provincePriceInRub) {
        return Objects.nonNull(provinceName) ? String.format("""
                                
                Стоимость доставки автомобиля из %s до Суйфыньхэ: %,.0f ₽                
                """, provinceName, provincePriceInRub) : "";
    }

    private String getCheCarLinkStringByCarId(int carId) {
        return carId != 0 ? String.format("""
                                
                <a href="https://www.che168.com/dealer/416034/%d.html">🔗Ссылка на автомобиль</a>                
                """, carId) : "";
    }
}
