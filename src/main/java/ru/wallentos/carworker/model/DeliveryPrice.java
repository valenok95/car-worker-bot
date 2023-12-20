package ru.wallentos.carworker.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeliveryPrice {
    private int bishkekDeliveryPrice;
    private int ussuriyskDeliveryPrice;
}
