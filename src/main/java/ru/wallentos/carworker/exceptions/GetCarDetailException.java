package ru.wallentos.carworker.exceptions;

/**
 * Исключение при получении деталей автомобиля с сайта.
 */
public class GetCarDetailException extends Exception {
    public GetCarDetailException(String message) {
        super(message);
    }
}
