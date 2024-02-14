package ru.wallentos.carworker.exceptions;

/**
 * Исключение при получении деталей автомобиля с сайта.
 */
public class GetCarDetailException extends RuntimeException {
    public GetCarDetailException(String message) {
        super(message);
    }
}
