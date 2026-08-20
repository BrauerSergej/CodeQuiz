package dev.codequiz.controller;

import dev.codequiz.dto.question.QuestionCreateDto;
import dev.codequiz.dto.question.QuestionDto;
import dev.codequiz.service.QuestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// В отличие от CategoryController/TopicController, здесь ВЕСЬ контроллер —
// и чтение, и запись — доступен только ADMIN (см. SecurityConfig). Причина:
// QuestionDto содержит поле explanation (объяснение правильного ответа) —
// если бы GET был открыт обычным USER, любой мог бы прочитать объяснения
// заранее через Swagger/DevTools, до прохождения квиза. Обычные пользователи
// увидят вопросы только через отдельный эндпоинт квиза с QuestionQuizDto
// (без explanation) — он появится вместе с QuizAttemptController.
@RestController
@RequestMapping("/questions")
@Tag(name = "Questions", description = "Вопросы (админ) — с полным содержимым, включая объяснение")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<Page<QuestionDto>> getByTopic(@RequestParam Long topicId, Pageable pageable) {
        return ResponseEntity.ok(questionService.getByTopic(topicId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<QuestionDto> create(@Valid @RequestBody QuestionCreateDto dto) {
        QuestionDto created = questionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionDto> update(@PathVariable Long id, @Valid @RequestBody QuestionCreateDto dto) {
        return ResponseEntity.ok(questionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}