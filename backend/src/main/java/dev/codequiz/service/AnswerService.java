package dev.codequiz.service;

import dev.codequiz.domain.Answer;
import dev.codequiz.domain.Question;
import dev.codequiz.dto.answer.AnswerCreateDto;
import dev.codequiz.dto.answer.AnswerDto;
import dev.codequiz.exception.AnswerNotFoundException;
import dev.codequiz.exception.QuestionNotFoundException;
import dev.codequiz.mapper.AnswerMapper;
import dev.codequiz.repository.AnswerRepository;
import dev.codequiz.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// Тоже административный сервис — отдаёт полный AnswerDto с полем correct.
// Метод, который отдаёт варианты БЕЗ correct для прохождения квиза
// (AnswerQuizDto), понадобится в QuizAttempt-сервисе, не здесь.
@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final AnswerMapper answerMapper;

    public AnswerService(AnswerRepository answerRepository,
                         QuestionRepository questionRepository,
                         AnswerMapper answerMapper) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.answerMapper = answerMapper;
    }

    @Transactional(readOnly = true)
    public List<AnswerDto> getByQuestion(Long questionId) {
        Question question = findQuestionOrThrow(questionId);
        return answerRepository.findByQuestionOrderByDisplayOrderAsc(question).stream()
                .map(answerMapper::toDto)
                .toList();
    }

    // Только для ADMIN — проверка роли на уровне SecurityConfig.
    @Transactional
    public AnswerDto create(AnswerCreateDto dto) {
        Question question = findQuestionOrThrow(dto.getQuestionId());

        Answer answer = answerMapper.toEntity(dto);
        answer.setQuestion(question);
        LocalDateTime now = LocalDateTime.now();
        answer.setCreatedAt(now);
        answer.setUpdatedAt(now);

        Answer saved = answerRepository.save(answer);
        return answerMapper.toDto(saved);
    }

    @Transactional
    public AnswerDto update(Long id, AnswerCreateDto dto) {
        Answer answer = findByIdOrThrow(id);
        Question question = findQuestionOrThrow(dto.getQuestionId());

        answerMapper.updateEntityFromDto(dto, answer);
        answer.setQuestion(question);
        answer.setUpdatedAt(LocalDateTime.now());

        Answer saved = answerRepository.save(answer);
        return answerMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Answer answer = findByIdOrThrow(id);
        answerRepository.delete(answer);
    }

    private Answer findByIdOrThrow(Long id) {
        return answerRepository.findById(id)
                .orElseThrow(() -> new AnswerNotFoundException("Вариант ответа не найден: id=" + id));
    }

    private Question findQuestionOrThrow(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Вопрос не найден: id=" + questionId));
    }
}