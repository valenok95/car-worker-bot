package ru.wallentos.carworker.service;

import static ru.wallentos.carworker.configuration.ConfigDataPool.CNY;
import static ru.wallentos.carworker.configuration.ConfigDataPool.USD;
import static ru.wallentos.carworker.configuration.ConfigDataPool.conversionRatesMap;
import static ru.wallentos.carworker.configuration.ConfigDataPool.manualConversionRatesMapInRubles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.wallentos.carworker.configuration.ConfigDataPool;
import ru.wallentos.carworker.exceptions.GetCarDetailException;
import ru.wallentos.carworker.exceptions.RecaptchaException;
import ru.wallentos.carworker.model.CarConverter;
import ru.wallentos.carworker.model.CarDto;
import ru.wallentos.carworker.model.CarEntity;

@Service
@Data
@Slf4j
public class RestService {
    @Value("${ru.wallentos.carworker.exchange-manager-coefficient:1}")
    public double managerCoefficient;
    @Value("${ru.wallentos.carworker.usd-krw-correction-rate:40}")
    public double usdKrwCorrectionRate;
    @Value("${ru.wallentos.carworker.exchange-api.host-cbr}")
    private String cbrMethod;
    @Value("${ru.wallentos.carworker.exchange-api.host-naver}")
    private String naverMethod;
    @Value("${ru.wallentos.carworker.exchange-api.host-profinance}")
    private String profinanceMethod;
    @Value("${ru.wallentos.carworker.exchange-api.fem-encar-detail-url}")
    private String femEncarDetailUrl;
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
    @Value("${ru.wallentos.carworker.exchange-api.ajax}")
    private boolean isAjax;
    private RestTemplate restTemplate;
    private UtilService utilService;
    private ObjectMapper mapper;
    private CarConverter carConverter;
    private RedisTemplate redisTemplate;
    private ConfigDataPool configDataPool;
    private double cbrUsdKrwMinusCorrection;
    private RecaptchaService recaptchaService;

    @Autowired
    public RestService(RestTemplate restTemplate, UtilService utilService, CarConverter carConverter, RedisTemplate redisTemplate, RecaptchaService recaptchaService, ConfigDataPool configDataPool) {
        this.restTemplate = restTemplate;
        this.utilService = utilService;
        this.carConverter = carConverter;
        this.redisTemplate = redisTemplate;
        this.recaptchaService = recaptchaService;
        this.configDataPool = configDataPool;
        mapper = new ObjectMapper();
    }

    /**
     * Обновляем курс для расчёта таможни.
     */
    public void refreshDutyExchangeRates() {
        ResponseEntity<String> response = restTemplate.getForEntity(cbrMethod, String.class);
        conversionRatesMap = utilService.backRatesToConversionRatesMap(response.getBody());
        
        log.info("курс ЦБ обновлён {}", conversionRatesMap);
        cbrUsdKrwMinusCorrection = Double.parseDouble(getNaverRate()) - usdKrwCorrectionRate;
        log.info("Курс ЦБ USD/KRW минус коррекция ({}) обновлён: {}", usdKrwCorrectionRate,
                cbrUsdKrwMinusCorrection);
    }

    /**
     * Инициализируем ручной курс для расчёта автомобилей.
     */
    public void initManualExchangeRates() {
        conversionRatesMap.forEach((key, value) -> ConfigDataPool.manualConversionRatesMapInRubles.put(key, conversionRatesMap.get("RUB") * configDataPool.coefficient / value));
        // курс расчёта доллара к рублю получаем отдельно в profinance.ru
        Double profinanceUsdRubRate = getUsdRubProfinanceRate();
        if (Objects.nonNull(profinanceUsdRubRate)) {
            ConfigDataPool.manualConversionRatesMapInRubles.put(USD,
                    getUsdRubProfinanceRate() * configDataPool.coefficient);
            log.info("курс расчёта для доллара установлен из источника profinance {} умноженный " +
                            "на КФ настройки {}",
                    profinanceUsdRubRate, configDataPool.coefficient);
        } else {
            log.error("НЕ удалось получить курс PROFINANCE, установлен курс ЦБ");
        }
        log.info("курс расчёта обновлён {}", manualConversionRatesMapInRubles);
    }

    /**
     * Получить курс доллара из источника profinance.ru
     *
     * @return курс USD/RUB из profinance.ru
     */
    private Double getUsdRubProfinanceRate() {
        try {
            Connection tokenConnection = Jsoup.connect(profinanceMethod).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
            String token = tokenConnection.execute().body();
            Connection rateConnection = Jsoup.connect(profinanceMethod).userAgent("Mozilla/5.0 (X11; Linux x86_64) " + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36").requestBody(String.format("1;SID=%s;A=;I=29;S=USD/RUB;\n", token));
            String rawRateString =
                    rateConnection.post().body().childNodes().get(0).toString().substring(30, 40);
            String rateString = utilService.parseProfinanceResponseToPositiveRate(rawRateString);
            return Double.parseDouble(rateString);
        } catch (IOException e) {
            return null;
        }
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
        var connection = Jsoup.connect(femEncarDetailUrl + carId).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
        connection.execute();
        connection.execute();
        Document document = Jsoup.parse(connection.execute().body());
        String valueJson = document.select("script:containsData(PRELOADED_STATE)").get(0).childNodes().get(0).toString().replace("__PRELOADED_STATE__ = ", "");
        return mapper.readTree(valueJson);
    }


    /**
     * Получить информацию по автомобилю encar
     *
     * @param carId идентификатор авто
     * @return данные со страницы ответа в json
     * @throws IOException
     */
    public JsonNode getEncarDetailJsonDataByRestTemplate(String carId) {
        return restTemplate.getForEntity(encarVehicleUrl + carId, JsonNode.class).getBody();
    }

    /**
     * Получить информацию по автомобилю encar AJAX
     *
     * @param carId идентификатор авто
     * @return данные со страницы ответа в json
     * @throws IOException
     */
    private JsonNode getEncarDetailJsonDataByJsoupAjax(String carId) throws IOException {
        var connection = Jsoup.newSession().url(encarDetailUrl).headers(Map.of("X-Requested-With", "XMLHttpRequest", "Accept", "application/json")).data(Map.of("method", "ajaxInspectView", "sdFlag", "N", "rgsid", carId)).userAgent("Mozilla/5.0 (X11; " + "Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103" + ".0.0.0 Safari/537.36");
        return mapper.readTree(connection.execute().body()).get(0).get("inspect").get("carSaleDto");
    }

    /**
     * Получить страховую информацию по автомобилю encar
     *
     * @param carId идентификатор авто
     * @return данные со страницы ответа в json
     * @throws IOException
     */
    private JsonNode getEncarInsuranceJsonDataByJsoup(String carId) throws IOException {
        var getVehicleNumConnection = Jsoup.connect(encarVehicleUrl + carId).ignoreContentType(true).execute().body();
        var jsonVehicleNumData = mapper.readTree(getVehicleNumConnection);
        int vehicleId = jsonVehicleNumData.get("vehicleId").asInt();
        String vehicleNo = jsonVehicleNumData.get("vehicleNo").asText();

        var connection = Jsoup.connect(String.format(encarInsuranceUrl, vehicleId)).data("vehicleNo", vehicleNo).header("content-type", "application/json;charset=UTF-8").userAgent("Mozilla/5" + ".0 (X11; Linux x86_64) " + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36").ignoreContentType(true);
        return mapper.readTree(connection.execute().body());
    }

    public CarDto getEncarDataByApi(String carId) throws GetCarDetailException, RecaptchaException {
        try {
            JsonNode jsonDetail = getEncarDetailJsonDataByRestTemplate(carId);
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


            var status = jsonDetail.get("advertisement").get("status").asText();
            if ("SOLD".equals(status) || "WAIT".equals(status)) {
                String errorMessage = String.format("The car %s has been sold", carId);
                throw new GetCarDetailException(errorMessage);
            } else if (!"ADVERTISE".equals(status)) {
                log.warn("unhandled car status {} by carId {}", status, carId);
            }
            var encarEntity = new CarEntity(carId, jsonDetail.get("advertisement").get("price").asText(), jsonDetail.get("category").get("yearMonth").asText().substring(0, 4), jsonDetail.get("category").get("yearMonth").asText().substring(4, 6), jsonDetail.get("spec").get("displacement").asText(), null, myAccidentCost, otherAccidentCost, hasInsuranceInfo);
            return carConverter.convertToDto(encarEntity);
        } catch (NullPointerException e) {
            String errorMessage = String.format("Error while getting info by id %s", carId);
            throw new GetCarDetailException(errorMessage);
        }
    }

    public CarDto getEncarDataByJsoupAjax(String carId) throws GetCarDetailException {
        try {
            JsonNode jsonDetail = getEncarDetailJsonDataByJsoupAjax(carId);
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

            var encarEntity = new CarEntity(carId, jsonDetail.get("price").asText(), jsonDetail.get("year").asText().substring(0, 4), jsonDetail.get("year").asText().substring(4, 6), jsonDetail.get("displacement").asText(), null, myAccidentCost, otherAccidentCost, hasInsuranceInfo);
            return carConverter.convertToDto(encarEntity);
        } catch (IOException | NullPointerException e) {
            String errorMessage = String.format("Error while getting info by id %s", carId);
            throw new GetCarDetailException(errorMessage);
        }
    }

    /**
     * в зависимости от настройки либо используем новый метод ajax либо старый, который сломался
     * из-за каптчи.
     *
     * @param carId
     * @return
     * @throws GetCarDetailException
     * @throws RecaptchaException
     */
    public CarDto getEncarData(String carId) throws GetCarDetailException, RecaptchaException {
        if (isAjax) {
            return getEncarDataByJsoupAjax(carId);
        } else {
            return getEncarDataByApi(carId);
        }
    }


    /**
     * Курс доллара по китаю для манагеров.
     *
     * @return
     */
    public double getManagerCnyUsdRate() {
        return managerCoefficient * (conversionRatesMap.get(CNY) / conversionRatesMap.get(USD));
    }

    public CarDto getCheDataByJsoup(String carId) throws GetCarDetailException, RecaptchaException {
        Document startDocument = null;
        try {
            var startConnection = Jsoup.connect(cheStartMethod).data("infoid", carId).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
            startDocument = Jsoup.parse(startConnection.execute().body());
            String specId = startDocument.getElementById("CarSpecid").val(); // Поиск деталей по specId
            String rawCarPriceString = startDocument.getElementById("car_price").val(); //
            int rawCarPrice = (int) (Double.parseDouble(rawCarPriceString) * 100 * 100);
            // Поиск деталей
            String rawCarYear = startDocument.getElementsByClass("auxiliary").get(0).childNodes().get(0).toString().replace("\n", "").substring(0, 4); // Поиск деталей
            String rawCarMonth = startDocument.getElementsByClass("auxiliary").get(0).childNodes().get(0).toString().replace("\n", "").substring(5, 7); // Поиск деталей
            String rawCarProvinceName = startDocument.getElementsByClass("auxiliary").get(0).childNodes().get(6).toString();

            var detailConnection = Jsoup.connect(cheDetailMethod).data("specid", specId).ignoreContentType(true).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
            String detailString = Jsoup.parse(detailConnection.execute().body()).text().replace("\n", "");
            String rawCarPower = utilService.parseCheCarPower(detailString);

// 排量(mL)
            var cheCarEntity = new CarEntity(carId, String.valueOf(rawCarPrice), rawCarYear, rawCarMonth, rawCarPower, rawCarProvinceName, 0, 0, false);
            return carConverter.convertToDto(cheCarEntity);


        } catch (IOException | NullPointerException e) {
            String errorMessage = String.format("Error while getting info by id %s", carId);
            throw new GetCarDetailException(errorMessage);
        }
    }
}

