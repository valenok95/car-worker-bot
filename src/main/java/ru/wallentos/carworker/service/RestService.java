package ru.wallentos.carworker.service;

import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Data
@Slf4j
public class RestService {
    @Value("${ru.wallentos.carworker.exchange-api.host-cbr}")
    private String cbrMethod;
    private RestTemplate restTemplate;

    private UtilService utilService;
    private Map<String, Double> conversionRatesMap;
    private double cbrUsdKrwMinus20;

    @Autowired
    public RestService(RestTemplate restTemplate, UtilService utilService) {
        this.restTemplate = restTemplate;
        this.utilService = utilService;
    }

    public void refreshExchangeRates() {
        ResponseEntity<String> response
                = restTemplate.getForEntity(cbrMethod, String.class);
        conversionRatesMap = utilService.backRatesToConversionRatesMap(response.getBody());
        log.info("курс обновлён {}", conversionRatesMap);
        cbrUsdKrwMinus20 = (conversionRatesMap.get("KRW") / conversionRatesMap.get("USD")) - 20;
    }
}

