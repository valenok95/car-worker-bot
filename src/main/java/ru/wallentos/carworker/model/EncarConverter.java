package ru.wallentos.carworker.model;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class EncarConverter {
    private final ModelMapper modelMapper;

    public EncarConverter() {
        this.modelMapper = new ModelMapper();
    }

    public EncarDto convertToDto(EncarEntity entity) {
        return modelMapper.map(entity, EncarDto.class);
    }
}