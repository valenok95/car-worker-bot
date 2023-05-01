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

    public double getResultPrice() {
        return firstPriceInRubles + feeRate + duty + recyclingFee + extraPayAmount;
    }

    @Override
    public String toString() {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля с учетом доставки и оформления:\s
                        %,.0fруб.
                                                
                        Таможенная пошлина и утилизационный сбор:
                        %,.0fруб.
                                                
                        Итого: %,.0fруб.
                                                
                        ❗️Итоговая стоимость указана за автомобиль %s, и включает все расходы, в том числе процедуру таможенной очистки.❗""", firstPriceInRubles + extraPayAmount,
                feeRate + duty + recyclingFee, getResultPrice(), location);
    }

    public void setFirstPriceInRubles(double firstPriceInRubles, double coefficient) {
        this.firstPriceInRubles = firstPriceInRubles * coefficient;
    }
}
