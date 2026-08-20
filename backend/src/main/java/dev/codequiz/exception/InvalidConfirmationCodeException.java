package dev.codequiz.exception;

// Бросается при подтверждении email, если код не совпадает, не найден
// или уже был использован ранее.
public class InvalidConfirmationCodeException extends RuntimeException {

    public InvalidConfirmationCodeException(String message) {
        super(message);
    }
}