package ru.wallentos.carworker.exceptions;

/**
 * Исключение при столкновении с каптчей. Требуется решать каптчу.
 */
public class RecaptchaException extends RuntimeException {
    public RecaptchaException(String message) {
        super(message);
    }
}
