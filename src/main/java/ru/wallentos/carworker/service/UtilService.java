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
            } else {
                throw new RuntimeException("неопознанная ссылка " + link);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Ошибка при обработке ссылки, carId должен " +
                    "состоять состоит из 8 цифр");
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


    protected String getResultMessageByBotNameAndCurrency(String botName, String currency, CarPriceResultData resultData) {

        if (botName.equals("KorexCalcBot")) { // определять сообщение для каждого botName + 
            // default
            return getKorexMessageByResultData(resultData);
        }
        if (botName.equals("EastWayCalcBot")) { // определять 
            // сообщение для каждого botName + default
            return getEastWayKrwMessageByResultData(resultData);
        } else {
            return String.format("""
                    %s
                            
                    Что бы заказать авто - пиши менеджеру🔻
                            """, resultData);
        }
    }

    protected String getAuctionKrwResultMessage(double resultKrwPrice) {
        return String.format(Locale.FRANCE, """
                Ваша ставка на аукционе %,.0f KRW
                """, resultKrwPrice);
    }

    private String getKorexMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        %,.0f ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽ 
                        %s               
                        ‼️Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину‼️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто обратитесь к нашему менеджеру @KorexAdmin.
                                                
                        <a href="https://t.me/korexautotradeofficial">🔗Официальный телеграмм канал</a>
                        <a href="https://t.me/korexautotradeofficial/705">🔗Видео инструкция по сайту Encar</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountInCurrency(),
                resultData.getExtraPayAmountInRubles(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                getEncarLinkStringByCarId(resultData.getCarId()));
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
                        ‼️Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину‼️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто обратитесь к нашему менеджеру @EastWayAdmin.
                                                
                        <a href="https://t.me/EastWayOfficial">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountInCurrency(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                getEncarLinkStringByCarId(resultData.getCarId()));
    }


    private String getEncarLinkStringByCarId(int carId) {
        return carId != 0 ? String.format("""
                                
                <a href="https://fem.encar.com/cars/detail/%d">🔗Ссылка на автомобиль</a>                
                """, carId) : "";
    }
}
