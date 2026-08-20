package dev.codequiz.exception;

// Бросается при попытке ответить на вопрос или получить следующий вопрос
// в рамках уже завершённой (COMPLETED) или брошенной (ABANDONED) попытки.
public class QuizAttemptNotInProgressException extends RuntimeException {

    public QuizAttemptNotInProgressException(String message) {
        super(message);
    }
}