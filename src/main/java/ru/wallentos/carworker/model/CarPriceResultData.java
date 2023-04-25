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
                        Стоимость автомобиля с учетом доставки и оформления: : %,.0fруб.
                        Таможенная пошлина и утилизационный сбор : %,.0fруб.
                        Итого окончательная стоимость авто %s: %n%,.0fруб.""", firstPriceInRubles + extraPayAmount,
                feeRate + duty + recyclingFee, location, getResultPrice());
    }

    public void setFirstPriceInRubles(double firstPriceInRubles, double coefficient) {
        this.firstPriceInRubles = firstPriceInRubles * coefficient;
    }
}
