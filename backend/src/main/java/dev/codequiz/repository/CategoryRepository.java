package dev.codequiz.repository;

import dev.codequiz.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Поиск категории по slug (человекочитаемый URL-идентификатор, например "java-basics").
    // Нужен для маршрутов вида GET /categories/{slug}, чтобы не показывать пользователю
    // числовой id в адресной строке.
    Optional<Category> findBySlug(String slug);

    // Проверка занятости slug перед созданием новой категории — аналогично
    // existsByEmail в UserRepository: дешевле, чем ловить ошибку unique-constraint из БД.
    boolean existsBySlug(String slug);

    // Список только активных категорий, отсортированный по полю displayOrder —
    // используется для публичного списка категорий на главной странице квиза.
    // Неактивные (скрытые/архивные) категории сюда не попадают.
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();
}
