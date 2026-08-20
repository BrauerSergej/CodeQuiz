package dev.codequiz.exception;

// Бросается при регистрации, если email уже занят другим аккаунтом.
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}