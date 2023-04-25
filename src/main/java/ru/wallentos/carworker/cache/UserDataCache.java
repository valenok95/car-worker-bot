package ru.wallentos.carworker.cache;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.wallentos.carworker.model.BotState;
import ru.wallentos.carworker.model.UserCarData;


@Component
@Slf4j
public class UserDataCache implements DataCache {
    private static Map<Long, BotState> usersBotStates = new HashMap<>();
    private static Map<Long, UserCarData> usersProfileData = new HashMap<>();

    @Override
    public void setUsersCurrentBotState(long userId, BotState botState) {
        usersBotStates.put(userId, botState);
        log.info("пользователь с id " + userId + " переведен в состояние " + botState.name());
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
    public UserCarData getUserCarData(long userId) {
        UserCarData userCarData = usersProfileData.get(userId);
        if (userCarData == null) {
            userCarData = new UserCarData();
        }
        return userCarData;
    }

    @Override
    public void saveUserCarData(long userId, UserCarData userCarData) {
        usersProfileData.put(userId, userCarData);
    }
    public static void resetUserData(long userId){
        usersBotStates.remove(userId);
        usersProfileData.remove(userId);
    }
}