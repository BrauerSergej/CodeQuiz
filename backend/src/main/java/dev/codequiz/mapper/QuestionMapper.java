package dev.codequiz.mapper;

import dev.codequiz.domain.Question;
import dev.codequiz.dto.answer.AnswerQuizDto;
import dev.codequiz.dto.question.QuestionCreateDto;
import dev.codequiz.dto.question.QuestionDto;
import dev.codequiz.dto.question.QuestionQuizDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    // Полный DTO (админка/разбор после ответа) — topic сплющивается в topicId,
    // как и в TopicMapper.
    @Mapping(source = "topic.id", target = "topicId")
    QuestionDto toDto(Question question);

    @Mapping(target = "topic", ignore = true)
    Question toEntity(QuestionCreateDto createDto);

    @Mapping(target = "topic", ignore = true)
    void updateEntityFromDto(QuestionCreateDto dto, @MappingTarget Question question);

    // QuestionQuizDto собирается из ДВУХ источников: самого вопроса (текст,
    // сложность и т.д.) и отдельно подготовленного списка AnswerQuizDto
    // (уже без поля correct — см. AnswerMapper.toQuizDto). Answer entity
    // не хранит прямой ссылки "все свои варианты" в списке внутри Question,
    // поэтому этот список приходится собирать в сервисе через
    // AnswerRepository.findByQuestionOrderByDisplayOrderAsc(...) и передавать
    // сюда вторым аргументом — маппер сам к базе данных не обращается.
    @Mapping(target = "id", source = "question.id")
    @Mapping(target = "questionText", source = "question.questionText")
    @Mapping(target = "difficulty", source = "question.difficulty")
    @Mapping(target = "questionType", source = "question.questionType")
    @Mapping(target = "codeSnippet", source = "question.codeSnippet")
    @Mapping(target = "codeLanguage", source = "question.codeLanguage")
    @Mapping(target = "answers", source = "answers")
    QuestionQuizDto toQuizDto(Question question, List<AnswerQuizDto> answers);
}