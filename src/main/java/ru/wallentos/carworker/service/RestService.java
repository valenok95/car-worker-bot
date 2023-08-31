package ru.wallentos.carworker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.wallentos.carworker.exceptions.GetCarDetailException;
import ru.wallentos.carworker.exceptions.RecaptchaException;
import ru.wallentos.carworker.model.EncarConverter;
import ru.wallentos.carworker.model.CarDto;
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
    @Value("${ru.wallentos.carworker.exchange-api.host-che}")
    private String cheMethod;
    private RestTemplate restTemplate;
    private UtilService utilService;
    private ObjectMapper mapper;
    private EncarConverter encarConverter;
    private RedisTemplate redisTemplate;
    private Map<String, Double> conversionRatesMap;
    private double cbrUsdKrwMinus20;
    private RecaptchaService recaptchaService;

    @Autowired
    public RestService(RestTemplate restTemplate, UtilService utilService,
                       EncarConverter encarConverter, RedisTemplate redisTemplate,
                       RecaptchaService recaptchaService) {
        this.restTemplate = restTemplate;
        this.utilService = utilService;
        this.encarConverter = encarConverter;
        this.redisTemplate = redisTemplate;
        this.recaptchaService = recaptchaService;
        mapper = new ObjectMapper();
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

    public CarDto getEncarDataByJsoup(String carId) throws GetCarDetailException, RecaptchaException {
        Document document = null;
        try {
            var connection = Jsoup.connect(encarMethod + carId).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
            connection.execute();
            connection.execute();
            document = Jsoup.parse(connection.execute().body());
            String valueJson =
                    document.select("script:containsData(PRELOADED_STATE)").get(0).childNodes().get(0).toString().replace("__PRELOADED_STATE__ = ", "");
            var json = mapper.readTree(valueJson);
            if (document.toString().contains("recaptcha")) {
                String errorMessage = "Требуется решение каптчи.";
                log.warn(errorMessage);
                throw new RecaptchaException(errorMessage);
            }
            var encarEntity = new EncarEntity(
                    carId,
                    json.get("cars").get("base").get("advertisement").get("price").asText(),
                    json.get("cars").get("base").get("category").get("formYear").asText(),
                    json.get("cars").get("base").get("category").get("yearMonth").asText().substring(4, 6),
                    json.get("cars").get("base").get("spec").get("displacement").asText());
            return encarConverter.convertToDto(encarEntity);
        } catch (RecaptchaException e) {
            recaptchaService.solveReCaptcha(encarMethod + carId, document);
            throw e;
        } catch (IOException | NullPointerException e) {
            String errorMessage = String.format("Error while getting info by id %s",
                    carId);
            throw new GetCarDetailException(errorMessage);
        }
    }

    public CarDto getCheDataByJsoup(String carId) throws GetCarDetailException, RecaptchaException {
        Document document = null;
        try {
            var connection = Jsoup.connect(cheMethod + carId + "html").userAgent("Mozilla/5.0 " +
                    "(X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
            //connection.execute();
            document = Jsoup.parse(connection.execute().body());
            String valueJson =
                    document.select("script:containsData(PRELOADED_STATE)").get(0).childNodes().get(0).toString().replace("__PRELOADED_STATE__ = ", "");
            var json = mapper.readTree(valueJson);
            if (document.toString().contains("recaptcha")) {
                String errorMessage = "Требуется решение каптчи.";
                log.warn(errorMessage);
                throw new RecaptchaException(errorMessage);
            }
            var encarEntity = new EncarEntity(
                    carId,
                    json.get("cars").get("base").get("advertisement").get("price").asText(),
                    json.get("cars").get("base").get("category").get("formYear").asText(),
                    json.get("cars").get("base").get("category").get("yearMonth").asText().substring(4, 6),
                    json.get("cars").get("base").get("spec").get("displacement").asText());
            return encarConverter.convertToDto(encarEntity);
        } catch (RecaptchaException e) {
            recaptchaService.solveReCaptcha(encarMethod + carId, document);
            throw e;
        } catch (IOException | NullPointerException e) {
            String errorMessage = String.format("Error while getting info by id %s",
                    carId);
            throw new GetCarDetailException(errorMessage);
        }
    }
}

