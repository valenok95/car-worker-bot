package ru.wallentos.carworker.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CarTotalResultData {
    double cnyPrice;
    double dollarPrice;
    String provinceName;
    int carId;
}
