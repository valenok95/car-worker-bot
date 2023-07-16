package ru.wallentos.carworker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.wallentos.carworker.cache.EncarCache;
import ru.wallentos.carworker.exceptions.GetCarDetailException;
import ru.wallentos.carworker.service.RedisCacheService;
import ru.wallentos.carworker.service.RestService;
import ru.wallentos.carworker.service.UtilService;

@RestController
public class WebController {
    private final RestService restService;
    private final EncarCache encarCache;
    private final RedisCacheService redisCacheService;
    private final UtilService utilService;

    @Autowired
    public WebController(RestService restService, EncarCache encarCache, RedisCacheService redisCacheService, UtilService utilService) {
        this.restService = restService;
        this.encarCache = encarCache;
        this.redisCacheService = redisCacheService;
        this.utilService = utilService;
    }
/*
    @PostMapping("/callback/update")
    public ResponseEntity<?> onUpdateReceived(@RequestBody Update update) {
        updateProcessor.processUpdate(update);
        return ResponseEntity.ok().build();
    }*/

    @GetMapping("/getEncarByIdJsoup")
    public ResponseEntity<?> getCarByIdJsoup(@RequestParam String carId) throws GetCarDetailException {
        return ResponseEntity.accepted().body(restService.getEncarDataByJsoup(carId));
    }

    @GetMapping("/getEncarByIdRedis")
    public ResponseEntity<?> getCarByIdRedis(@RequestParam String carId) {
        return ResponseEntity.accepted().body(encarCache.getEncarDtoByCarId(carId));
    }

    @GetMapping("/getEncarById")
    public ResponseEntity<?> getCarById(@RequestParam String carId) throws GetCarDetailException {
        return ResponseEntity.accepted().body(redisCacheService.fetchAndUpdateEncarDtoByCarId(carId));
    }

    @GetMapping("/getPageById")
    public ResponseEntity executeCarPageById(@RequestParam String carId) {
        restService.getEncarPageByJsoup(carId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/openPage")
    public void openPage() {
        restService.openPage();
    }

    @GetMapping("/closePage")
    public void closePage() {
        restService.closePage();
    }

    @GetMapping("/updateCache")
    public ResponseEntity<?> updateCache() {
        redisCacheService.updateEncarCache();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/parseFemLink")
    public ResponseEntity<?> parseFemLink(@RequestParam String input) throws GetCarDetailException {
        return ResponseEntity.ok(utilService.parseLinkToCarId(input));
    }
}

