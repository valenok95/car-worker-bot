package ru.wallentos.carworker.cache;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.wallentos.carworker.model.BotState;
import ru.wallentos.carworker.model.UserCarInputData;


@Component
@Slf4j
public class UserDataCache implements DataCache {
    private static Map<Long, BotState> usersBotStates = new HashMap<>();
    private static Map<Long, UserCarInputData> usersProfileData = new HashMap<>();

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
    public BotState getUsersCurrentBotState(long userId) {
        BotState botState = usersBotStates.get(userId);
        if (botState == null) {
            botState = BotState.ASK_CONCURRENCY;
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
    public static void resetUserData(long userId){
        usersBotStates.remove(userId);
        usersProfileData.remove(userId);
    }
}