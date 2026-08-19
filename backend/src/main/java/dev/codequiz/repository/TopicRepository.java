package dev.codequiz.repository;

import dev.codequiz.domain.Category;
import dev.codequiz.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    // Список активных тем внутри конкретной категории, в порядке отображения.
    // Используется на странице категории, чтобы показать пользователю доступные темы
    // для прохождения квиза (например, категория "Java" → темы "Коллекции", "Стримы" и т.д.).
    List<Topic> findByCategoryAndActiveTrueOrderByDisplayOrderAsc(Category category);
}