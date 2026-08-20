package dev.codequiz.mapper;

import dev.codequiz.domain.Answer;
import dev.codequiz.dto.answer.AnswerCreateDto;
import dev.codequiz.dto.answer.AnswerDto;
import dev.codequiz.dto.answer.AnswerQuizDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    // Полный DTO с полем correct — только для админки и разбора после ответа.
    AnswerDto toDto(Answer answer);

    // Урезанный DTO для показа во время прохождения квиза. AnswerQuizDto
    // просто не содержит поля correct, поэтому MapStruct его и не мапит —
    // никакого способа случайно "забыть скрыть" правильный ответ здесь нет,
    // это гарантируется на уровне типов, а не договорённостью между людьми.
    AnswerQuizDto toQuizDto(Answer answer);

    @Mapping(target = "question", ignore = true)
    Answer toEntity(AnswerCreateDto createDto);

    @Mapping(target = "question", ignore = true)
    void updateEntityFromDto(AnswerCreateDto dto, @MappingTarget Answer answer);
}