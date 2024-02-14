package ru.wallentos.carworker.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CarEntity {
    private String carId;
    private String rawCarPrice;
    private String rawCarYear;
    private String rawCarMonth;
    private String rawCarVolume;
    private String rawCarPower;
    private String rawCarProvinceName;
    private int myAccidentCost;
    private int otherAccidentCost;
    private boolean hasInsuranceInfo;
    private boolean isElectric;
}
