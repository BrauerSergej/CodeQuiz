package dev.codequiz.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Request-объект отправки ответа пользователя на конкретный вопрос —
// POST /quiz-attempts/{attemptId}/answers. selectedAnswerIds — список,
// а не одно значение, потому что QuestionType допускает вопросы с
// несколькими правильными вариантами (см. Difficulty/QuestionType enum) —
// для одиночного выбора список просто будет содержать один элемент.
//
// Обратите внимание: здесь нет отдельного DTO для UserAnswerSelection —
// он не нужен как самостоятельный объект в API, выбранные варианты
// передаются прямо списком id внутри этого DTO, а таблица
// user_answer_selections — деталь реализации на стороне сервера.
@Schema(description = "Ответ пользователя на вопрос в рамках попытки")
public class UserAnswerSubmitDto {

    @Schema(description = "Идентификатор вопроса", example = "5")
    @NotNull(message = "Вопрос обязателен")
    private Long questionId;

    @Schema(description = "Идентификаторы выбранных вариантов ответа", example = "[3]")
    @NotEmpty(message = "Нужно выбрать хотя бы один вариант ответа")
    private List<Long> selectedAnswerIds;

    public UserAnswerSubmitDto() {
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public List<Long> getSelectedAnswerIds() {
        return selectedAnswerIds;
    }

    public void setSelectedAnswerIds(List<Long> selectedAnswerIds) {
        this.selectedAnswerIds = selectedAnswerIds;
    }
}