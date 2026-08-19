package dev.codequiz.repository;

import dev.codequiz.domain.UserAnswer;
import dev.codequiz.domain.UserAnswerSelection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAnswerSelectionRepository extends JpaRepository<UserAnswerSelection, Long> {

    // Все выбранные варианты ответа для конкретного ответа пользователя —
    // актуально для вопросов с несколькими правильными вариантами (multiple choice),
    // где на один UserAnswer может приходиться несколько выбранных Answer.
    List<UserAnswerSelection> findByUserAnswer(UserAnswer userAnswer);
}