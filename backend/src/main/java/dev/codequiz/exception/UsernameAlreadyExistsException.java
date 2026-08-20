package dev.codequiz.exception;

// Бросается при регистрации или смене username, если имя уже занято.
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}