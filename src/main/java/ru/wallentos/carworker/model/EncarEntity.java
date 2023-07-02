package ru.wallentos.carworker.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class EncarEntity {
    private String carId;
    private String rawCarPrice;
    private String rawCarYear;
    private String rawCarMonth;
    private String rawCarPower;
}
