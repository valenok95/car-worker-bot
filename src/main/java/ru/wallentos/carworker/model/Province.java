package ru.wallentos.carworker.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Province {
    private String provinceFullName;
    private int provincePriceInCurrency;
}
