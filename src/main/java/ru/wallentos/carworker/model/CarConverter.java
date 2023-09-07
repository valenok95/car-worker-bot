package ru.wallentos.carworker.model;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class CarConverter {
    private final ModelMapper modelMapper;

    public CarConverter() {
        this.modelMapper = new ModelMapper();
    }

    public CarDto convertToDto(CarEntity entity) {
        return modelMapper.map(entity, CarDto.class);
    }
}