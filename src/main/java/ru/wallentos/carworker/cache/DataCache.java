package ru.wallentos.carworker.cache;

import ru.wallentos.carworker.model.BotState;
import ru.wallentos.carworker.model.CarPriceResultData;
import ru.wallentos.carworker.model.UserCarInputData;

public interface DataCache {
    void setUsersCurrentBotState(long userId, BotState botState);

    BotState getUsersCurrentBotState(long userId);

    UserCarInputData getUserCarData(long userId);

    void saveUserCarData(long userId, UserCarInputData userCarInputData);
    void deleteUserCarDataByUserId(long userId);

    void saveResultCarData(long userId, CarPriceResultData carPriceResultData);
    void deleteResultCarDataByUserId(long userId);

    CarPriceResultData getResultCarData(long userId);
    
}