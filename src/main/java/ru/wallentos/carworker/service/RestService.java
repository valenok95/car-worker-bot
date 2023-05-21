package ru.wallentos.carworker.service;

import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Data
public class RestService {
    @Value("${ru.wallentos.carworker.exchange-api.host-cbr}")
    private String cbrMethod;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UtilService utilService;
    private Map<String, Double> conversionRatesMap;


    public void refreshExchangeRates() {
        // 126866786 айди Миши
        ResponseEntity<String> response
                = restTemplate.getForEntity(cbrMethod, String.class);
        conversionRatesMap = utilService.backRatesToConversionRatesMap(response.getBody());
        System.out.println("курс обновлён" + conversionRatesMap);

    }
}

