package ru.wallentos.carworker.model;

/**
 * Возможные состояния бота
 */

public enum BotState {
    SET_CURRENCY_MENU,
    SET_CURRENCY,
    ASK_CURRENCY,
    ASK_PRICE,
    ASK_ISSUE_DATE,
    ASK_VOLUME,
    DATA_PREPARED
}