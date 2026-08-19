package dev.codequiz.repository;

import dev.codequiz.domain.Question;
import dev.codequiz.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Все активные вопросы по теме — основной запрос для генерации квиза:
    // сервис берёт этот список и случайным образом выбирает N вопросов для попытки.
    List<Question> findByTopicAndActiveTrue(Topic topic);

    // Количество активных вопросов по теме, без загрузки самих объектов —
    // нужно, чтобы заранее проверить, хватает ли вопросов в теме для запрошенного
    // размера квиза (например, пользователь просит 20 вопросов, а в теме их всего 5).
    long countByTopicAndActiveTrue(Topic topic);
}