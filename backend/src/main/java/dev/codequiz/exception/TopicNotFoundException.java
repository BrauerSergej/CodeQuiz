package dev.codequiz.exception;

// Бросается, когда тема не найдена по id.
public class TopicNotFoundException extends RuntimeException {

    public TopicNotFoundException(String message) {
        super(message);
    }
}
