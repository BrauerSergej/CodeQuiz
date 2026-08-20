package dev.codequiz.exception;

// Бросается, когда вариант ответа не найден по id.
public class AnswerNotFoundException extends RuntimeException {

    public AnswerNotFoundException(String message) {
        super(message);
    }
}