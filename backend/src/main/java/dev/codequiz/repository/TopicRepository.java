package dev.codequiz.repository;

import dev.codequiz.domain.Category;
import dev.codequiz.domain.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    // Page, а не List — темы внутри категории тоже отдаются порциями клиенту.
    Page<Topic> findByCategoryAndActiveTrueOrderByDisplayOrderAsc(Category category, Pageable pageable);
}