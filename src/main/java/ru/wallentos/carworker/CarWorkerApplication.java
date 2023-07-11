package ru.wallentos.carworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarWorkerApplication.class, args);
    }

}
