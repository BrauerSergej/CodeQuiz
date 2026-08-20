package dev.codequiz.service;

import dev.codequiz.domain.Question;
import dev.codequiz.domain.Topic;
import dev.codequiz.dto.question.QuestionCreateDto;
import dev.codequiz.dto.question.QuestionDto;
import dev.codequiz.exception.QuestionNotFoundException;
import dev.codequiz.exception.TopicNotFoundException;
import dev.codequiz.mapper.QuestionMapper;
import dev.codequiz.repository.QuestionRepository;
import dev.codequiz.repository.TopicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Административный сервис — работа с полным QuestionDto (с explanation).
// Сборка "урезанной" версии для прохождения квиза (QuestionQuizDto, без
// explanation и без correct у ответов) собирается отдельно в QuizAttemptService
// (там нужен весь набор вопросов темы сразу, не порциями — см. пояснение
// в QuestionRepository про две версии findByTopicAndActiveTrue).
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;
    private final QuestionMapper questionMapper;

    public QuestionService(QuestionRepository questionRepository,
                           TopicRepository topicRepository,
                           QuestionMapper questionMapper) {
        this.questionRepository = questionRepository;
        this.topicRepository = topicRepository;
        this.questionMapper = questionMapper;
    }

    @Transactional(readOnly = true)
    public Page<QuestionDto> getByTopic(Long topicId, Pageable pageable) {
        Topic topic = findTopicOrThrow(topicId);
        return questionRepository.findByTopicAndActiveTrue(topic, pageable)
                .map(questionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public QuestionDto getById(Long id) {
        return questionMapper.toDto(findByIdOrThrow(id));
    }

    // Только для ADMIN — проверка роли на уровне SecurityConfig.
    @Transactional
    public QuestionDto create(QuestionCreateDto dto) {
        Topic topic = findTopicOrThrow(dto.getTopicId());

        Question question = questionMapper.toEntity(dto);
        question.setTopic(topic);
        LocalDateTime now = LocalDateTime.now();
        question.setCreatedAt(now);
        question.setUpdatedAt(now);

        Question saved = questionRepository.save(question);
        return questionMapper.toDto(saved);
    }

    @Transactional
    public QuestionDto update(Long id, QuestionCreateDto dto) {
        Question question = findByIdOrThrow(id);
        Topic topic = findTopicOrThrow(dto.getTopicId());

        questionMapper.updateEntityFromDto(dto, question);
        question.setTopic(topic);
        question.setUpdatedAt(LocalDateTime.now());

        Question saved = questionRepository.save(question);
        return questionMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Question question = findByIdOrThrow(id);
        questionRepository.delete(question);
    }

    private Question findByIdOrThrow(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException("Вопрос не найден: id=" + id));
    }

    private Topic findTopicOrThrow(Long topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException("Тема не найдена: id=" + topicId));
    }
}