package dev.codequiz.repository;

import dev.codequiz.domain.Question;
import dev.codequiz.domain.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // List-версия — для внутренней бизнес-логики (QuizAttemptService), где
    // нужен ВЕСЬ набор активных вопросов темы сразу, чтобы вычесть из него
    // уже отвеченные и определить следующий вопрос попытки. Пагинация здесь
    // не подходит — это не то, что видит пользователь как список.
    List<Question> findByTopicAndActiveTrue(Topic topic);

    // Page-версия — для админского списка вопросов (QuestionController),
    // где как раз нужна порционная выдача, а не всё разом.
    Page<Question> findByTopicAndActiveTrue(Topic topic, Pageable pageable);

    // Количество активных вопросов по теме, без загрузки самих объектов —
    // нужно, чтобы заранее проверить, хватает ли вопросов в теме для запрошенного
    // размера квиза (например, пользователь просит 20 вопросов, а в теме их всего 5).
    long countByTopicAndActiveTrue(Topic topic);
}