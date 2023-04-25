package ru.wallentos.carworker.service;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Data
public class RestService {
    @Value("${ru.wallentos.carworker.exchange-api.method}")
    private String method;
    @Autowired
    private WebClient webClient;
    private Map<String, Double> conversionRatesMap = new HashMap<>();


    public void refreshExchangeRates() {
        conversionRatesMap =
                (Map<String, Double>) webClient.get()
                        .uri(method)
                        .retrieve()
                        .toEntity(HashMap.class)
                        .block().getBody().get("conversion_rates");
    }

    // CNYEUR 

}
// 1. выбор валюты
// 2. ввод стоимости автомобиля
// 3. ввод даты выпуска авто - подсчёт возраста авто
// 1. ввод объема двигателя КУБ.СМ
// 1
// . рассчитываем стоимость в евро

