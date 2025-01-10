package ru.wallentos.carworker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.wallentos.carworker.service.RestService;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestServiceTests extends BaseWebClientTest {
    @Autowired
    RestService service;
    private static final String METHOD_URL = "/v6/9652b74df78d3c8882998c04/latest/USD";

    @Test
    public void restTest() throws InterruptedException {
        service.refreshDutyExchangeRates();
    }

    public void someTest() {

    }


}
