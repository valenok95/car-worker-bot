package ru.wallentos.carworker.service;

import com.fasterxml.jackson.databind.JsonNode;
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
import ru.wallentos.carworker.model.CarConverter;
import ru.wallentos.carworker.model.CarDto;
import ru.wallentos.carworker.model.CarEntity;

@Service
@Data
@Slf4j
public class RestService {
    @Value("${ru.wallentos.carworker.exchange-api.host-cbr}")
    private String cbrMethod;
    @Value("${ru.wallentos.carworker.exchange-api.host-naver}")
    private String naverMethod;
    @Value("${ru.wallentos.carworker.exchange-api.encar-detail-url}")
    private String encarDetailUrl;
    @Value("${ru.wallentos.carworker.exchange-api.encar-vehicle-url}")
    private String encarVehicleUrl;
    @Value("${ru.wallentos.carworker.exchange-api.encar-insurance-url}")
    private String encarInsuranceUrl;
    @Value("${ru.wallentos.carworker.exchange-api.host-che-start}")
    private String cheStartMethod;
    @Value("${ru.wallentos.carworker.exchange-api.host-che-detail}")
    private String cheDetailMethod;
    private RestTemplate restTemplate;
    private UtilService utilService;
    private ObjectMapper mapper;
    private CarConverter carConverter;
    private RedisTemplate redisTemplate;
    private Map<String, Double> conversionRatesMap;
    private double cbrUsdKrwMinus20;
    private RecaptchaService recaptchaService;

    @Autowired
    public RestService(RestTemplate restTemplate, UtilService utilService,
                       CarConverter carConverter, RedisTemplate redisTemplate,
                       RecaptchaService recaptchaService) {
        this.restTemplate = restTemplate;
        this.utilService = utilService;
        this.carConverter = carConverter;
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

    /**
     * Получить информацию по автомобилю encar
     *
     * @param carId идентификатор авто
     * @return данные со страницы ответа в json
     * @throws IOException
     */
    private JsonNode getEncarDetailJsonDataByJsoup(String carId) throws IOException {
        var connection = Jsoup.connect(encarDetailUrl + carId).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
        connection.execute();
        connection.execute();
        Document document = Jsoup.parse(connection.execute().body());
        String valueJson =
                document.select("script:containsData(PRELOADED_STATE)").get(0).childNodes().get(0).toString().replace("__PRELOADED_STATE__ = ", "");
        return mapper.readTree(valueJson);
    }

    /**
     * Получить страховую информацию по автомобилю encar
     *
     * @param carId идентификатор авто
     * @return данные со страницы ответа в json
     * @throws IOException
     */
    private JsonNode getEncarInsuranceJsonDataByJsoup(String carId) throws IOException {
        var getVehicleNumConnection =
                Jsoup.connect(encarVehicleUrl + carId).ignoreContentType(true).execute().body();
        var jsonVehicleNumData = mapper.readTree(getVehicleNumConnection);
        int vehicleId = jsonVehicleNumData.get("vehicleId").asInt();
        String vehicleNo = jsonVehicleNumData.get("vehicleNo").asText();

        var connection = Jsoup.connect(String.format(encarInsuranceUrl, vehicleId))
                .data("vehicleNo", vehicleNo)
                .header("content-type", "application/json;charset=UTF-8").userAgent("Mozilla/5" +
                        ".0 (X11; Linux x86_64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36").ignoreContentType(true);
        return mapper.readTree(connection.execute().body());
    }

    public CarDto getEncarDataByJsoup(String carId) throws GetCarDetailException, RecaptchaException {
        try {
            JsonNode jsonDetail = getEncarDetailJsonDataByJsoup(carId);
            int myAccidentCost = 0;
            int otherAccidentCost = 0;
            boolean hasInsuranceInfo = false;
            try {
                JsonNode jsonInsurance = getEncarInsuranceJsonDataByJsoup(carId);
                myAccidentCost = jsonInsurance.get("myAccidentCost").asInt();
                otherAccidentCost = jsonInsurance.get("otherAccidentCost").asInt();
                hasInsuranceInfo = true;

            } catch (IOException e) {
                log.warn("cannot get insurance information by carId {}", carId);
            }


            var status = jsonDetail.get("cars").get("base").get("advertisement").get("status").asText();
            if ("SOLD".equals(status) || "WAIT".equals(status)) {
                String errorMessage = String.format("The car %s has been sold", carId);
                throw new GetCarDetailException(errorMessage);
            } else if (!"ADVERTISE".equals(status)) {
                log.warn("unhandled car status {} by carId {}", status, carId);
            }
            var encarEntity = new CarEntity(
                    carId,
                    jsonDetail.get("cars").get("base").get("advertisement").get("price").asText(),
                    jsonDetail.get("cars").get("base").get("category").get("formYear").asText(),
                    jsonDetail.get("cars").get("base").get("category").get("yearMonth").asText().substring(4, 6),
                    jsonDetail.get("cars").get("base").get("spec").get("displacement").asText(),
                    null, myAccidentCost, otherAccidentCost, hasInsuranceInfo);
            return carConverter.convertToDto(encarEntity);
        } catch (IOException | NullPointerException e) {
            String errorMessage = String.format("Error while getting info by id %s",
                    carId);
            throw new GetCarDetailException(errorMessage);
        }
    }

    public CarDto getCheDataByJsoup(String carId) throws GetCarDetailException, RecaptchaException {
        Document startDocument = null;
        try {
            var startConnection = Jsoup.connect(cheStartMethod).data("infoid", carId).userAgent(
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
            startDocument = Jsoup.parse(startConnection.execute().body());
            String specId = startDocument.getElementById("CarSpecid").val(); // Поиск деталей по specId
            String rawCarPriceString = startDocument.getElementById("car_price").val(); //
            int rawCarPrice = (int) (Double.parseDouble(rawCarPriceString) * 100 * 100);
            // Поиск деталей
            String rawCarYear =
                    startDocument.getElementsByClass("auxiliary").get(0).childNodes().get(0).toString().replace("\n", "").substring(0, 4); // Поиск деталей
            String rawCarMonth =
                    startDocument.getElementsByClass("auxiliary").get(0).childNodes().get(0).toString().replace("\n", "").substring(5, 7); // Поиск деталей
            String rawCarProvinceName =
                    startDocument.getElementsByClass("auxiliary").get(0).childNodes().get(6).toString();

            var detailConnection =
                    Jsoup.connect(cheDetailMethod)
                            .data("specid", specId)
                            .ignoreContentType(true)
                            .userAgent(
                                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
            String detailString = Jsoup.parse(detailConnection.execute().body()).text().replace(
                    "\n", "");
            String rawCarPower = utilService.parseCheCarPower(detailString);

// 排量(mL)
            var cheCarEntity = new CarEntity(
                    carId,
                    String.valueOf(rawCarPrice),
                    rawCarYear, rawCarMonth, rawCarPower, rawCarProvinceName, 0, 0, false
            );
            return carConverter.convertToDto(cheCarEntity);


        } catch (IOException | NullPointerException e) {
            String errorMessage = String.format("Error while getting info by id %s",
                    carId);
            throw new GetCarDetailException(errorMessage);
        }
    }
}

