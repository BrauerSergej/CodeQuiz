package dev.codequiz.exception;

// Бросается при попытке ответить второй раз на один и тот же вопрос
// в рамках одной попытки — соответствует unique-constraint
// (quiz_attempt_id, question_id) в таблице user_answers.
public class QuestionAlreadyAnsweredException extends RuntimeException {

    public QuestionAlreadyAnsweredException(String message) {
        super(message);
    }
}