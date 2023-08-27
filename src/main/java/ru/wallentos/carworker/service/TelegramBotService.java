package ru.wallentos.carworker.service;

import static ru.wallentos.carworker.configuration.ConfigDataPool.AUCTION_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.CANCEL_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.CANCEL_MAILING_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.CNY;
import static ru.wallentos.carworker.configuration.ConfigDataPool.CONFIRM_MAILING_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.KRW;
import static ru.wallentos.carworker.configuration.ConfigDataPool.LINK_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.MANAGER_MESSAGE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.MANUAL_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NEW_CAR;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NORMAL_CAR;
import static ru.wallentos.carworker.configuration.ConfigDataPool.OLD_CAR;
import static ru.wallentos.carworker.configuration.ConfigDataPool.RESET_MESSAGE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.RUB;
import static ru.wallentos.carworker.configuration.ConfigDataPool.TO_SET_CURRENCY_MENU;
import static ru.wallentos.carworker.configuration.ConfigDataPool.TO_START_MESSAGE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.USD;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.wallentos.carworker.cache.UserDataCache;
import ru.wallentos.carworker.configuration.BotConfiguration;
import ru.wallentos.carworker.configuration.ConfigDataPool;
import ru.wallentos.carworker.exceptions.GetCarDetailException;
import ru.wallentos.carworker.exceptions.RecaptchaException;
import ru.wallentos.carworker.model.BotState;
import ru.wallentos.carworker.model.CarPriceResultData;
import ru.wallentos.carworker.model.EncarDto;
import ru.wallentos.carworker.model.UserCarInputData;

@Service
@Data
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {

    @Value("${ru.wallentos.carworker.manager-link}")
    public String managerLink;
    @Autowired
    private RestService restService;
    @Autowired
    private ConfigDataPool configDataPool;
    private final BotConfiguration config;
    @Autowired
    private UtilService utilService;
    @Autowired
    private EncarCacheService encarCacheService;
    @Autowired
    private ExecutionService executionService;
    @Autowired
    private SubscribeService subscribeService;
    @Autowired
    private UserDataCache cache;

    public TelegramBotService(BotConfiguration config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "Старт"));
        listofCommands.add(new BotCommand("/cbr", "курс ЦБ"));
        listofCommands.add(new BotCommand("/currencyrates", "Актуальный курс оплаты"));
        listofCommands.add(new BotCommand("/settingservice", "Сервис"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        log.info("getting botName: " + config.getName());
        return config.getName();
    }

    @Override
    public String getBotToken() {
        log.info("getting botKey: " + config.getKey());
        return config.getKey();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            handleCallbackData(callbackData, update);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            String receivedText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            log.info("message received: " + receivedText);
            switch (receivedText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/cbr":
                    cbrCommandReceived(chatId);
                    break;
                case "/currencyrates":
                    currencyRatesCommandReceived(chatId);
                    break;
                case "/settingservice":
                    setCurrencyCommandReceived(chatId);
                    break;
                case "/mail":
                    mailingMenuCommandReceived(chatId);
                    break;
                case "/sleep":
                    unsubscribeCommandReceived(chatId);
                    break;
                default:
                    handleMessage(update, receivedText);
                    break;
            }
        } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            long chatId = update.getMessage().getChatId();
            BotState state = cache.getUsersCurrentBotState(chatId);
            switch (state) {
                case MAILING_MENU -> processSendPhotoMail(update);
                default -> unrecognizedCommandReceived(update.getMessage().getChatId());
            }
        } else {
            unrecognizedCommandReceived(update.getMessage().getChatId());
        }
    }

    /**
     * Получаем предварительные данные по рассылке - сохраняем их.
     * Отдаём эхо + кнопки.
     *
     * @param update
     */
    private void processSendPhotoMail(Update update) {
        log.info("photo received");
        long chatId = update.getMessage().getChatId();
        String photoData = update.getMessage().getPhoto().get(0).getFileId();
        String caption = update.getMessage().getCaption();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton confirmMailingButton = new InlineKeyboardButton(CONFIRM_MAILING_BUTTON);
        InlineKeyboardButton cancelButton = new InlineKeyboardButton(CANCEL_MAILING_BUTTON);

        confirmMailingButton.setCallbackData(CONFIRM_MAILING_BUTTON);
        cancelButton.setCallbackData(CANCEL_MAILING_BUTTON);
        row1.add(confirmMailingButton);
        row2.add(cancelButton);
        rows.add(row1);
        rows.add(row2);
        inlineKeyboardMarkup.setKeyboard(rows);

        String text = String.format("""
                Подтвердите сообщение для рассылки:
                """);
        executeMessage(utilService.prepareSendMessage(chatId, text));
        executeMessage(utilService.prepareSendMessage(chatId, photoData, caption, inlineKeyboardMarkup));
        subscribeService.setMailingText(caption);
        subscribeService.setPhotoData(photoData);
    }

    private void unsubscribeCommandReceived(long chatId) {
        String text = "Вы были отключены от рассылки.";
        executeMessage(utilService.prepareSendMessage(chatId, text));
        subscribeService.unSubscribeUser(chatId);
        log.info("пользователь {} отписался от рассылки", chatId);
    }

    private void handleCallbackData(String callbackData, Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        BotState currentState = cache.getUsersCurrentBotState(chatId);
        switch (callbackData) {
            case TO_START_MESSAGE, RESET_MESSAGE, CANCEL_BUTTON:
                startCommandReceived(chatId, update.getCallbackQuery().getMessage().getChat().getFirstName());
                return;
            case TO_SET_CURRENCY_MENU:
                setCurrencyCommandReceived(chatId);
                return;
            case LINK_BUTTON:
                processAskLink(update);
                return;
            case AUCTION_BUTTON:
                processAuction(update);
                return;
            case MANUAL_BUTTON:
                processManualCalculation(chatId);
                return;
            case CONFIRM_MAILING_BUTTON:
                doMailing(chatId);
                return;
            case CANCEL_MAILING_BUTTON:
                mailingMenuCommandReceived(chatId);
                return;
            default:
                break;
        }
        switch (currentState) {
            case ASK_CURRENCY:
                applyCurrencyAndDefineCalculateMode(chatId, callbackData);
                break;
            case SET_CURRENCY_MENU:
                processChooseCurrencyToSet(update, callbackData);
                break;
            case ASK_ISSUE_DATE:
                processIssueDate(chatId, callbackData);
                break;
            case ASK_AUCTION_ISSUE_DATE:
                processAuctionIssueDate(chatId, callbackData);
                break;
            default:
                break;
        }
    }

    /**
     * Расчёт ставки аукциона (в рублях).
     *
     * @param update
     */
    private void processAuction(Update update) {
        var chatId = update.getCallbackQuery().getMessage().getChatId();
        String text = "Введите бюджет в рублях";
        executeMessage(utilService.prepareSendMessage(chatId, text));
        cache.setUsersCurrentBotState(chatId, BotState.ASK_AUCTION_START_PRICE);
    }

    /**
     * Ручная установка валюты для курса оплаты.
     *
     * @param chatId
     */
    private void setCurrencyCommandReceived(long chatId) {
        if (configDataPool.getAdminId() != chatId) {
            executeMessage(utilService.prepareSendMessage(chatId, "Доступ к функционалу ограничен"));
            return;
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        StringBuilder builder = new StringBuilder("");
        InlineKeyboardButton usdButton = new InlineKeyboardButton(USD);
        configDataPool.getCurrencies().forEach(currency -> {
            InlineKeyboardButton button = new InlineKeyboardButton(currency);
            button.setCallbackData(currency);
            row.add(button);
            builder.append(String.format("%n%s = %,.4f ₽", currency, ConfigDataPool.manualConversionRatesMapInRubles.get(currency)));
        });
        String message = String.format("""
                Актуальный курс оплаты:            
                %s
                USD = %,.4f  ₽
                    
                Выберите валюту для ручной установки курса:
                    """, builder, ConfigDataPool.manualConversionRatesMapInRubles.get(USD));
        usdButton.setCallbackData(USD);
        row.add(usdButton);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(utilService.prepareSendMessage(chatId, message, inlineKeyboardMarkup));
        cache.setUsersCurrentBotState(chatId, BotState.SET_CURRENCY_MENU);
    }

    /**
     * Рассылка подписчикам.
     *
     * @param chatId
     */
    private void mailingMenuCommandReceived(long chatId) {
        if (configDataPool.getAdminId() != chatId) {
            executeMessage(utilService.prepareSendMessage(chatId, "Доступ к функционалу ограничен"));
            return;
        }
        String message = """
                Меню рассылки.
                Сообщение будет разослано всем подписчикам бота.            
                                
                Введите текст рассылки (в ответ вы получите предпросмотр рассылаемого сообщения):
                    """;
        executeMessage(utilService.prepareSendMessage(chatId, message));
        cache.setUsersCurrentBotState(chatId, BotState.MAILING_MENU);
        subscribeService.cleanData();
    }


    private void handleMessage(Update update, String receivedText) {
        long chatId = update.getMessage().getChatId();
        BotState currentState = cache.getUsersCurrentBotState(chatId);
        try {
            switch (currentState) {
                case ASK_PRICE:
                    processPrice(chatId, receivedText);
                    break;
                case ASK_AUCTION_START_PRICE:
                    processAuctionStartPrice(chatId, receivedText);
                    break;
                case ASK_VOLUME:
                    processVolume(chatId, receivedText);
                    break;
                case ASK_AUCTION_VOLUME:
                    processAuctionVolume(chatId, receivedText);
                    break;
                case SET_CURRENCY:
                    processSetCurrency(chatId, receivedText);
                    break;
                case WAITING_FOR_LINK:
                    processCalculateByLink(chatId, receivedText);
                    break;
                case MAILING_MENU:
                    processStartMailing(update);
                    break;
                default:
                    break;
            }
        } catch (IllegalArgumentException e) {
            executeMessage(utilService.prepareSendMessage(chatId, "Некорректный формат данных, попробуйте ещё раз."));
            return;
        }
    }

    /**
     * Получили сообщение для рассылки без ФОТО.
     * Сохраняем текст для последующей отправки.
     *
     * @param update
     */
    private void processStartMailing(Update update) {
        long chatId = update.getMessage().getChatId();
        String receivedText = update.getMessage().getText();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton confirmMailingButton = new InlineKeyboardButton(CONFIRM_MAILING_BUTTON);
        InlineKeyboardButton cancelButton = new InlineKeyboardButton(CANCEL_MAILING_BUTTON);

        confirmMailingButton.setCallbackData(CONFIRM_MAILING_BUTTON);
        cancelButton.setCallbackData(CANCEL_MAILING_BUTTON);
        row1.add(confirmMailingButton);
        row2.add(cancelButton);
        rows.add(row1);
        rows.add(row2);
        inlineKeyboardMarkup.setKeyboard(rows);

        String text = String.format("""
                Подтвердите сообщение для рассылки:
                """);
        subscribeService.setMailingText(update.getMessage().getText());
        executeMessage(utilService.prepareSendMessage(chatId, text));
        executeMessage(utilService.prepareSendMessage(chatId, receivedText, inlineKeyboardMarkup));
    }

    private void processSetCurrency(long chatId, String receivedText) {
        String currency = cache.getUserCarData(chatId).getCurrency();
        receivedText = receivedText.replace(',', '.');
        ConfigDataPool.manualConversionRatesMapInRubles.put(currency, Double.valueOf(receivedText));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton reset = new InlineKeyboardButton(TO_START_MESSAGE);
        InlineKeyboardButton toSetCurrencyMenu = new InlineKeyboardButton(TO_SET_CURRENCY_MENU);
        toSetCurrencyMenu.setCallbackData(TO_SET_CURRENCY_MENU);
        reset.setCallbackData(TO_START_MESSAGE);
        row1.add(toSetCurrencyMenu);
        row2.add(reset);
        rows.add(row1);
        rows.add(row2);
        inlineKeyboardMarkup.setKeyboard(rows);

        String message = String.format("Установлен курс: 1 %s = %s  ₽", currency, receivedText);
        executeMessage(utilService.prepareSendMessage(chatId, message, inlineKeyboardMarkup));
        cache.deleteUserCarDataByUserId(chatId);
    }

    /**
     * Процесс ввода стоимости автомобиля.
     *
     * @param chatId
     * @param receivedText
     */
    private void processPrice(long chatId, String receivedText) {
        UserCarInputData data = cache.getUserCarData(chatId);
        int priceInCurrency = Integer.parseInt(receivedText);
        data.setPrice(priceInCurrency);
        data.setPriceInEuro(executionService.convertMoneyToEuro(priceInCurrency, data.getCurrency()));
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
        executeMessage(utilService.prepareSendMessage(chatId, text, inlineKeyboardMarkup));
    }

    /**
     * Процесс ввода бюджета пользователя для режима аукциона.
     *
     * @param chatId
     * @param receivedText
     */
    private void processAuctionStartPrice(long chatId, String receivedText) {
        UserCarInputData data = cache.getUserCarData(chatId);
        data.setCurrency(RUB);
        int auctionStartPrice = Integer.parseInt(receivedText);
        data.setUserAuctionStartPrice(auctionStartPrice);
        data.setPriceInEuro(executionService.convertMoneyToEuro(auctionStartPrice, data.getCurrency()));
        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.ASK_AUCTION_ISSUE_DATE);
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
        executeMessage(utilService.prepareSendMessage(chatId, text, inlineKeyboardMarkup));
    }

    /**
     * Процесс обработки возраста автомобиля.
     *
     * @param chatId
     * @param receivedText
     */
    private void processIssueDate(long chatId, String receivedText) {
        UserCarInputData data = cache.getUserCarData(chatId);
        data.setAge(receivedText);
        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.ASK_VOLUME);
        String text = """
                Введите объем двигателя в кубических сантиметрах.
                                
                Пример: 1995""";

        executeMessage(utilService.prepareSendMessage(chatId, text));
    }

    /**
     * Процесс обработки возраста автомобиля для режима аукциона.
     *
     * @param chatId
     * @param receivedText
     */
    private void processAuctionIssueDate(long chatId, String receivedText) {
        UserCarInputData data = cache.getUserCarData(chatId);
        data.setAge(receivedText);
        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.ASK_AUCTION_VOLUME);
        String text = """
                Введите объем двигателя в кубических сантиметрах.
                                
                Пример: 1995""";

        executeMessage(utilService.prepareSendMessage(chatId, text));
    }

    /**
     * Процесс ввода объема двигателя (запускает расчёт).
     *
     * @param chatId
     * @param receivedText
     */
    private void processVolume(long chatId, String receivedText) {
        UserCarInputData data = cache.getUserCarData(chatId);
        data.setVolume(Integer.parseInt(receivedText));
        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.DATA_PREPARED);
        processExecuteResult(data, chatId);
    }


    /**
     * Процесс ввода объема двигателя для режима аукциона(запускает расчёт).
     *
     * @param chatId
     * @param receivedText
     */
    private void processAuctionVolume(long chatId, String receivedText) {
        UserCarInputData data = cache.getUserCarData(chatId);
        data.setVolume(Integer.parseInt(receivedText));
        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.DATA_PREPARED);
        processAuctionExecuteResult(data, chatId);
    }

    /**
     * Расчитываем стоимость автомобиля под ключ.
     *
     * @param data
     * @param chatId
     */
    private void processExecuteResult(UserCarInputData data, long chatId) {
        String dataPreparedtext = String.format("""
                Данные переданы в обработку ⏳
                 
                %s
                """, data);
        executeMessage(utilService.prepareSendMessage(chatId, dataPreparedtext));
        CarPriceResultData resultData = executionService.executeCarPriceResultData(data);
        cache.deleteUserCarDataByUserId(chatId);
        log.info("""
                Данные рассчёта:
                First price in rubles {},
                Extra pay amount RUB {},
                Extra pay amount curr {},
                Extra pay amount {},
                Fee rate {},
                Duty {},
                Recycling fee {}
                """, resultData.getFirstPriceInRubles(), resultData.getExtraPayAmountInRubles(), resultData.getExtraPayAmountInCurrency(), resultData.getExtraPayAmount(), resultData.getFeeRate(), resultData.getDuty(), resultData.getRecyclingFee());
        String text = utilService.getResultMessageByBotNameAndCurrency(config.getName(), data.getCurrency(), resultData);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton reset = new InlineKeyboardButton(RESET_MESSAGE);
        InlineKeyboardButton manager = new InlineKeyboardButton(MANAGER_MESSAGE);
        reset.setCallbackData(RESET_MESSAGE);
        manager.setUrl(managerLink);
        row1.add(manager);
        row2.add(reset);
        rows.add(row1);
        rows.add(row2);
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(utilService.prepareSendMessage(chatId, text, inlineKeyboardMarkup));
    }

    /**
     * Расчитываем ставку аукциона для введённых данных.
     *
     * @param data
     * @param chatId
     */
    private void processAuctionExecuteResult(UserCarInputData data, long chatId) {
        String dataPreparedtext = String.format(Locale.FRANCE, """
                Данные переданы в обработку ⏳
                Возраст: %s.
                Бюджет: %,.0f ₽
                Объем двигателя: %d cc
                """, data.getAge(), data.getUserAuctionStartPrice(), data.getVolume());
        executeMessage(utilService.prepareSendMessage(chatId, dataPreparedtext));
        int resultAuctionPriceInKrw = executionService.executeAuctionResultInKrw(data);
        cache.deleteUserCarDataByUserId(chatId);
        String text = utilService.getAuctionKrwResultMessage(resultAuctionPriceInKrw);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton reset = new InlineKeyboardButton(RESET_MESSAGE);
        InlineKeyboardButton manager = new InlineKeyboardButton(MANAGER_MESSAGE);
        reset.setCallbackData(RESET_MESSAGE);
        manager.setUrl(managerLink);
        row1.add(manager);
        row2.add(reset);
        rows.add(row1);
        rows.add(row2);
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(utilService.prepareSendMessage(chatId, text, inlineKeyboardMarkup));
    }


    private void startCommandReceived(long chatId, String name) {
        if (configDataPool.isSingleCurrencyMode()) { //singleCurrencyMode не спрашиваем валюту
            processSingleCurrencyStart(chatId, name);
            restService.refreshExchangeRates();
            subscribeService.subscribeUser(chatId);
            return;
        }
        String message = String.format("""
                Здравствуйте, %s!
                        
                Для расчета автомобиля из южной Кореи выберите KRW, для автомобиля из Китая CNY.
                """, name);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton cnyButton = new InlineKeyboardButton(CNY);
        InlineKeyboardButton krwButton = new InlineKeyboardButton(KRW);
        cnyButton.setCallbackData(CNY);
        krwButton.setCallbackData(KRW);
        row.add(cnyButton);
        row.add(krwButton);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(utilService.prepareSendMessage(chatId, message, inlineKeyboardMarkup));
        cache.setUsersCurrentBotState(chatId, BotState.ASK_CURRENCY);
        restService.refreshExchangeRates();
        subscribeService.subscribeUser(chatId);
    }

    private void cbrCommandReceived(long chatId) {
        restService.refreshExchangeRates();
        Map<String, Double> rates = restService.getConversionRatesMap();
        StringBuilder builder = new StringBuilder("");
        configDataPool.getCurrencies().forEach(currency -> {
            builder.append(String.format("%n%s %,.4f ₽", currency, rates.get(RUB) / rates.get(currency)));
        });
        String message = """
                Курс валют ЦБ:
                                
                EUR %,.4f ₽
                USD %,.4f ₽%s
                                
                """.formatted(rates.get(RUB), rates.get(RUB) / rates.get(USD), builder);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton reset = new InlineKeyboardButton(TO_START_MESSAGE);
        reset.setCallbackData(TO_START_MESSAGE);
        row1.add(reset);
        rows.add(row1);
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(utilService.prepareSendMessage(chatId, message, inlineKeyboardMarkup));
    }

    private void currencyRatesCommandReceived(long chatId) {
        StringBuilder builder = new StringBuilder("");
        configDataPool.getCurrencies().forEach(currency -> {
            builder.append(String.format("%n%s = %,.4f  ₽", currency, ConfigDataPool.manualConversionRatesMapInRubles.get(currency)));
        });
        if (configDataPool.getCurrencies().contains(KRW)) {
            builder.append(String.format("%nUSD = %,.2f ₩", restService.getCbrUsdKrwMinus20()));
        }
        String message = String.format("""
                Актуальный курс оплаты:
                                            
                USD = %,.4f  ₽%s
                    """, ConfigDataPool.manualConversionRatesMapInRubles.get(USD), builder);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton reset = new InlineKeyboardButton(TO_START_MESSAGE);
        reset.setCallbackData(TO_START_MESSAGE);
        row.add(reset);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(utilService.prepareSendMessage(chatId, message, inlineKeyboardMarkup));
    }

    /**
     * callback process.
     * Проверяем доступен ли выбор режима расчёта (link/manual) для валюты.
     *
     * @param currency
     */
    private void applyCurrencyAndDefineCalculateMode(long chatId, String currency) {
        UserCarInputData data = cache.getUserCarData(chatId);
        data.setCurrency(currency);
        data.setStock(executionService.executeStock(currency));
        cache.saveUserCarData(chatId, data);
        boolean isLinkModeEnabled = executionService.isLinkModeEnabled(currency);
        boolean isAuctionModeEnabled = executionService.isAuctionModeEnabled(currency);
        if (isLinkModeEnabled || isAuctionModeEnabled) {
            processChooseModeForCalculation(chatId, isLinkModeEnabled, isAuctionModeEnabled);
        } else {
            processManualCalculation(chatId);
        }
    }

    /**
     * Расчёт автомобиля ВРУЧНУЮ.
     */
    private void processManualCalculation(long chatId) {
        UserCarInputData data = cache.getUserCarData(chatId);
        String currency = data.getCurrency();
        String text = String.format("""
                Тип валюты: %s 
                                                
                Теперь введите стоимость автомобиля в валюте.
                """, currency);
        executeMessage(utilService.prepareSendMessage(chatId, text));
        cache.setUsersCurrentBotState(chatId, BotState.ASK_PRICE);
    }

    /**
     * Процесс выбора расчёта ВРУЧНУЮ/ПО ССЫЛКЕ/АУКЦИОН. До трёх кнопок.
     */
    private void processChooseModeForCalculation(long chatId, boolean isLinkModeEnabled, boolean isAuctionModeEnabled) {
        String message = """
                Выберите тип расчёта 🔻
                """;
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        if (isLinkModeEnabled) {
            InlineKeyboardButton linkModeButton = new InlineKeyboardButton(LINK_BUTTON);
            linkModeButton.setCallbackData(LINK_BUTTON);
            row1.add(linkModeButton);
            rows.add(row1);
        }
        InlineKeyboardButton manualModeButton = new InlineKeyboardButton(MANUAL_BUTTON);
        manualModeButton.setCallbackData(MANUAL_BUTTON);
        row2.add(manualModeButton);
        rows.add(row2);
        if (isAuctionModeEnabled) {
            InlineKeyboardButton auctionModeButton = new InlineKeyboardButton(AUCTION_BUTTON);
            auctionModeButton.setCallbackData(AUCTION_BUTTON);
            row3.add(auctionModeButton);
            rows.add(row3);
        }
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(utilService.prepareSendMessage(chatId, message, inlineKeyboardMarkup));
        cache.setUsersCurrentBotState(chatId, BotState.ASK_CALCULATION_MODE);
    }

    /**
     * Пользователь выбрал расчёт по ссылке.
     * Вспоминаем его валюту и спрашиваем соответствующую ссылку.
     *
     * @param update
     */
    private void processAskLink(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        var data = cache.getUserCarData(chatId);
        switch (data.getCurrency()) {
            case KRW -> processAskEncarLink(update);
            default -> log.info("ask link unavaliable for currency");
        }
    }

    private void processAskEncarLink(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        executeMessage(utilService.prepareSendMessage(chatId, "Вставьте ссылку с сайта Encar.com"));
        cache.setUsersCurrentBotState(chatId, BotState.WAITING_FOR_LINK);
    }

    /**
     * Используем полученную ссылку для расчёта стоимости автомобиля.
     *
     * @param link
     */
    private void processCalculateByLink(long chatId, String link) {
        var data = cache.getUserCarData(chatId);
        switch (data.getCurrency()) {
            case KRW -> processCalculateByEncarLink(chatId, link);
            default -> log.info("Link mode unavaliable for currency");
        }
    }

    /**
     * Расчитываем стоимость по ссылке encar.com
     *
     * @param chatId
     * @param link
     */
    private void processCalculateByEncarLink(long chatId, String link) {
        String carId;
        EncarDto encarDto;
        try {
            carId = utilService.parseLinkToCarId(link);
            encarDto = encarCacheService.fetchAndUpdateEncarDtoByCarId(carId);
        } catch (GetCarDetailException | RecaptchaException e) {
            String errorMessage = """
                    Ошибка получения данных с сайта Encar.com
                                        
                    Попробуйте позже...
                    """;
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton cancelButton = new InlineKeyboardButton(CANCEL_BUTTON);
            cancelButton.setCallbackData(CANCEL_BUTTON);
            row.add(cancelButton);
            rows.add(row);
            inlineKeyboardMarkup.setKeyboard(rows);
            executeMessage(utilService.prepareSendMessage(chatId, errorMessage, inlineKeyboardMarkup));
            return;
        }
        UserCarInputData data = cache.getUserCarData(chatId);
        int priceInCurrency = encarDto.getRawCarPrice() * 10_000;
        data.setPrice(priceInCurrency);
        data.setPriceInEuro(executionService.convertMoneyToEuro(priceInCurrency, data.getCurrency()));
        data.setVolume(encarDto.getRawCarPower());
        data.setAge(executionService.calculateCarAgeByRawDate(encarDto.getRawCarYear(), encarDto.getRawCarMonth()));
        data.setCarId(encarDto.getCarId());
        processExecuteResult(data, chatId);
    }

    /**
     * Процесс обработки одной валюты - single valute Mode
     *
     * @param chatId
     * @param name
     */
    private void processSingleCurrencyStart(long chatId, String name) {
        String text = String.format("""
                Здравствуйте, %s! 
                """, name);
        executeMessage(utilService.prepareSendMessage(chatId, text));
        applyCurrencyAndDefineCalculateMode(chatId, configDataPool.singleCurrency());
    }

    /**
     * callback process.
     *
     * @param update
     * @param currency
     */
    private void processChooseCurrencyToSet(Update update, String currency) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String text = String.format("""
                Вы выбрали тип валюты: %s 
                                                
                Теперь введите курс валюты к рублю.
                                                
                Например 1.234
                В таком случае будет установлен курс 1 %s = 1.234  ₽
                """, currency, currency);
        executeEditMessageText(text, chatId, update.getCallbackQuery().getMessage().getMessageId());
        UserCarInputData data = cache.getUserCarData(chatId);
        data.setCurrency(currency);
        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.SET_CURRENCY);
    }


    private void unrecognizedCommandReceived(long chatId) {
        executeMessage(utilService.prepareSendMessage(chatId, "Команда не распознана, чтобы начать - нажмите /start"));
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

    private void executeMessage(SendPhoto message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Делаем рассылку на основании данных в сервисе подписчиков. Если есть фото-данные, то
     * отправляем данные с фото, иначе отправляем просто текст рассылки. После рассылки удаляем
     * данные для рассылки.
     *
     * @param chatId
     */
    private void doMailing(long chatId) {
        String startMessage = """
                Рассылка запущена. 
                Дождитесь уведомление об окончании рассылки прежде, чем начать новую рассылку.
                    """;
        executeMessage(utilService.prepareSendMessage(chatId, startMessage));
        List<Long> subscriptionIds = subscribeService.getSubscribers();
        subscriptionIds.forEach(id -> {
            try {
                if (Objects.nonNull(subscribeService.getPhotoData())) {
                    executeMessage(utilService.prepareSendMessage(id, subscribeService.getPhotoData(), subscribeService.getMailingText()));
                } else {
                    executeMessage(utilService.prepareSendMessage(id, subscribeService.getMailingText()));
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.warn("Interrupted!", e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        });
        String finishMessage = """
                Рассылка успешно завершена.
                Теперь можно вернуться в меню рассылок /mail 
                    """;
        executeMessage(utilService.prepareSendMessage(chatId, finishMessage));
        subscribeService.cleanData();
    }


}
