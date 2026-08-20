package dev.codequiz.controller;

import dev.codequiz.dto.answer.AnswerCreateDto;
import dev.codequiz.dto.answer.AnswerDto;
import dev.codequiz.service.AnswerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Тоже полностью ADMIN-only — AnswerDto содержит поле correct (см. пояснение
// в QuestionController про explanation, здесь та же логика).
@RestController
@RequestMapping("/answers")
@Tag(name = "Answers", description = "Варианты ответов (админ) — с полем correct")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @GetMapping
    public ResponseEntity<List<AnswerDto>> getByQuestion(@RequestParam Long questionId) {
        return ResponseEntity.ok(answerService.getByQuestion(questionId));
    }

    @PostMapping
    public ResponseEntity<AnswerDto> create(@Valid @RequestBody AnswerCreateDto dto) {
        AnswerDto created = answerService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnswerDto> update(@PathVariable Long id, @Valid @RequestBody AnswerCreateDto dto) {
        return ResponseEntity.ok(answerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        answerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}