package dev.codequiz.mapper;

import dev.codequiz.domain.Category;
import dev.codequiz.dto.category.CategoryCreateDto;
import dev.codequiz.dto.category.CategoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(Category category);

    // Создание новой сущности из request-объекта — id, createdAt, updatedAt
    // отсутствуют в CategoryCreateDto, поэтому останутся null/0 и будут
    // проставлены сервером (id — автогенерацией, даты — при save()).
    Category toEntity(CategoryCreateDto createDto);

    // Обновление уже существующей категории теми же полями, что при создании
    // (name, slug, description, displayOrder, active).
    void updateEntityFromDto(CategoryCreateDto dto, @MappingTarget Category category);
}