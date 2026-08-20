package dev.codequiz.service;

import dev.codequiz.domain.Category;
import dev.codequiz.dto.category.CategoryCreateDto;
import dev.codequiz.dto.category.CategoryDto;
import dev.codequiz.exception.CategoryNotFoundException;
import dev.codequiz.exception.SlugAlreadyExistsException;
import dev.codequiz.mapper.CategoryMapper;
import dev.codequiz.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    // Публичный список — только активные категории, в порядке displayOrder,
    // порциями по pageable (см. параметры page/size/sort в запросе).
    // Page.map() — как Stream.map(), но применяется к содержимому страницы,
    // сохраняя при этом метаданные (totalElements, totalPages и т.д.).
    @Transactional(readOnly = true)
    public Page<CategoryDto> getAllActive(Pageable pageable) {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc(pageable)
                .map(categoryMapper::toDto);
    }

    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        Category category = findByIdOrThrow(id);
        return categoryMapper.toDto(category);
    }

    // Только для ADMIN (проверка роли — на уровне SecurityConfig/контроллера,
    // не здесь — сервис доверяет, что до него дошли только авторизованные вызовы).
    @Transactional
    public CategoryDto create(CategoryCreateDto dto) {
        if (categoryRepository.existsBySlug(dto.getSlug())) {
            throw new SlugAlreadyExistsException("Категория с slug '" + dto.getSlug() + "' уже существует");
        }

        Category category = categoryMapper.toEntity(dto);
        LocalDateTime now = LocalDateTime.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        Category saved = categoryRepository.save(category);
        return categoryMapper.toDto(saved);
    }

    @Transactional
    public CategoryDto update(Long id, CategoryCreateDto dto) {
        Category category = findByIdOrThrow(id);

        // Slug меняется — проверяем, что новый не занят ДРУГОЙ категорией
        // (а не этой же самой, которая и так уже владеет своим текущим slug).
        if (!category.getSlug().equals(dto.getSlug()) && categoryRepository.existsBySlug(dto.getSlug())) {
            throw new SlugAlreadyExistsException("Категория с slug '" + dto.getSlug() + "' уже существует");
        }

        categoryMapper.updateEntityFromDto(dto, category);
        category.setUpdatedAt(LocalDateTime.now());

        Category saved = categoryRepository.save(category);
        return categoryMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Category category = findByIdOrThrow(id);
        categoryRepository.delete(category);
    }

    private Category findByIdOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена: id=" + id));
    }
}