package dev.codequiz.exception;

// Бросается при попытке повторно запросить код подтверждения для аккаунта,
// который уже ACTIVE — присылать код тут бессмысленно, пользователю нужно
// просто войти.
public class EmailAlreadyConfirmedException extends RuntimeException {

    public EmailAlreadyConfirmedException(String message) {
        super(message);
    }
}