package ru.wallentos.carworker.model;

/**
 * Возможные состояния бота
 */

public enum BotState {
    SET_CURRENCY_MENU,
    MAILING_MENU,
    SET_CURRENCY,
    ASK_CURRENCY,
    ASK_PRICE,
    ASK_ISSUE_DATE,
    ASK_VOLUME,
    ASK_CALCULATION_MODE,
    WAITING_FOR_LINK,
    DATA_PREPARED,
    ASK_CLIENT_REQUEST_MESSAGE,
    ASK_CLIENT_CONTACT,
    ASK_CHINA_LINK
}