package ru.wallentos.carworker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.wallentos.carworker.cache.EncarCache;
import ru.wallentos.carworker.exceptions.GetCarDetailException;
import ru.wallentos.carworker.exceptions.RecaptchaException;
import ru.wallentos.carworker.service.CheCarCacheService;
import ru.wallentos.carworker.service.EncarCacheService;
import ru.wallentos.carworker.service.ExecutionService;
import ru.wallentos.carworker.service.GoogleService;
import ru.wallentos.carworker.service.RestService;
import ru.wallentos.carworker.service.SubscribeService;
import ru.wallentos.carworker.service.UtilService;

@RestController
public class WebController {
    private final RestService restService;
    private final ExecutionService executionService;
    private final EncarCache encarCache;
    private final EncarCacheService encarCacheService;
    private final CheCarCacheService cheCarCacheService;
    private final UtilService utilService;
    private final SubscribeService subscribeService;
    private final GoogleService googleService;

    @Autowired
    public WebController(RestService restService, EncarCache encarCache,
                         EncarCacheService encarCacheService,
                         CheCarCacheService cheCarCacheService, UtilService utilService,
                         SubscribeService subscribeService,
                         GoogleService googleService, ExecutionService executionService) {
        this.restService = restService;
        this.encarCache = encarCache;
        this.encarCacheService = encarCacheService;
        this.cheCarCacheService = cheCarCacheService;
        this.utilService = utilService;
        this.subscribeService = subscribeService;
        this.googleService = googleService;
        this.executionService = executionService;
    }


    @GetMapping("/googleTest")
    public ResponseEntity<?> testG() {
        googleService.getManagerLogisticsProvinceMap();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/getEncarByIdJsoup")
    public ResponseEntity<?> getCarByIdJsoup(@RequestParam String carId) throws GetCarDetailException, RecaptchaException {
        return ResponseEntity.accepted().body(restService.getEncarDataByApi(carId));
    }

    @GetMapping("/getEncarByIdRestTemplate")
    public ResponseEntity<?> getCarByIdRestTemplate(@RequestParam String carId) throws GetCarDetailException, RecaptchaException {
        return ResponseEntity.accepted().body(restService.getEncarDetailJsonDataByRestTemplate(carId));
    }

    @GetMapping("/getEncarByIdJsoupAjax")
    public ResponseEntity<?> getCarByIdJsoupAjax(@RequestParam String carId) throws GetCarDetailException, RecaptchaException {
        return ResponseEntity.accepted().body(restService.getEncarDataByJsoupAjax(carId));
    }

    @GetMapping("/getCheByIdJsoup")
    public ResponseEntity<?> getCheCarByIdJsoup(@RequestParam String carId) throws GetCarDetailException, RecaptchaException {
        return ResponseEntity.accepted().body(restService.getCheDataByJsoup(carId));
    }

    @GetMapping("/getEncarByIdRedis")
    public ResponseEntity<?> getCarByIdRedis(@RequestParam String carId) {
        return ResponseEntity.accepted().body(encarCache.getById(carId));
    }

    @GetMapping("/getEncarById")
    public ResponseEntity<?> getCarById(@RequestParam String carId) throws GetCarDetailException, RecaptchaException {
        return ResponseEntity.accepted().body(encarCacheService.fetchAndUpdateEncarDtoByCarId(carId));
    }

    @GetMapping("/updateCache")
    public ResponseEntity<?> updateCache() {
        cheCarCacheService.updateCheCarCache();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sub")
    public ResponseEntity<?> subtest() {
        subscribeService.subscribeUser(1234567);
        subscribeService.unSubscribeUser(1234567);
        subscribeService.getSubscribers();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/parseFemLink")
    public ResponseEntity<?> parseFemLink(@RequestParam String input) throws GetCarDetailException {
        return ResponseEntity.ok(utilService.parseLinkToCarId(input));
    }

    @GetMapping("/appendGoogle")
    public ResponseEntity<?> testGoogle() {
        googleService.appendClientRequestToGoogleSheet("clientText", "userName");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/testDynamicValutePart/{amount}")
    public ResponseEntity<Integer> testDynamicValutePart(@PathVariable double amount) {
        return ResponseEntity.ok(executionService.getDynamicValutePartByPriceInUsd(amount));
    }
}

