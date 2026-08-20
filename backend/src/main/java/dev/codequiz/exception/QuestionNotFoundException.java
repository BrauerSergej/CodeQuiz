package dev.codequiz.exception;

// Бросается, когда вопрос не найден по id.
public class QuestionNotFoundException extends RuntimeException {

    public QuestionNotFoundException(String message) {
        super(message);
    }
}