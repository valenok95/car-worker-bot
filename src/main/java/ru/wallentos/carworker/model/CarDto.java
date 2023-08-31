package ru.wallentos.carworker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class CarDto {
    private int carId;
    private int rawCarPrice;
    private int rawCarYear;
    private int rawCarMonth;
    private int rawCarPower;
}