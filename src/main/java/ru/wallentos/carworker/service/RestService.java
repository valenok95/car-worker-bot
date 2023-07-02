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
            var connection = Jsoup.connect(encarMethod + carId).userAgent("Mozilla/5.0 (X11; " +
                    "Ubuntu; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/114.0");
            connection = connection.cookie("WMONID", cookie);
            var document = Jsoup.parse(connection.execute().body());
            //  .cookies(request.cookies())
            var encarEntity = new EncarEntity(
                    carId,
                    document.select("meta[name=WT.z_price]").attr("content"),
                    document.select("meta[name=WT.z_year]").attr("content"),
                    document.select("meta[name=WT.z_month]").attr("content"),
                    document.select("input[name=dsp]").attr("value"));
            log.info("response received");
            EncarDto dto = encarConverter.convertToDto(encarEntity);
            //    for(int i=0;i<500;i++){
            //        redisTemplate.opsForValue().set(carId+i,dto);
            //        Thread.sleep(100);
            //    }
            return dto;
        } catch (IOException e) {
            String errorMessage = String.format("Ошибка при получении авто по id %s", carId);
            log.error(errorMessage);
            throw new GetCarDetailException(errorMessage);
        }
    }

    public void updateCookie() {
        try {
            var connection = Jsoup.connect(encarMethod + "34556293").userAgent("Mozilla/5.0 (X11; " +
                    "Ubuntu; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/114.0");
            cookie = connection.execute().cookie("WMONID");
            log.info("response received");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
/*
    public EncarResponseDto getEncarDataByRest(String carId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/114.0");
        if (enableCookie) {
            log.info("start to get cookie");
            var entity = restTemplate.getForEntity(encarMethod + carId, String.class);
            log.info("cookie received");

            headers.add("Cookie", entity.getHeaders().get("Set-cookie").get(0));

        } else {
            headers.add("Cookie", "WMONID=GTiOzw_URtl;");
        }
        String htmlBody = restTemplate.exchange(encarMethod + carId, HttpMethod.GET,
                new HttpEntity<String>(headers), String.class).getBody();
        var document = Jsoup.parse(htmlBody);

        //  .cookies(request.cookies())
        log.info("response received");

        return new EncarResponseDto(
                carId,
                document.select("meta[name=WT.z_price]").attr("content"),
                document.select("meta[name=WT.z_year]").attr("content"),
                document.select("meta[name=WT.z_month]").attr("content"),
                document.select("input[name=dsp]").attr("value"));
    }*/
}

