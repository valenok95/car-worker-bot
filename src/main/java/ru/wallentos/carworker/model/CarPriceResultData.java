package ru.wallentos.carworker.model;

import java.util.Locale;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CarPriceResultData {
    int carCategory;
    double feeRate;
    double duty;
    double firstPriceInRubles;
    int recyclingFee;
    double extraPayAmount;
    /**
     * Рынок ввоза.
     */
    String stock;
    String age;
    String location;
    String sanctionMessage = "Санкционный авто, ";

    public double getResultPrice() {
        return firstPriceInRubles + feeRate + duty + recyclingFee + extraPayAmount;
    }

    @Override
    public String toString() {
        return String.format(Locale.FRANCE, """
                        Итого: <b>%,.0f руб.</b>
                                                
                        Стоимость автомобиля с учетом доставки и оформления:
                        %,.0fруб.
                                                
                        Таможенная пошлина и утилизационный сбор:
                        %,.0fруб.
                                           
                        ❗️Итоговая стоимость указана за автомобиль %s и включает все расходы, в том числе процедуру таможенной очистки.❗""", 
                getResultPrice(), firstPriceInRubles + extraPayAmount,
                feeRate + duty + recyclingFee, location);
    }
}
