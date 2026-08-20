package dev.codequiz.mapper;

import dev.codequiz.domain.Topic;
import dev.codequiz.dto.topic.TopicCreateDto;
import dev.codequiz.dto.topic.TopicDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TopicMapper {

    // Здесь автоматическое сопоставление по имени не сработает: в entity
    // есть объект category (ManyToOne), а в DTO — плоские categoryId и
    // categoryName. @Mapping(source = "category.id", ...) говорит MapStruct
    // "возьми topic.getCategory().getId()" — он умеет обращаться к вложенным
    // полям через точку, а не только к прямым геттерам.
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    TopicDto toDto(Topic topic);

    // Обратное направление MapStruct сделать не может: чтобы превратить
    // categoryId (Long) обратно в объект Category, нужен поход в БД
    // (CategoryRepository.findById), а маппер — чистая функция без
    // репозиториев. Поэтому поле category здесь игнорируется, и сервис
    // сам находит Category по categoryId и проставляет её через
    // topic.setCategory(...) после вызова toEntity().
    @Mapping(target = "category", ignore = true)
    Topic toEntity(TopicCreateDto createDto);

    @Mapping(target = "category", ignore = true)
    void updateEntityFromDto(TopicCreateDto dto, @MappingTarget Topic topic);
}