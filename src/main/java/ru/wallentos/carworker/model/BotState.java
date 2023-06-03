package ru.wallentos.carworker.model;

/**
 * Возможные состояния бота
 */

public enum BotState {
    SET_CONCURRENCY_MENU,
    SET_CONCURRENCY,
    ASK_CONCURRENCY,
    ASK_PRICE,
    ASK_ISSUE_DATE,
    ASK_VOLUME,
    DATA_PREPARED
}