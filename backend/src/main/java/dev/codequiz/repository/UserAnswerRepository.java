package dev.codequiz.repository;

import dev.codequiz.domain.Question;
import dev.codequiz.domain.QuizAttempt;
import dev.codequiz.domain.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    // Все ответы, данные в рамках конкретной попытки прохождения квиза —
    // используется, чтобы посчитать итоговый результат и показать пользователю
    // разбор его ответов после завершения квиза.
    List<UserAnswer> findByQuizAttempt(QuizAttempt quizAttempt);

    // Проверка, отвечал ли пользователь уже на этот вопрос в рамках данной попытки —
    // соответствует составному unique-constraint (quiz_attempt_id, question_id) в БД.
    // Нужна, чтобы не дать сохранить два ответа на один и тот же вопрос в одной попытке.
    Optional<UserAnswer> findByQuizAttemptAndQuestion(QuizAttempt quizAttempt, Question question);
}