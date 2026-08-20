package dev.codequiz.exception;

// Бросается, когда попытка не найдена по id ИЛИ найдена, но принадлежит
// другому пользователю — см. QuizAttemptRepository.findByIdAndAccount.
// Намеренно один и тот же текст/статус для обоих случаев (как и с
// InvalidCredentialsException при логине) — чтобы не дать пользователю
// понять, существует ли вообще чужая попытка с таким id (защита от IDOR).
public class QuizAttemptNotFoundException extends RuntimeException {

    public QuizAttemptNotFoundException(String message) {
        super(message);
    }
}