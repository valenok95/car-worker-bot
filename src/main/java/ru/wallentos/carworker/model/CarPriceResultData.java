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
    double extraPayAmountInCurrency;
    double extraPayAmountInRubles;
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
    public String getDisableChinaMessage() {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        %,.0f ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽ 
                                                
                        ‼️Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину‼️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто обратитесь к нашему менеджеру @KorexAdmin.
                        
                        <a href="https://t.me/korexautotradeofficial">🔗Официальный телеграмм канал</a>
                        <a href="https://t.me/korexautotradeofficial/705">🔗Видео инструкция по сайту Encar</a>
                        """, 
                getResultPrice(), firstPriceInRubles + extraPayAmountInCurrency,
                extraPayAmountInRubles,
                feeRate + duty + recyclingFee);
    }
}
