package ru.wallentos.carworker.service;

import java.io.IOException;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.wallentos.carworker.exceptions.GetCarDetailException;
import ru.wallentos.carworker.model.EncarConverter;
import ru.wallentos.carworker.model.EncarDto;
import ru.wallentos.carworker.model.EncarEntity;

@Service
@Data
@Slf4j
public class RestService {
    @Value("${ru.wallentos.carworker.exchange-api.host-cbr}")
    private String cbrMethod;
    @Value("${ru.wallentos.carworker.exchange-api.host-naver}")
    private String naverMethod;
    @Value("${ru.wallentos.carworker.exchange-api.host-encar}")
    private String encarMethod;
    private RestTemplate restTemplate;
    private String cookie = "someCookie";

    private UtilService utilService;
    private EncarConverter encarConverter;
    private RedisTemplate redisTemplate;
    private Map<String, Double> conversionRatesMap;
    private double cbrUsdKrwMinus20;

    @Autowired
    public RestService(RestTemplate restTemplate, UtilService utilService,
                       EncarConverter encarConverter, RedisTemplate redisTemplate) {
        this.restTemplate = restTemplate;
        this.utilService = utilService;
        this.encarConverter = encarConverter;
        this.redisTemplate = redisTemplate;
    }

    public void refreshExchangeRates() {
        ResponseEntity<String> response
                = restTemplate.getForEntity(cbrMethod, String.class);
        conversionRatesMap = utilService.backRatesToConversionRatesMap(response.getBody());
        log.info("курс обновлён {}", conversionRatesMap);
        cbrUsdKrwMinus20 = Double.parseDouble(getNaverRate()) - 20;
    }

    public String getNaverRate() {
        try {
            var document = Jsoup.connect(naverMethod).get();
            return document.getElementById("select_from").childNodes().get(3).attributes().asList().get(0).getValue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public EncarDto getEncarDataByJsoup(String carId) throws GetCarDetailException {
        try {
            var connection = Jsoup.connect(encarMethod + carId).userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/115.0");
            connection.execute();
            connection.execute();
            var document = Jsoup.parse(connection.execute().body());
            var encarEntity = new EncarEntity(
                    carId,
                    document.select("meta[name=WT.z_price]").attr("content"),
                    document.select("meta[name=WT.z_year]").attr("content"),
                    document.select("meta[name=WT.z_month]").attr("content"),
                    document.select("input[name=dsp]").attr("value"));
            log.info("response received {}", document);

            EncarDto dto = encarConverter.convertToDto(encarEntity);
            if (dto.getRawCarPrice() == 0) {
                String errorMessage = String.format("Error while getting info by id %s from " +
                        "document %s ", carId, document);
                log.error(errorMessage + dto);
                throw new GetCarDetailException(errorMessage);
            }
            return dto;
        } catch (IOException e) {
            String errorMessage = String.format("Error while getting info by id %s", carId);
            throw new GetCarDetailException(errorMessage);
        }
    }
}

