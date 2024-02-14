package ru.wallentos.carworker.exceptions;

/**
 * Исключение если в справочнике электромобилей encar отсутствует тачка.
 */
public class ElectricCarNotFoundException extends RuntimeException {
    public ElectricCarNotFoundException(String message) {
        super(message);
    }
}
