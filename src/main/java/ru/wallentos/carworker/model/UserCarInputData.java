package ru.wallentos.carworker.model;

import java.time.LocalDate;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCarInputData {
    int carId;
    boolean isSanctionCar;
    String currency;
    int price;
    double priceInEuro;
    LocalDate issueDate;
    int volume;
    String age;
    String stock;

    @Override
    public String toString() {
        return String.format(Locale.FRANCE, """
                Возраст: %s.
                Стоимость: %d %s\s
                Объем двигателя: %d cc""", age, price, currency, volume);


    }
}