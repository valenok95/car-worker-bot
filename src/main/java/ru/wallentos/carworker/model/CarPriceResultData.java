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
    double extraPayAmountValutePart;
    double extraPayAmountRublePart;
    double provincePriceInRubles;
    String provinceName;
    int carId;
    /**
     * Рынок ввоза.
     */
    String stock;
    String age;
    String location;
    String sanctionMessage = "Санкционный авто, ";
    String currency;

    public double getResultPrice() {
        return firstPriceInRubles + feeRate + duty + recyclingFee + extraPayAmountValutePart + extraPayAmountRublePart + provincePriceInRubles;
    }


    @Override
    public String toString() {
        return String.format(Locale.FRANCE, """
                        Итого: <b>%,.0f руб.</b>
                                                
                        Стоимость автомобиля с учетом доставки и оформления:
                        %,.0fруб.
                                                
                        Таможенная пошлина и утилизационный сбор:
                        %,.0fруб.
                                           
                        Итоговая стоимость указана за автомобиль %s и включает все расходы, в том числе процедуру таможенной очистки.""",
                getResultPrice(), firstPriceInRubles + extraPayAmountValutePart,
                feeRate + duty + recyclingFee, location);
    }


}
