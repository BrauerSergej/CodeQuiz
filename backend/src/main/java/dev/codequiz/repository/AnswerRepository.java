package dev.codequiz.repository;

import dev.codequiz.domain.Answer;
import dev.codequiz.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // Варианты ответов для конкретного вопроса, в порядке отображения (displayOrder) —
    // используется при показе вопроса пользователю, чтобы варианты шли в заданном
    // порядке, а не в случайном порядке из базы.
    List<Answer> findByQuestionOrderByDisplayOrderAsc(Question question);
}