package dev.codequiz.controller;

import dev.codequiz.dto.topic.TopicCreateDto;
import dev.codequiz.dto.topic.TopicDto;
import dev.codequiz.service.TopicService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Права доступа — как у CategoryController: чтение любому авторизованному,
// запись только ADMIN (правило в SecurityConfig).
@RestController
@RequestMapping("/topics")
@Tag(name = "Topics", description = "Темы внутри категорий")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    // categoryId — обязательный query-параметр, а не часть пути
    // (/categories/{id}/topics), потому что тема — самостоятельный ресурс
    // со своим id, а не вложенная сущность без собственного адреса.
    // Pageable — как в CategoryController: GET /topics?categoryId=1&page=0&size=20
    @GetMapping
    public ResponseEntity<Page<TopicDto>> getByCategory(@RequestParam Long categoryId, Pageable pageable) {
        return ResponseEntity.ok(topicService.getByCategory(categoryId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(topicService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TopicDto> create(@Valid @RequestBody TopicCreateDto dto) {
        TopicDto created = topicService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TopicDto> update(@PathVariable Long id, @Valid @RequestBody TopicCreateDto dto) {
        return ResponseEntity.ok(topicService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        topicService.delete(id);
        return ResponseEntity.noContent().build();
    }
}