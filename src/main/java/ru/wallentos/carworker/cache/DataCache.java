package ru.wallentos.carworker.cache;

import ru.wallentos.carworker.model.BotState;
import ru.wallentos.carworker.model.UserCarData;

public interface DataCache {
    void setUsersCurrentBotState(long userId, BotState botState);

    BotState getUsersCurrentBotState(long userId);

    UserCarData getUserCarData(long userId);

    void saveUserCarData(long userId, UserCarData userCarData);
}