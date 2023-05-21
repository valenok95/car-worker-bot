package ru.wallentos.carworker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Service
public class UtilService {
    private static final String EUR = "EUR";
    private static final String RUB = "RUB";
    private static final String USD = "USD";
    private static final String KRW = "KRW";
    private static final String CNY = "CNY";
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
}
