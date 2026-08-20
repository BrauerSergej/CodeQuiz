package dev.codequiz.service;

import dev.codequiz.domain.Category;
import dev.codequiz.domain.Topic;
import dev.codequiz.dto.topic.TopicCreateDto;
import dev.codequiz.dto.topic.TopicDto;
import dev.codequiz.exception.CategoryNotFoundException;
import dev.codequiz.exception.TopicNotFoundException;
import dev.codequiz.mapper.TopicMapper;
import dev.codequiz.repository.CategoryRepository;
import dev.codequiz.repository.TopicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final CategoryRepository categoryRepository;
    private final TopicMapper topicMapper;

    public TopicService(TopicRepository topicRepository,
                        CategoryRepository categoryRepository,
                        TopicMapper topicMapper) {
        this.topicRepository = topicRepository;
        this.categoryRepository = categoryRepository;
        this.topicMapper = topicMapper;
    }

    // Список активных тем внутри категории, порциями по pageable — сначала
    // находим саму категорию (чтобы отдать 404, если categoryId битый,
    // а не пустую страницу молча), затем её темы.
    @Transactional(readOnly = true)
    public Page<TopicDto> getByCategory(Long categoryId, Pageable pageable) {
        Category category = findCategoryOrThrow(categoryId);
        return topicRepository.findByCategoryAndActiveTrueOrderByDisplayOrderAsc(category, pageable)
                .map(topicMapper::toDto);
    }

    @Transactional(readOnly = true)
    public TopicDto getById(Long id) {
        return topicMapper.toDto(findByIdOrThrow(id));
    }

    // Только для ADMIN — проверка роли на уровне SecurityConfig.
    @Transactional
    public TopicDto create(TopicCreateDto dto) {
        Category category = findCategoryOrThrow(dto.getCategoryId());

        Topic topic = topicMapper.toEntity(dto);
        // category игнорируется маппером (см. комментарий в TopicMapper) —
        // проставляем её сами, раз уже нашли по categoryId.
        topic.setCategory(category);
        LocalDateTime now = LocalDateTime.now();
        topic.setCreatedAt(now);
        topic.setUpdatedAt(now);

        Topic saved = topicRepository.save(topic);
        return topicMapper.toDto(saved);
    }

    @Transactional
    public TopicDto update(Long id, TopicCreateDto dto) {
        Topic topic = findByIdOrThrow(id);

        // Категорию тема тоже может сменить — проверяем и перепривязываем,
        // даже если сохранённый categoryId совпадает с прежним (лишний поход
        // в БД, но код проще и без риска рассинхронизации).
        Category category = findCategoryOrThrow(dto.getCategoryId());

        topicMapper.updateEntityFromDto(dto, topic);
        topic.setCategory(category);
        topic.setUpdatedAt(LocalDateTime.now());

        Topic saved = topicRepository.save(topic);
        return topicMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Topic topic = findByIdOrThrow(id);
        topicRepository.delete(topic);
    }

    private Topic findByIdOrThrow(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new TopicNotFoundException("Тема не найдена: id=" + id));
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена: id=" + categoryId));
    }
}