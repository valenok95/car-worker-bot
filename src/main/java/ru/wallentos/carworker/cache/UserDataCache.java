package ru.wallentos.carworker.cache;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.wallentos.carworker.model.BotState;
import ru.wallentos.carworker.model.CarPriceResultData;
import ru.wallentos.carworker.model.UserCarInputData;


@Component
@Slf4j
public class UserDataCache implements DataCache {
    private static Map<Long, BotState> usersBotStates = new HashMap<>();
    private static Map<Long, UserCarInputData> usersProfileData = new HashMap<>();
    private static Map<Long, CarPriceResultData> usersCarPriceData = new HashMap<>();

    @Override
    public void setUsersCurrentBotState(long userId, BotState botState) {
        usersBotStates.put(userId, botState);
        log.info("пользователь с id " + userId + " переведен в состояние " + botState.name());
    }

    @Override
    public void deleteUserCarDataByUserId(long userId) {
        usersBotStates.remove(userId);
        usersProfileData.remove(userId);
        log.info("удалены данные по пользователю с id " + userId);
    }

    @Override
    public void saveResultCarData(long userId, CarPriceResultData carPriceResultData) {
        usersCarPriceData.put(userId, carPriceResultData);
    }

    @Override
    public void deleteResultCarDataByUserId(long userId) {
        usersCarPriceData.remove(userId);
    }

    @Override
    public CarPriceResultData getResultCarData(long userId) {
        CarPriceResultData carPriceResultData = usersCarPriceData.get(userId);
        if (carPriceResultData == null) {
            carPriceResultData = new CarPriceResultData();
        }
        return carPriceResultData;
    }

    @Override
    public BotState getUsersCurrentBotState(long userId) {
        BotState botState = usersBotStates.get(userId);
        if (botState == null) {
            botState = BotState.ASK_CURRENCY_MANUAL_MODE;
        }

        return botState;
    }

    @Override
    public UserCarInputData getUserCarData(long userId) {
        UserCarInputData userCarInputData = usersProfileData.get(userId);
        if (userCarInputData == null) {
            userCarInputData = new UserCarInputData();
        }
        return userCarInputData;
    }

    @Override
    public void saveUserCarData(long userId, UserCarInputData userCarInputData) {
        usersProfileData.put(userId, userCarInputData);
    }

    public static void resetUserData(long userId) {
        usersBotStates.remove(userId);
        usersProfileData.remove(userId);
    }
}