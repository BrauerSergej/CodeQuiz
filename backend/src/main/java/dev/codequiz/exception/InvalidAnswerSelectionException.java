package dev.codequiz.exception;

// Бросается, если пользователь прислал questionId, не относящийся к теме
// попытки, или selectedAnswerIds, среди которых есть id, не принадлежащий
// присланному вопросу — оба случая означают подделанный/некорректный запрос.
public class InvalidAnswerSelectionException extends RuntimeException {

    public InvalidAnswerSelectionException(String message) {
        super(message);
    }
}