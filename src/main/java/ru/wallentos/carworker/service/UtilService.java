package ru.wallentos.carworker.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Service
public class UtilService {

    protected SendMessage prepareSendMessage(long chatId, String text, InlineKeyboardMarkup
            inlineKeyboardMarkup) {
        SendMessage message = new SendMessage();
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

/*    protected LocalDate parseDateFromString(String stringDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        LocalDate resultDate;
        try {
            if (stringDate.length() < 10) {
                int currentDay = LocalDate.now().getDayOfMonth();
                if (currentDay < 10) {
                    stringDate += ".0" + currentDay;
                } else {
                    stringDate += "." + currentDay;
                }
            }
            resultDate = LocalDate.parse(stringDate, formatter);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException(e);
        }
        return resultDate;
    }*/

}
