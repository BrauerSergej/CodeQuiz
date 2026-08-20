package dev.codequiz.exception;

// Бросается при старте попытки, если в теме меньше активных вопросов,
// чем запрошено (totalQuestions).
public class NotEnoughQuestionsException extends RuntimeException {

    public NotEnoughQuestionsException(String message) {
        super(message);
    }
}