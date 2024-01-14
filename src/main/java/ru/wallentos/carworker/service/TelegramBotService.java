package ru.wallentos.carworker.service;

import static ru.wallentos.carworker.configuration.ConfigDataPool.AUCTION_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.CANCEL_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.CANCEL_MAILING_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.CAR_REPORT_BUTTON_CALLBACK;
import static ru.wallentos.carworker.configuration.ConfigDataPool.CAR_REPORT_BUTTON_TEXT;
import static ru.wallentos.carworker.configuration.ConfigDataPool.CAR_RESULT_DETAIL_BUTTON_CALLBACK;
import static ru.wallentos.carworker.configuration.ConfigDataPool.CAR_RESULT_DETAIL_BUTTON_TEXT;
import static ru.wallentos.carworker.configuration.ConfigDataPool.CLIENT_REQUEST_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.CNY;
import static ru.wallentos.carworker.configuration.ConfigDataPool.CONFIRM_MAILING_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.KRW;
import static ru.wallentos.carworker.configuration.ConfigDataPool.LINK_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.MANAGER_MESSAGE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.MANUAL_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NEW_CAR;
import static ru.wallentos.carworker.configuration.ConfigDataPool.NORMAL_CAR;
import static ru.wallentos.carworker.configuration.ConfigDataPool.OLD_CAR;
import static ru.wallentos.carworker.configuration.ConfigDataPool.READY_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.RESET_CALLBACK;
import static ru.wallentos.carworker.configuration.ConfigDataPool.RESET_MANAGER_BUTTON;
import static ru.wallentos.carworker.configuration.ConfigDataPool.RESET_MANAGER_CALLBACK;
import static ru.wallentos.carworker.configuration.ConfigDataPool.RESET_MESSAGE;
import static ru.wallentos.carworker.configuration.ConfigDataPool.RUB;
import static ru.wallentos.carworker.configuration.ConfigDataPool.TO_SET_CURRENCY_MENU;
import static ru.wallentos.carworker.configuration.ConfigDataPool.USD;
import static ru.wallentos.carworker.configuration.ConfigDataPool.conversionRatesMap;

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
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
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
import ru.wallentos.carworker.model.CarDto;
import ru.wallentos.carworker.model.CarPriceResultData;
import ru.wallentos.carworker.model.CarTotalResultData;
import ru.wallentos.carworker.model.Province;
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
    private UtilMessageService utilMessageService;
    @Autowired
    private EncarCacheService encarCacheService;
    @Autowired
    private CheCarCacheService cheCarCacheService;
    @Autowired
    private ExecutionService executionService;
    @Autowired
    private SubscribeService subscribeService;
    @Autowired
    private GoogleService googleService;
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
                    startCommandReceived(update.getMessage());
                    break;
                case "/cbr":
                    cbrCommandReceived(chatId);
                    break;
                case "/currencyrates":
                    currencyRatesCommandReceived(chatId);
                    break;
                case "/settingservice":
                    setCurrencyCommandReceived(chatId, update.getMessage());
                    break;
                case "/mail":
                    mailingMenuCommandReceived(chatId, update.getMessage());
                    break;
                case "/sleep":
                    unsubscribeCommandReceived(chatId);
                    break;
                case "/total":
                    totalManagerCommandReceived(chatId);
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
     * Получена команда от манагеров, чтобы посчитать ЮАНИ. Надо спросить у них ссылку.
     *
     * @param chatId
     */
    private void totalManagerCommandReceived(long chatId) {
        String text = String.format("""
                Здравствуйте, пожалуйста отправьте ссылку с сайта che168.com
                """);
        executeMessage(utilMessageService.prepareSendMessage(chatId, text));
        cache.setUsersCurrentBotState(chatId, BotState.ASK_CHINA_LINK);
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
        executeMessage(utilMessageService.prepareSendMessage(chatId, text));
        executeMessage(utilMessageService.prepareSendMessage(chatId, photoData, caption, inlineKeyboardMarkup));
        subscribeService.setMailingText(caption);
        subscribeService.setPhotoData(photoData);
    }

    private void unsubscribeCommandReceived(long chatId) {
        String text = "Вы были отключены от рассылки.";
        executeMessage(utilMessageService.prepareSendMessage(chatId, text));
        subscribeService.unSubscribeUser(chatId);
        log.info("пользователь {} отписался от рассылки", chatId);
    }

    private void handleCallbackData(String callbackData, Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        BotState currentState = cache.getUsersCurrentBotState(chatId);
        switch (callbackData) {
            case RESET_CALLBACK, CANCEL_BUTTON:
                startCommandReceived(update.getCallbackQuery().getMessage());
                return;
            case TO_SET_CURRENCY_MENU:
                setCurrencyCommandReceived(chatId, update.getCallbackQuery().getMessage());
                return;
            case LINK_BUTTON:
                processAskLink(update);
                return;
            case AUCTION_BUTTON:
                processAuction(update);
                return;
            case MANUAL_BUTTON:
                processManualCalculation(update.getCallbackQuery().getMessage());
                return;
            case CONFIRM_MAILING_BUTTON:
                doMailing(chatId);
                return;
            case CANCEL_MAILING_BUTTON:
                mailingMenuCommandReceived(chatId, update.getCallbackQuery().getMessage());
                return;
            case CLIENT_REQUEST_BUTTON:
                clientRequestStartCommand(chatId);
                return;
            case CAR_REPORT_BUTTON_CALLBACK:
                processReport(chatId, update);
                return;
            case CAR_RESULT_DETAIL_BUTTON_CALLBACK:
                processResultDetalization(chatId);
                return;
            case RESET_MANAGER_CALLBACK:
                totalManagerCommandReceived(chatId);
                return;
            default:
                break;
        }
        switch (currentState) {
            case ASK_CURRENCY:
                applyCurrencyAndDefineCalculateMode(update.getCallbackQuery().getMessage(), callbackData);
                break;
            case SET_CURRENCY_MENU:
                processChooseCurrencyToSet(update, callbackData);
                break;
            case ASK_ISSUE_DATE:
                processIssueDate(update.getCallbackQuery().getMessage(), callbackData);
                break;
            case ASK_AUCTION_ISSUE_DATE:
                processAuctionIssueDate(update.getCallbackQuery().getMessage(), callbackData);
                break;
            default:
                break;
        }
    }

    /**
     * Детализированное сообщение о расчёте.
     *
     * @param chatId
     */
    private void processResultDetalization(long chatId) {
        CarPriceResultData resultData = cache.getResultCarData(chatId);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();

        if (!configDataPool.isManagerBot) {
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton manager = new InlineKeyboardButton(MANAGER_MESSAGE);
            manager.setUrl(managerLink);
            row2.add(manager);
            rows.add(row2);
        }

        InlineKeyboardButton reset = new InlineKeyboardButton(RESET_MESSAGE);
        reset.setCallbackData(RESET_CALLBACK);
        row3.add(reset);
        rows.add(row3);

        inlineKeyboardMarkup.setKeyboard(rows);

        String text = utilMessageService.getResultDetailMessageByBotNameAndCurrency(config.getName(), resultData.getCurrency(), resultData);
        executeMessage(utilMessageService.prepareSendMessage(chatId, text, inlineKeyboardMarkup));
        cache.deleteResultCarDataByUserId(chatId);
    }

    /**
     * Подготовка отчёта по автомобилю
     *
     * @param chatId идентификатор пользователя
     * @param update сущность из поступившего сообщения
     */
    private void processReport(long chatId, Update update) {
        String carId = update.getCallbackQuery().getMessage().getEntities().get(2).getUrl().substring(34);
        CarDto car = getEncarCacheService().getEncarDtoFromCache(carId);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();

        if (!configDataPool.isManagerBot) {
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton manager = new InlineKeyboardButton(MANAGER_MESSAGE);
            manager.setUrl(managerLink);
            row2.add(manager);
            rows.add(row2);
        }

        InlineKeyboardButton reset = new InlineKeyboardButton(RESET_MESSAGE);
        reset.setCallbackData(RESET_CALLBACK);
        row3.add(reset);
        rows.add(row3);

        inlineKeyboardMarkup.setKeyboard(rows);

        executeMessage(utilMessageService.prepareSendMessage(chatId, utilMessageService.getEncarReportMessage(car), inlineKeyboardMarkup));
    }

    /**
     * Оставить заявку для группы админов.
     *
     * @param chatId
     */
    private void clientRequestStartCommand(long chatId) {
        String text = """ 
                Вы находитесь в меню составления заявки.
                                
                Пожалуйста, отправьте ваш запрос в ответном сообщении!""";
        Message sendOutMessage = executeMessage(utilMessageService.prepareSendMessage(chatId, text));

        // запомнить сообщение для удаления
        UserCarInputData data = cache.getUserCarData(chatId);
        data.setLastMessageToDelete(sendOutMessage);
        cache.saveUserCarData(chatId, data);

        cache.setUsersCurrentBotState(chatId, BotState.ASK_CLIENT_REQUEST_MESSAGE);
    }

    /**
     * Отправить клиентскую завяку группе менеджеров.
     *
     * @param update
     */
    private void clientRequestProcessCommand(Update update, boolean first) {
        long chatId = update.getMessage().getChatId();

        // запомнить сообщение для удаления
        UserCarInputData data = cache.getUserCarData(chatId);
        // Удаляем сообщения
        deleteMessage(data.getLastMessageToDelete());
        deleteMessage(update.getMessage());

        String clientUsername = update.getMessage().getChat().getUserName();
        String clientText = update.getMessage().getText();
        // если первичное обращение к методу, значит в тексте заявка
        if (first) {
            data.setClientMessage(clientText);
        }
        String clientContact;
        if (Objects.isNull(clientUsername)) {
            if (Objects.isNull(data.getUserContact())) {
                processAskContact(chatId, data);
                return;
            } else {
                clientContact = clientText;
            }
        } else {
            clientContact = clientUsername;
        }
        String textToClient = "Ваша заявка принята, в ближайшее время с вами свяжется наш " + "менеджер!";
        String textToGroup = String.format("""
                Получена заявка от пользователя %s
                                
                %s                
                """, clientContact, data.getClientMessage());

        // отправляем запрос в группу менеджеров.
        executeMessage(utilMessageService.prepareSendMessage(configDataPool.getClientRequestGroupId(), textToGroup));
        // отправляем заявку в гугл таблицу
        googleService.appendClientRequestToGoogleSheet(data.getClientMessage(), clientContact);


        cache.deleteUserCarDataByUserId(chatId);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton reset = new InlineKeyboardButton(RESET_MESSAGE);
        InlineKeyboardButton manager = new InlineKeyboardButton(MANAGER_MESSAGE);
        InlineKeyboardButton cliendRequest = new InlineKeyboardButton(CLIENT_REQUEST_BUTTON);
        reset.setCallbackData(RESET_CALLBACK);
        manager.setUrl(managerLink);
        cliendRequest.setCallbackData(CLIENT_REQUEST_BUTTON);
        row1.add(manager);
        row2.add(reset);
        row3.add(cliendRequest);
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        inlineKeyboardMarkup.setKeyboard(rows);

        // отправляем ответ клиенту.
        Message sendOutMessage = executeMessage(utilMessageService.prepareSendMessage(chatId, textToClient, inlineKeyboardMarkup));

        data.setLastMessageToDelete(sendOutMessage);
        cache.saveUserCarData(chatId, data);
    }

    /**
     * Сохранить контакт клиента для обратной связи.
     *
     * @param update
     */
    private void clientContactReceivedCommand(long chatId, Update update) {
        // запомнить сообщение для удаления
        UserCarInputData data = cache.getUserCarData(chatId);
        // Удаляем сообщения
        deleteMessage(data.getLastMessageToDelete());
        deleteMessage(update.getMessage());
        String clientContact = update.getMessage().getText();
        data.setUserContact(clientContact);
        cache.saveUserCarData(chatId, data);
        clientRequestProcessCommand(update, false);
    }

    /**
     * Уточнить контакт для связи и сохранить его в базу
     *
     * @param chatId
     * @param data
     */
    private void processAskContact(long chatId, UserCarInputData data) {
        String text = "Мне не удалось обнаружить ваш Telegram ID, пожалуйста, отправьте ваш номер" + " телефона ответным сообщением, что бы наши менеджеры смогли с вами связаться.";
        Message sendOutMessage = executeMessage(utilMessageService.prepareSendMessage(chatId, text));
        data.setLastMessageToDelete(sendOutMessage);
        cache.setUsersCurrentBotState(chatId, BotState.ASK_CLIENT_CONTACT);
    }

    /**
     * Расчёт ставки аукциона (в рублях).
     *
     * @param update
     */
    private void processAuction(Update update) {
        var chatId = update.getCallbackQuery().getMessage().getChatId();

        String text = "Пожалуйста, введите бюджет в рублях";
        Message sendOutMessage = executeMessage(utilMessageService.prepareSendMessage(chatId, text));


        UserCarInputData data = cache.getUserCarData(chatId);

        // Удаляем сообщения
        deleteMessage(data.getLastMessageToDelete());
        deleteMessage(update.getCallbackQuery().getMessage());

        // запомнить сообщение для удаления
        data.setLastMessageToDelete(sendOutMessage);

        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.ASK_AUCTION_START_PRICE);
    }

    /**
     * Ручная установка валюты для курса оплаты.
     *
     * @param chatId
     */
    private void setCurrencyCommandReceived(long chatId, Message updateMessage) {
        String userName = updateMessage.getChat().getUserName();

        if (configDataPool.getAdminList().stream().noneMatch(userName::equalsIgnoreCase)) {
            executeMessage(utilMessageService.prepareSendMessage(chatId, "Доступ к функционалу ограничен"));
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
                                
                Пожалуйста, выберите валюту для ручной установки курса:
                    """, builder, ConfigDataPool.manualConversionRatesMapInRubles.get(USD));
        usdButton.setCallbackData(USD);
        row.add(usdButton);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(utilMessageService.prepareSendMessage(chatId, message, inlineKeyboardMarkup));
        cache.setUsersCurrentBotState(chatId, BotState.SET_CURRENCY_MENU);
    }

    /**
     * Рассылка подписчикам.
     *
     * @param chatId
     */
    private void mailingMenuCommandReceived(long chatId, Message updateMessage) {
        String userName = updateMessage.getChat().getUserName().toLowerCase();

        if (!configDataPool.getAdminList().contains(userName)) {
            executeMessage(utilMessageService.prepareSendMessage(chatId, "Доступ к функционалу ограничен"));
            return;
        }
        long subCount = subscribeService.getSubscribers().size();
        String message = String.format("""
                Меню рассылки.
                Количество подписчиков: %d.
                Сообщение будет разослано всем подписчикам бота.            
                                
                Введите текст рассылки (в ответ вы получите предпросмотр рассылаемого сообщения):
                    """, subCount);
        executeMessage(utilMessageService.prepareSendMessage(chatId, message));
        cache.setUsersCurrentBotState(chatId, BotState.MAILING_MENU);
        subscribeService.cleanData();
    }


    private void handleMessage(Update update, String receivedText) {
        long chatId = update.getMessage().getChatId();
        BotState currentState = cache.getUsersCurrentBotState(chatId);
        try {
            switch (currentState) {
                case ASK_PRICE:
                    processPrice(update.getMessage(), receivedText);
                    break;
                case ASK_AUCTION_START_PRICE:
                    processAuctionStartPrice(update.getMessage(), receivedText);
                    break;
                case ASK_VOLUME:
                    processVolume(update.getMessage(), receivedText);
                    break;
                case ASK_AUCTION_VOLUME:
                    processAuctionVolume(update.getMessage(), receivedText);
                    break;
                case SET_CURRENCY:
                    processSetCurrency(chatId, receivedText);
                    break;
                case WAITING_FOR_LINK:
                    processCalculateByLink(update.getMessage(), receivedText);
                    break;
                case MAILING_MENU:
                    processStartMailing(update);
                    break;
                case ASK_CLIENT_REQUEST_MESSAGE:
                    clientRequestProcessCommand(update, true);
                    break;
                case ASK_CLIENT_CONTACT:
                    clientContactReceivedCommand(chatId, update);
                    break;
                case ASK_CHINA_LINK:
                    processCalculateByCheCarLinkForManagers(update.getMessage(),
                            update.getMessage().getText());
                    break;
                default:
                    break;
            }
        } catch (IllegalArgumentException e) {
            executeMessage(utilMessageService.prepareSendMessage(chatId, "Некорректный формат данных, попробуйте ещё раз."));
            return;
        }
    }

    /**
     * Получена ссылка на che168 , посчитать тачки и вывести ответ манагерам.
     *
     * @param chatId
     * @param update
     */
    private void executeChinaResult(long chatId, Update update) {
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
        executeMessage(utilMessageService.prepareSendMessage(chatId, text));
        executeMessage(utilMessageService.prepareSendMessage(chatId, receivedText, inlineKeyboardMarkup));
    }

    private void processSetCurrency(long chatId, String receivedText) {
        String currency = cache.getUserCarData(chatId).getCurrency();
        receivedText = receivedText.replace(',', '.');
        ConfigDataPool.manualConversionRatesMapInRubles.put(currency, Double.valueOf(receivedText));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton reset = new InlineKeyboardButton(RESET_MESSAGE);
        InlineKeyboardButton toSetCurrencyMenu = new InlineKeyboardButton(TO_SET_CURRENCY_MENU);
        toSetCurrencyMenu.setCallbackData(TO_SET_CURRENCY_MENU);
        reset.setCallbackData(RESET_CALLBACK);
        row1.add(toSetCurrencyMenu);
        row2.add(reset);
        rows.add(row1);
        rows.add(row2);
        inlineKeyboardMarkup.setKeyboard(rows);

        String message = String.format("Установлен курс: 1 %s = %s  ₽", currency, receivedText);
        executeMessage(utilMessageService.prepareSendMessage(chatId, message, inlineKeyboardMarkup));
        cache.deleteUserCarDataByUserId(chatId);
    }

    /**
     * Процесс ввода стоимости автомобиля.
     *
     * @param receivedText
     */
    private void processPrice(Message message, String receivedText) {
        long chatId = message.getChatId();
        UserCarInputData data = cache.getUserCarData(chatId);

        // Удаляем сообщения
        deleteMessage(data.getLastMessageToDelete());
        deleteMessage(message);

        int priceInCurrency = Integer.parseInt(receivedText);
        data.setPrice(priceInCurrency);
        data.setPriceInEuro(executionService.convertMoneyToEuro(priceInCurrency, data.getCurrency()));
        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.ASK_ISSUE_DATE);
        String text = "Пожалуйста, выберите возраст автомобиля:";
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
        executeMessage(utilMessageService.prepareSendMessage(chatId, text, inlineKeyboardMarkup));
    }

    /**
     * Процесс ввода бюджета пользователя для режима аукциона.
     *
     * @param receivedText
     */
    private void processAuctionStartPrice(Message message, String receivedText) {
        long chatId = message.getChatId();
        UserCarInputData data = cache.getUserCarData(chatId);
        data.setCurrency(RUB);
        int auctionStartPrice = Integer.parseInt(receivedText);
        data.setUserAuctionStartPrice(auctionStartPrice);
        data.setPriceInEuro(executionService.convertMoneyToEuro(auctionStartPrice, data.getCurrency()));

        // Удаляем сообщения
        deleteMessage(data.getLastMessageToDelete());
        deleteMessage(message);

        String text = "Пожалуйста, выберите возраст автомобиля:";
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


        Message sendOutMessage = executeMessage(utilMessageService.prepareSendMessage(chatId, text, inlineKeyboardMarkup));
        data.setLastMessageToDelete(sendOutMessage);
        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.ASK_AUCTION_ISSUE_DATE);

    }

    /**
     * Процесс обработки возраста автомобиля.
     *
     * @param receivedText
     */
    private void processIssueDate(Message message, String receivedText) {
        long chatId = message.getChatId();
        deleteMessage(message);

        UserCarInputData data = cache.getUserCarData(chatId);
        data.setAge(receivedText);
        cache.setUsersCurrentBotState(chatId, BotState.ASK_VOLUME);
        String text = """
                Пожалуйста, введите объем двигателя в кубических сантиметрах.
                                
                Пример: 1995""";

        Message sendOutMessage = executeMessage(utilMessageService.prepareSendMessage(chatId, text));
        data.setLastMessageToDelete(sendOutMessage);
        cache.saveUserCarData(chatId, data);
    }

    /**
     * Процесс обработки возраста автомобиля для режима аукциона.
     *
     * @param receivedText
     */
    private void processAuctionIssueDate(Message message, String receivedText) {
        long chatId = message.getChatId();
        UserCarInputData data = cache.getUserCarData(chatId);

        // Удаляем сообщения
        deleteMessage(data.getLastMessageToDelete());

        data.setAge(receivedText);
        String text = """
                Пожалуйста, введите объем двигателя в кубических сантиметрах.
                                
                Пример: 1995""";

        Message sendOutMessage = executeMessage(utilMessageService.prepareSendMessage(chatId, text));

        data.setLastMessageToDelete(sendOutMessage);
        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.ASK_AUCTION_VOLUME);

    }

    /**
     * Процесс ввода объема двигателя (запускает расчёт).
     *
     * @param receivedText
     */
    private void processVolume(Message message, String receivedText) {
        long chatId = message.getChatId();
        UserCarInputData data = cache.getUserCarData(chatId);

        // Удаляем сообщения
        deleteMessage(data.getLastMessageToDelete());
        deleteMessage(message);

        data.setVolume(Integer.parseInt(receivedText));
        cache.saveUserCarData(chatId, data);
        cache.setUsersCurrentBotState(chatId, BotState.DATA_PREPARED);
        processExecuteResultAndShowHeader(data, chatId);
    }


    /**
     * Процесс ввода объема двигателя для режима аукциона(запускает расчёт).
     *
     * @param receivedText
     */
    private void processAuctionVolume(Message message, String receivedText) {
        long chatId = message.getChatId();
        UserCarInputData data = cache.getUserCarData(chatId);
        data.setVolume(Integer.parseInt(receivedText));

        // Удаляем сообщения
        deleteMessage(data.getLastMessageToDelete());
        deleteMessage(message);

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
    private void processExecuteResultAndShowHeader(UserCarInputData data, long chatId) {
        String dataPreparedtext = String.format("""
                Данные переданы в обработку ⏳
                 
                %s
                """, data);
        executeMessage(utilMessageService.prepareSendMessage(chatId, dataPreparedtext));
        CarPriceResultData resultData = executionService.executeCarPriceResultData(data);
        int carId = data.getCarId();
        log.info("""
                        Данные рассчёта:
                        First price in rubles {},
                        Extra pay amount RUB {},
                        Extra pay amount curr {},
                        Extra pay amount {},
                        Fee rate {},
                        Duty {},
                        Recycling fee {}
                        """, resultData.getFirstPriceInRubles(), resultData.getExtraPayAmountRublePart(),
                resultData.getExtraPayAmountValutePart(), resultData.getExtraPayAmountValutePart(),
                resultData.getFeeRate(), resultData.getDuty(), resultData.getRecyclingFee());
        String text = utilMessageService.getResultHeaderMessageByBotNameAndCurrency(config.getName(), data.getCurrency(), resultData);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();

        if (configDataPool.enableResultDetalization) {
            List<InlineKeyboardButton> row0 = new ArrayList<>();
            InlineKeyboardButton carResultDetail = new InlineKeyboardButton(CAR_RESULT_DETAIL_BUTTON_TEXT);
            carResultDetail.setCallbackData(CAR_RESULT_DETAIL_BUTTON_CALLBACK);
            row0.add(carResultDetail);
            rows.add(row0);
        }

        if (carId != 0 && configDataPool.isEnableEncarReportMode() && data.isHasInsuranceInfo()) {
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            InlineKeyboardButton carReport = new InlineKeyboardButton(CAR_REPORT_BUTTON_TEXT);
            carReport.setCallbackData(CAR_REPORT_BUTTON_CALLBACK);
            row1.add(carReport);
            rows.add(row1);
        }

        if (configDataPool.isEnableClientRequest()) {
            InlineKeyboardButton cliendRequest = new InlineKeyboardButton(CLIENT_REQUEST_BUTTON);
            List<InlineKeyboardButton> row4 = new ArrayList<>();
            cliendRequest.setCallbackData(CLIENT_REQUEST_BUTTON);
            row4.add(cliendRequest);
            rows.add(row4);
        }

        if (!configDataPool.isManagerBot) {
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton manager = new InlineKeyboardButton(MANAGER_MESSAGE);
            manager.setUrl(managerLink);
            row2.add(manager);
            rows.add(row2);
        }

        InlineKeyboardButton reset = new InlineKeyboardButton(RESET_MESSAGE);
        reset.setCallbackData(RESET_CALLBACK);
        row3.add(reset);
        rows.add(row3);

        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(utilMessageService.prepareSendMessage(chatId, text, inlineKeyboardMarkup));
        // удаляем исходные данные и сохраняем результат для детализации
        cache.deleteUserCarDataByUserId(chatId);
        cache.saveResultCarData(chatId, resultData);
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
        executeMessage(utilMessageService.prepareSendMessage(chatId, dataPreparedtext));
        int resultAuctionPriceInKrw = executionService.executeAuctionResultInKrw(data);
        cache.deleteUserCarDataByUserId(chatId);
        String text = utilMessageService.getAuctionKrwResultMessage(resultAuctionPriceInKrw);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton reset = new InlineKeyboardButton(RESET_MESSAGE);
        reset.setCallbackData(RESET_CALLBACK);

        if (!configDataPool.isManagerBot) {
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            InlineKeyboardButton manager = new InlineKeyboardButton(MANAGER_MESSAGE);
            manager.setUrl(managerLink);
            row1.add(manager);
            rows.add(row1);
        }

        row2.add(reset);
        rows.add(row2);
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(utilMessageService.prepareSendMessage(chatId, text, inlineKeyboardMarkup));
    }


    private void startCommandReceived(Message message) {
        long chatId = message.getChatId();
        String name = message.getChat().getFirstName();

        if (configDataPool.isManagerBot && configDataPool.getWhiteManagerList().stream().noneMatch(message.getChat().getUserName()::equalsIgnoreCase)) {
            String text = String.format("""
                    Отсутствуют правa доступа к функционалу.
                    Для использования бота пройдите по ссылке: %s
                    """, configDataPool.getParentLink());
            executeMessage(utilMessageService.prepareSendMessage(chatId, text));
            return;
        }
        if (configDataPool.isCheckChannelSubscribers && !isChannelSubscriber(chatId)) {
            String text = String.format("""
                    Здравствуйте %s! Для доступа к калькулятору, пожалуйста подпишитесь на <a href="%s">🔗Официальный телеграмм канал</a>     
                    """, name, configDataPool.getChannelSubscribersLink());

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton readyButton = new InlineKeyboardButton(READY_BUTTON);
            readyButton.setCallbackData(RESET_CALLBACK);
            row.add(readyButton);
            rows.add(row);
            inlineKeyboardMarkup.setKeyboard(rows);
            executeMessage(utilMessageService.prepareSendMessage(chatId, text, inlineKeyboardMarkup));
            return;
        }

        // Удаляем сообщения
        UserCarInputData data = cache.getUserCarData(chatId);
        deleteMessage(data.getLastMessageToDelete());
        deleteMessage(data.getPreLastMessageToDelete());

        if (configDataPool.isSingleCurrencyMode()) { //singleCurrencyMode не спрашиваем валюту
            processSingleCurrencyStart(message, name);
            restService.refreshExchangeRates();
            subscribeService.subscribeUser(chatId);
            return;
        }
        String text = String.format("""
                Здравствуйте, %s!
                        
                Для расчёта автомобиля из южной Кореи, пожалуйста, выберите KRW, для автомобиля из Китая CNY.
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
        executeMessage(utilMessageService.prepareSendMessage(chatId, text, inlineKeyboardMarkup));
        cache.setUsersCurrentBotState(chatId, BotState.ASK_CURRENCY);
        restService.refreshExchangeRates();
        subscribeService.subscribeUser(chatId);
    }

    private void cbrCommandReceived(long chatId) {
        restService.refreshExchangeRates();
        Map<String, Double> rates = conversionRatesMap;
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
        InlineKeyboardButton reset = new InlineKeyboardButton(RESET_MESSAGE);
        reset.setCallbackData(RESET_CALLBACK);
        row1.add(reset);
        rows.add(row1);
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(utilMessageService.prepareSendMessage(chatId, message, inlineKeyboardMarkup));
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
        InlineKeyboardButton reset = new InlineKeyboardButton(RESET_MESSAGE);
        reset.setCallbackData(RESET_CALLBACK);
        row.add(reset);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        executeMessage(utilMessageService.prepareSendMessage(chatId, message, inlineKeyboardMarkup));
    }

    /**
     * callback process.
     * Проверяем доступен ли выбор режима расчёта (link/manual) для валюты.
     *
     * @param currency
     */
    private void applyCurrencyAndDefineCalculateMode(Message message, String currency) {
        long chatId = message.getChatId();
        UserCarInputData data = cache.getUserCarData(chatId);
        data.setCurrency(currency);
        data.setStock(executionService.executeStock(currency));

        // Если в сообщении на удаление есть Здравствуйте, значит можно удалять
        if (message.getText().contains("Здравствуйте")) {
            deleteMessage(message);
        }

        cache.saveUserCarData(chatId, data);
        boolean isLinkModeEnabled = executionService.isLinkModeEnabled(currency);
        boolean isAuctionModeEnabled = executionService.isAuctionModeEnabled(currency);
        if (isLinkModeEnabled || isAuctionModeEnabled) {
            processChooseModeForCalculation(chatId, isLinkModeEnabled, isAuctionModeEnabled);
        } else {
            processManualCalculation(message);
        }
    }

    /**
     * Расчёт автомобиля ВРУЧНУЮ.
     */
    private void processManualCalculation(Message message) {
        long chatId = message.getChatId();

        UserCarInputData data = cache.getUserCarData(chatId);

        deleteMessage(data.getLastMessageToDelete());
        deleteMessage(message);

        String currency = data.getCurrency();
        String text = String.format("""
                Тип валюты: %s 
                                                
                Пожалуйста, введите стоимость автомобиля в валюте.
                """, currency);
        Message sendOutMessage = executeMessage(utilMessageService.prepareSendMessage(chatId, text));
        data.setLastMessageToDelete(sendOutMessage);
        cache.saveUserCarData(chatId, data);

        cache.setUsersCurrentBotState(chatId, BotState.ASK_PRICE);
    }

    /**
     * Процесс выбора расчёта ВРУЧНУЮ/ПО ССЫЛКЕ/АУКЦИОН. До трёх кнопок.
     */
    private void processChooseModeForCalculation(long chatId, boolean isLinkModeEnabled, boolean isAuctionModeEnabled) {
        String message = """
                Вы можете выбрать тип расчёта 🔻
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
        executeMessage(utilMessageService.prepareSendMessage(chatId, message, inlineKeyboardMarkup));
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

        // Удаляем сообщения
        if (Objects.nonNull(data.getLastMessageToDelete())) {
            try {
                deleteMessage(data.getLastMessageToDelete());
            } catch (Exception e) {
                log.info("сообщение уже удалено");
            }
        }
        deleteMessage(update.getCallbackQuery().getMessage());

        switch (data.getCurrency()) {
            case KRW -> processAskEncarLink(update);
            case CNY -> processAskCheCarLink(update);
            default -> log.info("ask link unavaliable for currency");
        }
    }

    /**
     * Спрашиваем ссылку на корейский сайт.
     *
     * @param update
     */
    private void processAskEncarLink(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        Message sendOutMessage = executeMessage(utilMessageService.prepareSendMessage(chatId, "Пожалуйста, вставьте ссылку с сайта Encar.com"));

        // запомнить сообщение для удаления
        UserCarInputData data = cache.getUserCarData(chatId);
        data.setLastMessageToDelete(sendOutMessage);
        cache.saveUserCarData(chatId, data);

        cache.setUsersCurrentBotState(chatId, BotState.WAITING_FOR_LINK);
    }

    /**
     * Спрашиваем ссылку на китайский сайт.
     *
     * @param update
     */
    private void processAskCheCarLink(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        Message sendOutMessage = executeMessage(utilMessageService.prepareSendMessage(chatId, "Пожалуйста, вставьте ссылку с сайта che168.com"));

        // запомнить сообщение для удаления
        UserCarInputData data = cache.getUserCarData(chatId);
        data.setLastMessageToDelete(sendOutMessage);
        cache.saveUserCarData(chatId, data);

        cache.setUsersCurrentBotState(chatId, BotState.WAITING_FOR_LINK);
    }

    /**
     * Используем полученную ссылку для расчёта стоимости автомобиля.
     *
     * @param link
     */
    private void processCalculateByLink(Message message, String link) {
        long chatId = message.getChatId();
        var data = cache.getUserCarData(chatId);
        switch (data.getCurrency()) {
            case KRW -> processCalculateByEncarLink(message, link);
            case CNY -> processCalculateByCheCarLink(message, link);
            default -> log.info("Link mode unavaliable for currency");
        }
    }

    /**
     * Расчитываем стоимость по ссылке encar.com
     *
     * @param link
     */
    private void processCalculateByEncarLink(Message message, String link) {
        long chatId = message.getChatId();
        String carId;
        CarDto carDto;
        try {
            carId = utilService.parseLinkToCarId(link);
            carDto = encarCacheService.fetchAndUpdateEncarDtoByCarId(carId);
        } catch (GetCarDetailException | RecaptchaException e) {
            String errorMessage = """
                    Ошибка получения данных с сайта Encar.com
                                        
                    Попробуйте позже...
                    
                    Если вы пытались отправить ссылку с мобильной версии сайта, пожалуйста, отправьте ссылку с полной версии сайта(внизу кнопка <b>PC버전</b>).
                    """;
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton cancelButton = new InlineKeyboardButton(CANCEL_BUTTON);
            cancelButton.setCallbackData(CANCEL_BUTTON);
            row.add(cancelButton);
            rows.add(row);
            inlineKeyboardMarkup.setKeyboard(rows);
            Message sendOutMessage = executeMessage(utilMessageService.prepareSendMessage(chatId, errorMessage, inlineKeyboardMarkup));

            // запомнить сообщение для удаления
            UserCarInputData data = cache.getUserCarData(chatId);
            deleteMessage(data.getLastMessageToDelete());
            data.setLastMessageToDelete(sendOutMessage);
            data.setPreLastMessageToDelete(message);
            cache.saveUserCarData(chatId, data);
            return;
        }
        UserCarInputData data = cache.getUserCarData(chatId);
        int priceInCurrency = carDto.getRawCarPrice() * 10_000;
        data.setPrice(priceInCurrency);
        data.setPriceInEuro(executionService.convertMoneyToEuro(priceInCurrency, data.getCurrency()));
        data.setVolume(carDto.getRawCarPower());
        data.setAge(executionService.calculateCarAgeByRawDate(carDto.getRawCarYear(), carDto.getRawCarMonth()));
        data.setCarId(carDto.getCarId());
        if (carDto.isHasInsuranceInfo()) {
            data.setOtherAccidentCost(carDto.getOtherAccidentCost());
            data.setMyAccidentCost(carDto.getMyAccidentCost());
            data.setHasInsuranceInfo(carDto.isHasInsuranceInfo());
        }

        // Удаляем сообщения
        deleteMessage(data.getLastMessageToDelete());
        deleteMessage(message);

        processExecuteResultAndShowHeader(data, chatId);
    }

    /**
     * Расчитываем стоимость по ссылке che168.com для манагеров.
     *
     * @param link
     */
    private void processCalculateByCheCarLinkForManagers(Message message, String link) {
        long chatId = message.getChatId();
        String carId;
        CarDto carDto;
        try {
            carId = utilService.parseLinkToCarId(link);
            carDto = cheCarCacheService.fetchAndUpdateCheCarDtoByCarId(carId);
        } catch (GetCarDetailException | RecaptchaException e) {
            String errorMessage = """
                    Ошибка получения данных с сайта che168.com
                                        
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
            executeMessage(utilMessageService.prepareSendMessage(chatId, errorMessage, inlineKeyboardMarkup));
            return;
        }
        UserCarInputData data = cache.getUserCarData(chatId);

        int priceInCurrency = carDto.getRawCarPrice();
        data.setPrice(priceInCurrency);
        data.setCarId(carDto.getCarId());
        Map<String, Province> managerLogisticsProvinceMap = googleService.getManagerLogisticsProvinceMap();
        data.setProvince(managerLogisticsProvinceMap.get(carDto.getRawCarProvinceName()));
        processExecuteResultForChinaManagers(data, chatId);
    }

    /**
     * Рассчитать для манагеров стоимость тачек с доставкой без таможни по исходным данным.
     *
     * @param data
     * @param chatId
     */
    private void processExecuteResultForChinaManagers(UserCarInputData data, long chatId) {
        restService.refreshExchangeRates();
        CarTotalResultData resultData = executionService.executeCarTotalResultData(data);
        log.info("""
                        Данные рассчёта:
                        price in CNY {},
                        provinceName {},
                        carId {}
                        """, resultData.getCnyPrice(),
                resultData.getProvince().getProvinceFullName(), resultData.getCarId());
        String textUssuriysk =
                utilMessageService.getKorexManagerCnyMessageToUssuriyskByResultData(resultData);
        String textBishkek =
                utilMessageService.getKorexManagerCnyMessageToBishkekByResultData(resultData);
        executeMessage(utilMessageService.prepareSendMessage(chatId, textUssuriysk));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton resetManagerButton = new InlineKeyboardButton(RESET_MANAGER_BUTTON);
        resetManagerButton.setCallbackData(RESET_MANAGER_CALLBACK);
        row.add(resetManagerButton);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);

        executeMessage(utilMessageService.prepareSendMessage(chatId, textBishkek, inlineKeyboardMarkup));
    }


    /**
     * Расчитываем стоимость по ссылке che168.com
     *
     * @param link
     */
    private void processCalculateByCheCarLink(Message message, String link) {
        long chatId = message.getChatId();
        String carId;
        CarDto carDto;
        try {
            carId = utilService.parseLinkToCarId(link);
            carDto = cheCarCacheService.fetchAndUpdateCheCarDtoByCarId(carId);
        } catch (GetCarDetailException | RecaptchaException e) {
            String errorMessage = """
                    Ошибка получения данных с сайта che168.com
                                        
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
            Message sendOutMessage = executeMessage(utilMessageService.prepareSendMessage(chatId, errorMessage, inlineKeyboardMarkup));

            // запомнить сообщение для удаления
            UserCarInputData data = cache.getUserCarData(chatId);
            deleteMessage(data.getLastMessageToDelete());
            data.setLastMessageToDelete(sendOutMessage);
            data.setPreLastMessageToDelete(message);
            cache.saveUserCarData(chatId, data);
            return;
        }
        UserCarInputData data = cache.getUserCarData(chatId);

        int priceInCurrency = carDto.getRawCarPrice();
        data.setPrice(priceInCurrency);
        data.setPriceInEuro(executionService.convertMoneyToEuro(priceInCurrency, data.getCurrency()));
        data.setVolume(carDto.getRawCarPower());
        data.setAge(executionService.calculateCarAgeByRawDate(carDto.getRawCarYear(), carDto.getRawCarMonth()));
        data.setCarId(carDto.getCarId());
        if (configDataPool.isKorexProvinceMatrix) {
            data.setProvince(ConfigDataPool.provincePriceMapForKorex.get(carDto.getRawCarProvinceName()));
        } else {
            data.setProvince(ConfigDataPool.provincePriceMap.get(carDto.getRawCarProvinceName()));
        }
        // Удаляем сообщения
        deleteMessage(data.getLastMessageToDelete());
        deleteMessage(message);

        processExecuteResultAndShowHeader(data, chatId);
    }

    /**
     * Процесс обработки одной валюты - single valute Mode
     *
     * @param name
     */
    private void processSingleCurrencyStart(Message message, String name) {
        long chatId = message.getChatId();
        String text = String.format("""
                Здравствуйте, %s! 
                """, name);
        executeMessage(utilMessageService.prepareSendMessage(chatId, text));

        applyCurrencyAndDefineCalculateMode(message, configDataPool.singleCurrency());
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
        executeMessage(utilMessageService.prepareSendMessage(chatId, "Команда не распознана, чтобы начать - нажмите /start"));
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

    private Message executeMessage(SendMessage message) {
        try {
            return execute(message);
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

    private boolean isChannelSubscriber(long userId) {
        try {
            GetChatMember request = new GetChatMember(configDataPool.getChannelSubscribersId(), userId);
            var response = execute(request);
            return "member".equals(response.getStatus());
        } catch (TelegramApiException e) {
            return false;
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
        executeMessage(utilMessageService.prepareSendMessage(chatId, startMessage));
        List<Long> subscriptionIds = subscribeService.getSubscribers();

        // добавили кнопки в сообщение рассылки
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton reset = new InlineKeyboardButton(RESET_MESSAGE);
        InlineKeyboardButton manager = new InlineKeyboardButton(MANAGER_MESSAGE);
        reset.setCallbackData(RESET_CALLBACK);
        manager.setUrl(managerLink);
        row1.add(manager);
        row2.add(reset);
        rows.add(row1);
        rows.add(row2);
        if (configDataPool.isEnableClientRequest()) {
            InlineKeyboardButton cliendRequest = new InlineKeyboardButton(CLIENT_REQUEST_BUTTON);
            List<InlineKeyboardButton> row3 = new ArrayList<>();
            cliendRequest.setCallbackData(CLIENT_REQUEST_BUTTON);
            row3.add(cliendRequest);
            rows.add(row3);
        }
        inlineKeyboardMarkup.setKeyboard(rows);

        subscriptionIds.forEach(id -> {
            log.info("Начинаем отправку сообщение пользователю с id {}", id);
            try {
                if (Objects.nonNull(subscribeService.getPhotoData())) {
                    executeMessage(utilMessageService.prepareSendMessage(id, subscribeService.getPhotoData(), subscribeService.getMailingText(), inlineKeyboardMarkup));
                } else {
                    executeMessage(utilMessageService.prepareSendMessage(id, subscribeService.getMailingText(), inlineKeyboardMarkup));
                }
                Thread.sleep(200);
                log.info("Сообщение пользователю отправлено пользователю с id {}", id);

            } catch (InterruptedException e) {
                log.error("поток прерван");
            } catch (RuntimeException e) {
                log.error("Ошибка отправки сообщения пользователю с id {} , {}", id, e.getMessage());
            }
        });
        String finishMessage = """
                Рассылка успешно завершена.
                Теперь можно вернуться в меню рассылок /mail 
                    """;

        executeMessage(utilMessageService.prepareSendMessage(chatId, finishMessage));
        subscribeService.cleanData();
    }

    private void deleteMessage(Message message) {
        if (Objects.isNull(message)) {
            log.warn("Отсутствует сообщение для удаления");
            return;
        }
        try {
            execute(utilMessageService.prepareDeleteMessageByChatIdAndMessageId(message.getMessageId(), message.getChatId()));
        } catch (TelegramApiException e) {
            log.warn("Отсутствует сообщение для удаления");
        }
    }
}
