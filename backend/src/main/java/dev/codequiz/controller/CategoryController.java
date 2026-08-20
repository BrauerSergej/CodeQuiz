package dev.codequiz.controller;

import dev.codequiz.dto.category.CategoryCreateDto;
import dev.codequiz.dto.category.CategoryDto;
import dev.codequiz.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// GET-эндпоинты доступны любому авторизованному пользователю (роль USER
// или ADMIN — оба вошли по JWT, разница только в правах на запись).
// POST/PUT/DELETE ограничены ролью ADMIN — правило задано в SecurityConfig
// (requestMatchers по HTTP-методу), а не здесь, чтобы вся конфигурация
// доступа собиралась в одном месте, а не размазывалась по контроллерам.
@RestController
@RequestMapping("/categories")
@Tag(name = "Categories", description = "Категории квизов")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Pageable Spring сам собирает из query-параметров запроса:
    // GET /categories?page=0&size=20&sort=displayOrder,asc
    // Без параметров — page=0, size=20 (дефолты Spring Data).
    @GetMapping
    public ResponseEntity<Page<CategoryDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(categoryService.getAllActive(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryCreateDto dto) {
        CategoryDto created = categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(@PathVariable Long id, @Valid @RequestBody CategoryCreateDto dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}