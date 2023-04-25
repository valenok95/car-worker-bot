package ru.wallentos.carworker.model;

/**
 * Возможные состояния бота
 */

public enum BotState {
    ASK_CONCURRENCY,
    ASK_PRICE,
    ASK_ISSUE_DATE,
    ASK_VOLUME,
    DATA_PREPARED
}