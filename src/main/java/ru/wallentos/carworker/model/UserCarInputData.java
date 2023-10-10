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
    String currency;
    int price;
    double userAuctionStartPrice;
    double userAuctionResultPrice;
    double priceInEuro;
    LocalDate issueDate;
    int volume;
    String age;
    String stock;
    Province province;
    Message lastMessageToDelete;
    Message preLastMessageToDelete;

    @Override
    public String toString() {
        return String.format(Locale.FRANCE, """
                Возраст: %s.
                Стоимость: %d %s\s
                Объем двигателя: %d cc""", age, price, currency, volume);


    }
}