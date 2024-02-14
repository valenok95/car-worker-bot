package ru.wallentos.carworker.model;

import java.time.LocalDate;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.objects.Message;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCarInputData {
    int carId;
    boolean isSanctionCar;
    boolean isElectric;
    String currency;
    int price;
    double userAuctionStartPrice;
    double userAuctionResultPrice;
    double priceInEuro;
    LocalDate issueDate;
    int volume;
    int power;
    String age;
    String stock;
    Province province;
    Message lastMessageToDelete;
    Message preLastMessageToDelete;
    int myAccidentCost;
    int otherAccidentCost;
    boolean hasInsuranceInfo;
    String userContact;
    String clientMessage;

    @Override
    public String toString() {
        if (isElectric()) {
            return String.format(Locale.FRANCE, """
                    Тип: Электромобиль.
                    Возраст: %s.
                    Стоимость: %d %s\s
                    Мощность: %d л.с""", age, price, currency, power);
        }
        return String.format(Locale.FRANCE, """
                Возраст: %s.
                Стоимость: %d %s\s
                Объем двигателя: %d cc""", age, price, currency, volume);
    }
}