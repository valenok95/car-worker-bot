package ru.wallentos.carworker.service;

import static ru.wallentos.carworker.configuration.ConfigDataPool.CNY;
import static ru.wallentos.carworker.configuration.ConfigDataPool.KRW;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.wallentos.carworker.model.CarDto;
import ru.wallentos.carworker.model.CarPriceResultData;
import ru.wallentos.carworker.model.CarTotalResultData;
import ru.wallentos.carworker.model.DeliveryPrice;

@Service
@Slf4j
public class UtilMessageService {
    @Autowired
    private RestService restService;
    @Autowired
    private UtilService utilService;


    protected SendMessage prepareSendMessage(long chatId, String text, InlineKeyboardMarkup
            inlineKeyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setParseMode(ParseMode.HTML);
        message.setReplyMarkup(inlineKeyboardMarkup);
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    protected SendMessage prepareSendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setParseMode(ParseMode.HTML);
        message.setText(text);
        return message;
    }

    protected SendPhoto prepareSendMessage(long chatId, String file, String text) {
        SendPhoto message = SendPhoto.builder()
                .chatId(chatId)
                .caption(text)
                .photo(new InputFile(file))
                .build();
        return message;
    }

    protected SendPhoto prepareSendMessage(long chatId, String file, String text, InlineKeyboardMarkup
            inlineKeyboardMarkup) {
        SendPhoto message = SendPhoto.builder()
                .chatId(chatId)
                .caption(text)
                .photo(new InputFile(file))
                .replyMarkup(inlineKeyboardMarkup)
                .build();
        return message;
    }

    /**
     * Удалить сообщение по messageId и chatId
     */
    protected DeleteMessage prepareDeleteMessageByChatIdAndMessageId(int messageId, long chatId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        return deleteMessage;

    }


    /**
     * Главное сообщение по результатам расчёта.
     *
     * @param botName
     * @param currency
     * @param resultData
     * @return
     */
    protected String getResultHeaderMessageByBotNameAndCurrency(String botName, String currency, CarPriceResultData resultData) {
        if (botName.equals("KorexCalcBot")) {
            if (Objects.equals(currency, CNY)) {
                return getKorexCnyMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getKorexKrwMessageByResultData(resultData);
            }
        } else if (botName.equals("EastWayCalcBot")) { // 
            // определять сообщение для каждого botName + default
            if (Objects.equals(currency, CNY)) {
                return getEastWayCnyMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getEastWayKrwMessageByResultData(resultData);
            }
        } else if (botName.equals("AutoDillerBot")) {
            if (Objects.equals(currency, CNY)) {
                return getRostovCnyMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getRostovKrwMessageByResultData(resultData);
            }
        } else if (botName.equals("KorexManagerBot") || botName.equals("carworkerbot")) {
            if (Objects.equals(currency, CNY)) {
                return getKorexManagerCnyMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getKorexManagerKrwMessageByResultData(resultData);
            }
        } else if (botName.equals("DemoCarBot")) {
            if (Objects.equals(currency, CNY)) {
                return getKorexDemoCnyMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getKorexDemoKrwMessageByResultData(resultData);
            }
        }
        return String.format("""
                %s
                        
                Что бы заказать авто - вы можете обратиться к менеджеру🔻
                        """, resultData);
    }


    private String getKorexKrwMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                        %s
                                                
                        Стоимость "под ключ" включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️.
                                                
                        Актуальные курсы валют вы можете посмотреть в Меню.
                                                
                        По вопросам заказа авто вы можете обратиться к нашему менеджеру @KOREXKOREA.
                                                
                        <a href="https://t.me/korex_official">🔗Официальный телеграмм канал</a>
                        <a href="https://youtu.be/PGWUzjEbV1k?si=Nc3aLb2JE7hQ-UeW">🔗Видео инструкция по сайту Encar</a>
                        """,
                resultData.getResultPrice(),
                utilService.getEncarLinkStringByCarId(resultData.getCarId()));
    }

    private String getKorexCnyMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                        %s
                        Стоимость "под ключ" включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Китае, логистику, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину.
                                                
                        Актуальные курсы валют вы можете посмотреть в Меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @KOREXKOREA.
                                                
                        <a href="https://t.me/korex_official">🔗Официальный телеграмм канал</a>
                        <a href="https://www.youtube.com/@KOREX_OFFICIAL/featured">🔗Официальный YouTube канал</a>
                        """,
                resultData.getResultPrice(),
                utilService.getCheCarLinkStringByCarId(resultData.getCarId()));
    }

    private String getEastWayCnyMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                        %s             
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽               
                                                
                        Комиссия компании: 50 000 ₽
                        %s 
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Китае, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @EastWayAdmin.
                                                
                        <a href="https://t.me/EastWayOfficial">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountValutePart(),
                utilService.getProvinceStringByProvinceNameAndPrice(resultData.getProvinceName(),
                        resultData.getProvincePriceInRubles()),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                utilService.getCheCarLinkStringByCarId(resultData.getCarId()));
    }

    private String getEastWayKrwMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽ 
                                                
                        Комиссия компании: 50 000 ₽
                        %s               
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @EastWayAdmin.
                                                
                        <a href="https://t.me/EastWayOfficial">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountValutePart(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                utilService.getEncarLinkStringByCarId(resultData.getCarId()));
    }


    private String getRostovCnyMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ в Новочеркасске:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Уссурийска:
                        %,.0f ₽
                        %s             
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽     
                                                
                        Доставка до Новочеркасска: %,.0f ₽             
                                                
                        Комиссия компании: 50 000 ₽
                        %s 
                        Итоговая стоимость включает в себя все расходы до г. Новочеркасск, а именно: оформление экспорта в Китае, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @Roman_autodiler.
                                                
                        <a href="https://t.me/autodiler61">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountValutePart(),
                utilService.getProvinceStringByProvinceNameAndPrice(resultData.getProvinceName(),
                        resultData.getProvincePriceInRubles()),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                resultData.getExtraPayAmountRublePart(),
                utilService.getCheCarLinkStringByCarId(resultData.getCarId()));
    }

    private String getRostovKrwMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ в Новочеркасске:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽ 
                                                
                        Доставка до Новочеркасска: %,.0f ₽     
                                                
                        Комиссия компании: 50 000 ₽
                        %s               
                        Итоговая стоимость включает в себя все расходы до г. Новочеркасск, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @Roman_autodiler.
                                                
                        <a href="https://t.me/autodiler61">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountValutePart(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                resultData.getExtraPayAmountRublePart(),
                utilService.getEncarLinkStringByCarId(resultData.getCarId()));
    }


    private String getKorexManagerCnyMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                        %s
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Китае, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountValutePart(),
                utilService.getProvinceStringByProvinceNameAndPrice(resultData.getProvinceName(),
                        resultData.getProvincePriceInRubles()),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                utilService.getCheCarLinkStringByCarId(resultData.getCarId()));
    }

    /**
     * Сообщение с результатом до Уссурийска с учётом провинции.
     *
     * @param resultData
     * @return
     */
    public String getKorexManagerCnyMessageToUssuriyskByResultData(CarTotalResultData resultData
            , Map<String, DeliveryPrice> managerLogisticsMap) {
        return String.format(Locale.FRANCE, """
                        Ссылка на авто: %s
                              
                        Провинция: %s
                              
                        Total to Ussuriysk CNY: <u><b>%,.0f</b></u>
                        Total to Ussuriysk USD: <u><b>%,.0f</b></u>
                        """,
                String.format("https://www.che168.com/dealer/416034/%d.html", resultData.getCarId()),
                resultData.getProvinceName(),
                resultData.getCnyPrice() + managerLogisticsMap.get(resultData.getProvinceName()).getUssuriyskDeliveryPrice() + 15000,
                (resultData.getCnyPrice() + managerLogisticsMap.get(resultData.getProvinceName()).getUssuriyskDeliveryPrice() + 15000) / restService.getManagerCnyUsdRate());
    }

    /**
     * Сообщение с результатом до Бишкека с учётом провинции.
     *
     * @param resultData
     * @return
     */
    public String getKorexManagerCnyMessageToBishkekByResultData(CarTotalResultData resultData
            , Map<String, DeliveryPrice> managerLogisticsMap) {
        return String.format(Locale.FRANCE, """
                        Ссылка на авто: %s
                              
                        Провинция: %s
                              
                        Total to Bishkek CNY: <u><b>%,.0f</b></u>
                        Total to Bishkek USD: <u><b>%,.0f</b></u>          
                        """,
                String.format("https://www.che168.com/dealer/416034/%d.html", resultData.getCarId()),
                resultData.getProvinceName(),
                resultData.getCnyPrice() + managerLogisticsMap.get(resultData.getProvinceName()).getBishkekDeliveryPrice() + 16000,
                (resultData.getCnyPrice() + managerLogisticsMap.get(resultData.getProvinceName()).getBishkekDeliveryPrice() + 16000) / restService.getManagerCnyUsdRate());
    }

    private String getKorexManagerKrwMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountValutePart(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                utilService.getEncarLinkStringByCarId(resultData.getCarId()));
    }

    private String getKorexDemoCnyMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                        %s
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Китае, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину
                             
                        <u><b>По вопросу сотрудничества</b></u>
                        Telegram / WhatsApp
                        +82 10-9926-0978 Сергей Шек
                             
                        @param botName
                        @param currency
                        @param resultData
                        @return
                        @Korexkorea """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountValutePart(),
                utilService.getProvinceStringByProvinceNameAndPrice(resultData.getProvinceName(),
                        resultData.getProvincePriceInRubles()),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                utilService.getCheCarLinkStringByCarId(resultData.getCarId()));
    }

    private String getKorexDemoKrwMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину
                                                
                        <u><b>По вопросу сотрудничества</b></u>
                        Telegram / WhatsApp
                        +82 10-9926-0978 Сергей Шек
                        @Korexkorea""",
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles() + resultData.getExtraPayAmountValutePart(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                utilService.getEncarLinkStringByCarId(resultData.getCarId()));
    }

    /**
     * Сообщение с деталями расчёта.
     **/
    protected String getResultDetailMessageByBotNameAndCurrency(String botName, String currency,
                                                                CarPriceResultData resultData) {
        if (botName.equals("KorexCalcBot") || botName.equals("carworkerbot")) {
            if (Objects.equals(currency, CNY)) {
                return getKorexCnyDetailMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getKorexKrwDetailMessageByResultData(resultData);
            }
        } else if (botName.equals("EastWayCalcBot")) { //
            // определять сообщение для каждого botName + default
            if (Objects.equals(currency, CNY)) {
                return getEastWayCnyDetailMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getEastWayKrwDetailMessageByResultData(resultData);
            }
        } else if (botName.equals("AutoDillerBot")) {
            if (Objects.equals(currency, CNY)) {
                return getRostovCnyDetailMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getRostovKrwDetailMessageByResultData(resultData);
            }
        } else if (botName.equals("KorexManagerBot")) {
            if (Objects.equals(currency, CNY)) {
                return getKorexManagerCnyDetailMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getKorexManagerKrwDetailMessageByResultData(resultData);
            }
        } else if (botName.equals("DemoCarBot")) {
            if (Objects.equals(currency, CNY)) {
                return getKorexDemoCnyDetailMessageByResultData(resultData);
            } else if (Objects.equals(currency, KRW)) {
                return getKorexDemoKrwDetailMessageByResultData(resultData);
            }
        }
        return String.format("""
                %s
                                
                Что бы заказать авто - вы можете обратиться к менеджеру🔻
                """, resultData);
    }

    private String getKorexKrwDetailMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля в Корее:
                        %,.0f ₽
                                                
                        Стоимость оформления экспортных документов и доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        %,.0f ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                        """,
                resultData.getFirstPriceInRubles(),
                resultData.getExtraPayAmountValutePart(),
                resultData.getExtraPayAmountRublePart(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee());
    }

    private String getKorexCnyDetailMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля в Китае:
                        %,.0f ₽
                        %s
                                                
                        Стоимость оформления экспортных документов и доставки из Суйфыньхэ до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        %,.0f ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                        """,
                resultData.getFirstPriceInRubles(),
                utilService.getProvinceStringByProvinceNameAndPrice(resultData.getProvinceName(),
                        resultData.getProvincePriceInRubles()),
                resultData.getExtraPayAmountValutePart(),
                resultData.getExtraPayAmountRublePart(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee());
    }

    private String getEastWayCnyDetailMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                        %s
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                                                
                        Комиссия компании: 50 000 ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Китае, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @EastWayAdmin.
                             
                        <a href="https://t.me/EastWayOfficial">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles(),
                utilService.getProvinceStringByProvinceNameAndPrice(resultData.getProvinceName(),
                        resultData.getProvincePriceInRubles()),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                utilService.getCheCarLinkStringByCarId(resultData.getCarId()));
    }

    private String getEastWayKrwDetailMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Detail Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                                                
                        Комиссия компании: 50 000 ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @EastWayAdmin.
                             
                        <a href="https://t.me/EastWayOfficial">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                utilService.getEncarLinkStringByCarId(resultData.getCarId()));
    }

    private String getRostovCnyDetailMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ в Новочеркасске:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Уссурийска:
                        %,.0f ₽
                        %s
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                                                
                        Доставка до Новочеркасска: %,.0f ₽
                                                
                        Комиссия компании: 50 000 ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Новочеркасск, а именно: оформление экспорта в Китае, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @Roman_autodiler.
                             
                        <a href="https://t.me/autodiler61">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles(),
                utilService.getProvinceStringByProvinceNameAndPrice(resultData.getProvinceName(),
                        resultData.getProvincePriceInRubles()),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                resultData.getExtraPayAmountRublePart(),
                utilService.getCheCarLinkStringByCarId(resultData.getCarId()));
    }

    private String getRostovKrwDetailMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ в Новочеркасске:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                                                
                        Доставка до Новочеркасска: %,.0f ₽
                                                
                        Комиссия компании: 50 000 ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Новочеркасск, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину️
                                                
                        Актуальный курс оплаты наличными и курсы ЦБ вы можете найти в меню.
                                                
                        По вопросам проведения платежа и заказа авто вы можете обратиться к нашему менеджеру @Roman_autodiler.
                             
                        <a href="https://t.me/autodiler61">🔗Официальный телеграмм канал</a>
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                resultData.getExtraPayAmountRublePart(),
                utilService.getEncarLinkStringByCarId(resultData.getCarId()));
    }

    private String getKorexManagerCnyDetailMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                        %s
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Китае, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles(),
                utilService.getProvinceStringByProvinceNameAndPrice(resultData.getProvinceName(),
                        resultData.getProvincePriceInRubles()),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                utilService.getCheCarLinkStringByCarId(resultData.getCarId()));
    }

    private String getKorexManagerKrwDetailMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину
                        """,
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                utilService.getEncarLinkStringByCarId(resultData.getCarId()));
    }

    private String getKorexDemoCnyDetailMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                        %s
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Китае, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину
                             
                        <u><b>По вопросу сотрудничества</b></u>
                        Telegram / WhatsApp
                        +82 10-9926-0978 Сергей Шек
                             
                        @param carId
                        @return
                        @Korexkorea""",
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles(),
                utilService.getProvinceStringByProvinceNameAndPrice(resultData.getProvinceName(),
                        resultData.getProvincePriceInRubles()),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                utilService.getCheCarLinkStringByCarId(resultData.getCarId()));
    }

    private String getKorexDemoKrwDetailMessageByResultData(CarPriceResultData resultData) {
        return String.format(Locale.FRANCE, """
                        Стоимость автомобиля под ключ во Владивостоке:
                        <u><b>%,.0f ₽</b></u>
                                                
                        Стоимость автомобиля с учетом доставки до Владивостока:
                        %,.0f ₽
                                                
                        Брокерские расходы, СВХ, СБКТС:
                        100 000 ₽
                                                
                        Таможенная пошлина и утилизационный сбор: %,.0f ₽
                        %s
                        Итоговая стоимость включает в себя все расходы до г. Владивосток, а именно: оформление экспорта в Корее, фрахт, услуги брокера, склады временного хранения, прохождение лаборатории для получения СБКТС и таможенную пошлину
                             
                        <u><b>По вопросу сотрудничества</b></u>
                        Telegram / WhatsApp
                        +82 10-9926-0978 Сергей Шек
                        @Korexkorea""",
                resultData.getResultPrice(),
                resultData.getFirstPriceInRubles(),
                resultData.getFeeRate() + resultData.getDuty() + resultData.getRecyclingFee(),
                utilService.getEncarLinkStringByCarId(resultData.getCarId()));
    }

    public String getEncarReportMessage(CarDto carDto) {
        return String.format(Locale.FRANCE, """
                        Страховые выплаты по представленному автомобилю:
                        %,d ₩
                                                
                        Cтраховые выплаты другим участникам ДТП:
                        %,d ₩
                                                
                        %s
                        """,
                carDto.getMyAccidentCost(), carDto.getOtherAccidentCost(),
                utilService.getEncarInspectLinkStringByCarId(carDto.getCarId()));
    }

    protected String getAuctionKrwResultMessage(double resultKrwPrice) {
        return String.format(Locale.FRANCE, """
                Ваша ставка на аукционе %,.0f KRW
                """, resultKrwPrice);
    }
}
