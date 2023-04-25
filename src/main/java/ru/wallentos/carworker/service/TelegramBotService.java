package ru.wallentos.carworker.service;

import static ru.wallentos.carworker.service.ExecutionService.NEW_CAR;
import static ru.wallentos.carworker.service.ExecutionService.NORMAL_CAR;
import static ru.wallentos.carworker.service.ExecutionService.OLD_CAR;
import static ru.wallentos.carworker.service.ExecutionService.RESET_MESSAGE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.wallentos.carworker.cache.UserDataCache;
import ru.wallentos.carworker.configuration.BotConfiguration;
import ru.wallentos.carworker.model.BotState;
import ru.wallentos.carworker.model.CarPriceResultData;
import ru.wallentos.carworker.model.UserCarData;

@Service
@Data
@Slf4j
@RequiredArgsConstructor
public class TelegramBotService extends TelegramLongPollingBot {
    private final RestService restService;
    private final BotConfiguration config;
    private final UtilService service;
    private final ExecutionService executionService;
    private final UserDataCache cache;
    private static final String USD = "USD";
    private static final String CNY = "CNY";
    private static final String KRW = "KRW";

    @Override
    public String getBotUsername() {
        log.info("setting botName: " + config.getName());
        return config.getName();
    }

    @Override
    public String getBotToken() {
        log.info("setting botKey: " + config.getKey());
        return config.getKey();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String receivedText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            int messageId = update.getMessage().getMessageId();
            log.info("message received: " + receivedText);
            switch (receivedText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                default:
                    handleMessage(receivedText, chatId, messageId);
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if(callbackData.equals(RESET_MESSAGE)){
                startCommandReceived(chatId, update.getCallbackQuery().getMessage().getChat().getFirstName());
                return;
            }
            handleMessage(callbackData, chatId, messageId);
        }
    }


    private void handleMessage(String receivedText, long chatId, int messageId) {
        BotState currentState = cache.getUsersCurrentBotState(chatId);
        try {
            switch (currentState) {
                case ASK_CONCURRENCY:
                    processConcurrency(chatId, messageId, receivedText);
                    break;
                case ASK_PRICE:
                    processPrice(chatId, receivedText);
                    break;
                case ASK_ISSUE_DATE:
                    processIssueDate(chatId, receivedText);
                    break;
                case ASK_VOLUME:
                    processVolume(chatId, receivedText);
                    break;
                default:
                    break;
            }
        } catch (IllegalArgumentException e) {
            executeMessage(service.prepareSendMessage(chatId, "Некорректный формат данных, попробуйте ещё раз."));
            return;
        }
    }

    private void processPrice(long chatId, String receivedText) {
        UserCarData data = cache.getUserCarData(chatId);
        int priceInConcurrency = Integer.parseInt(receivedText);
        data.setPrice(priceInConcurrency);
        data.setPriceInEuro(executionService.convertMoneyToEuro(priceInConcurrency, data.getConcurrency()));
        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.ASK_ISSUE_DATE);
        String text = "Выберите возраст автомобиля:";
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton newCar = new InlineKeyboardButton(NEW_CAR);
        InlineKeyboardButton normalCar = new InlineKeyboardButton(NORMAL_CAR);
        InlineKeyboardButton oldCar = new InlineKeyboardButton(OLD_CAR);
        newCar.setCallbackData(NEW_CAR);
        normalCar.setCallbackData(NORMAL_CAR);
        oldCar.setCallbackData(OLD_CAR);
        row.add(newCar);
        row.add(normalCar);
        row.add(oldCar);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(service.prepareSendMessage(chatId, text, inlineKeyboardMarkup));

        //executeMessage(service.prepareSendMessage(chatId, text));
    }

    private void processIssueDate(long chatId, String receivedText) {
        UserCarData data = cache.getUserCarData(chatId);
        data.setAge(receivedText);
        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.ASK_VOLUME);
        String text = "Введите объем двигателя в кубических сантиметрах.\nПример: 1998";
        executeMessage(service.prepareSendMessage(chatId, text));
    }

    private void processVolume(long chatId, String receivedText) {
        UserCarData data = cache.getUserCarData(chatId);
        data.setVolume(Integer.parseInt(receivedText));
        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.DATA_PREPARED);
        String text = String.format("Данные переданы в обработку: %n %s" +
                " %n Рассчитываем стоимость...", data);
        executeMessage(service.prepareSendMessage(chatId, text));
        processExecuteResult(data, chatId);
    }

    private void processExecuteResult(UserCarData data, long chatId) {
        CarPriceResultData resultData = executionService.executeCarPriceResultData(data);
        String text = String.format("Результаты рассчёта: %n %s", resultData);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton reset = new InlineKeyboardButton(RESET_MESSAGE);
        reset.setCallbackData(RESET_MESSAGE);
        row.add(reset);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(service.prepareSendMessage(chatId, text, inlineKeyboardMarkup));
    }


    private void startCommandReceived(long chatId, String name) {
        //restService.refreshExchangeRates();
        //Double rate = restService.getConversionRatesMap().get("USD");
        //String text = String.format("один USD равен %f EUR", rate);
        restService.refreshExchangeRates();
        String message = "Здравствуйте, " + name + "!\n" +
                "Я бот для расчета конечной стоимости автомобиля. Выберите валюту покупки.";
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton usdButton = new InlineKeyboardButton(USD);
        InlineKeyboardButton cnyButton = new InlineKeyboardButton(CNY);
        InlineKeyboardButton krwButton = new InlineKeyboardButton(KRW);
        usdButton.setCallbackData(USD);
        cnyButton.setCallbackData(CNY);
        krwButton.setCallbackData(KRW);
        row.add(usdButton);
        row.add(cnyButton);
        row.add(krwButton);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(service.prepareSendMessage(chatId, message, inlineKeyboardMarkup));
        cache.setUsersCurrentBotState(chatId, BotState.ASK_CONCURRENCY);
    }

    private void processConcurrency(long chatId, int messageId, String concurrency) {
        //restService.refreshExchangeRates();
        //Double rate = restService.getConversionRatesMap().get("USD");
        //String text = String.format("один USD равен %f EUR", rate);
        UserCarData data = cache.getUserCarData(chatId);
        data.setConcurrency(concurrency);
        data.setStock(executionService.executeStock(concurrency));
        cache.saveUserCarData(chatId, data);
        String text =
                String.format("Вы выбрали тип валюты: %s\n Теперь введите стоимость автомобиля в " +
                                "валюте."
                        , concurrency);
        executeEditMessageText(text, chatId, messageId);
        cache.setUsersCurrentBotState(chatId, BotState.ASK_PRICE);
    }


    private void unrecognizedCommandReceived(long chatId) {
        executeMessage(service.prepareSendMessage(chatId, "Команда не распознана, чтобы начать - нажмите /start"));
    }

    private void executeEditMessageText(String text, long chatId, int messageId) {

        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(text);
        message.setMessageId(messageId);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
