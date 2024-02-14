package ru.wallentos.carworker.service;

import static ru.wallentos.carworker.configuration.ConfigDataPool.CNY;
import static ru.wallentos.carworker.configuration.ConfigDataPool.USD;
import static ru.wallentos.carworker.configuration.ConfigDataPool.conversionRatesMap;
import static ru.wallentos.carworker.configuration.ConfigDataPool.manualConversionRatesMapInRubles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    @Value("${ru.wallentos.carworker.exchange-api.host-cbr}")
    private String cbrMethod;
    @Value("${ru.wallentos.carworker.exchange-api.host-naver}")
    private String naverMethod;
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
    private double cbrUsdKrwMinus20;
    private RecaptchaService recaptchaService;
    private GoogleService googleService;
    private static List<String> ELECTRIC_FUEL_TYPE_LIST = List.of("수소", "전기");

    @Autowired
    public RestService(RestTemplate restTemplate, UtilService utilService,
                       CarConverter carConverter, RedisTemplate redisTemplate,
                       RecaptchaService recaptchaService, ConfigDataPool configDataPool,
                       GoogleService googleService) {
        this.restTemplate = restTemplate;
        this.utilService = utilService;
        this.carConverter = carConverter;
        this.redisTemplate = redisTemplate;
        this.recaptchaService = recaptchaService;
        this.configDataPool = configDataPool;
        this.googleService = googleService;
        mapper = new ObjectMapper();
    }

    public void refreshExchangeRates() {
        ResponseEntity<String> response
                = restTemplate.getForEntity(cbrMethod, String.class);
        conversionRatesMap =
                utilService.backRatesToConversionRatesMap(response.getBody());
        if (configDataPool.isCbrRateToCalculate()) {
            conversionRatesMap.forEach((key, value) -> {
                ConfigDataPool.manualConversionRatesMapInRubles.put(key, conversionRatesMap.get("RUB") * configDataPool.coefficient / value);
            });
            log.info("курс расчёта обновлён {}", manualConversionRatesMapInRubles);
        }
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
        var connection = Jsoup.connect(femEncarDetailUrl + carId).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
        connection.execute();
        connection.execute();
        Document document = Jsoup.parse(connection.execute().body());
        String valueJson =
                document.select("script:containsData(PRELOADED_STATE)").get(0).childNodes().get(0).toString().replace("__PRELOADED_STATE__ = ", "");
        return mapper.readTree(valueJson);
    }

    /**
     * Получить информацию по автомобилю encar AJAX
     *
     * @param carId идентификатор авто
     * @return данные со страницы ответа в json
     * @throws IOException
     */
    private JsonNode getEncarDetailJsonDataByJsoupAjax(String carId) throws IOException {
        var connection = Jsoup.newSession().url(encarDetailUrl).headers(Map.of("X-Requested-With",
                        "XMLHttpRequest", "Accept", "application/json")).data(Map.of("method",
                        "ajaxInspectView", "sdFlag", "N", "rgsid",
                        carId))
                .userAgent("Mozilla/5.0 (X11; " +
                        "Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103" +
                        ".0.0.0 Safari/537.36");
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
    @Deprecated

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
                    jsonDetail.get("cars").get("base").get("category").get("yearMonth").asText().substring(0, 4),
                    jsonDetail.get("cars").get("base").get("category").get("yearMonth").asText().substring(4, 6),
                    jsonDetail.get("cars").get("base").get("spec").get("displacement").asText(),
                    null,
                    null, myAccidentCost, otherAccidentCost, hasInsuranceInfo, false);
            return carConverter.convertToDto(encarEntity);
        } catch (IOException | NullPointerException e) {
            String errorMessage = String.format("Error while getting info by id %s",
                    carId);
            throw new GetCarDetailException(errorMessage);
        }
    }

    /**
     * Текущий актуальный метод получения encar data
     * @param carId
     * @return
     * @throws GetCarDetailException
     */
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
            boolean isElectric = false;
            String carVolume = jsonDetail.get("displacement").asText();
            String carPrice = jsonDetail.get("price").asText();
            String carYear = jsonDetail.get("year").asText().substring(0, 4);
            String carMonth = jsonDetail.get("year").asText().substring(4, 6);
            String carName = jsonDetail.get("modelNm").asText() + jsonDetail.get("badgeNm").asText();
            String fuelName = jsonDetail.get("fuelNm").asText();
            String carPower = "0";
            log.info("""
                    Получаем данные с encar:
                    car id {}
                    car Volume {}
                    car Price: {}
                    car Year: {}
                    car Month: {}
                    car Name: {}
                    car fuel name: {}
                    """, carId, carVolume, carPrice, carYear, carMonth, carName, fuelName);
            if (ELECTRIC_FUEL_TYPE_LIST.contains(fuelName)) {
                carPower = googleService.getCarPowerByCarName(carName);
                isElectric = true;
                log.info("Авто определено как электро, мощность авто из справочника {} л.с", carPower);
            }

            CarEntity encarEntity = new CarEntity(
                    carId,
                    carPrice,
                    carYear,
                    carMonth,
                    carVolume, carPower, null,
                    myAccidentCost, otherAccidentCost, hasInsuranceInfo, isElectric);
            return carConverter.convertToDto(encarEntity);
        } catch (IOException | NullPointerException e) {
            String errorMessage = String.format("Error while getting info by id %s",
                    carId);
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
            return getEncarDataByJsoup(carId);
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

    /**
     * Получить данные по китайскому авто.
     *
     * @param carId
     * @return
     * @throws GetCarDetailException
     * @throws RecaptchaException
     */
    public CarDto getCheDataByJsoup(String carId) throws GetCarDetailException, RecaptchaException {
        Document startDocument;
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
            String rawCarVolume = utilService.parseCheCarVolume(detailString);
            String rawCarPower = null;

            boolean isElectric = false;
            if (Objects.isNull(rawCarVolume)) {
                rawCarPower = utilService.parseCheCarPower(detailString);
                isElectric = true;
            }
// 排量(mL) - объём двигателя
// 电动机(Ps) - лошадиные силы
            var cheCarEntity = new CarEntity(
                    carId,
                    String.valueOf(rawCarPrice),
                    rawCarYear, rawCarMonth, rawCarVolume, rawCarPower, rawCarProvinceName, 0, 0,
                    false, isElectric);
            return carConverter.convertToDto(cheCarEntity);


        } catch (IOException | NullPointerException e) {
            String errorMessage = String.format("Error while getting info by id %s",
                    carId);
            throw new GetCarDetailException(errorMessage);
        }
    }
}

