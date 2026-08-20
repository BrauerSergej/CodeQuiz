package dev.codequiz.mapper;

import dev.codequiz.domain.QuizAttempt;
import dev.codequiz.dto.quiz.QuizAttemptDto;
import dev.codequiz.dto.quiz.QuizAttemptStartDto;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuizAttemptMapper {

    // account в QuizAttemptDto намеренно нет — это всегда попытка ТЕКУЩЕГО
    // аутентифицированного пользователя (см. QuizAttemptRepository.findByIdAndAccount),
    // отдавать чужой accountId в ответе не нужно.
    @Mapping(source = "topic.id", target = "topicId")
    QuizAttemptDto toDto(QuizAttempt quizAttempt);

    // account и topic здесь не участвуют — MapStruct создаст QuizAttempt
    // только с полем totalQuestions (напрямую из totalQuestions в DTO — имена
    // совпадают). account, topic, status, startedAt, correctAnswers, score
    // сервис проставляет сам: account — из аутентификации, topic — по
    // topicId из этого же DTO (запрос к TopicRepository), остальное —
    // начальные значения новой попытки (0, IN_PROGRESS, now()).
    QuizAttempt toEntity(QuizAttemptStartDto startDto);
}