package dev.codequiz.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// Response-объект — результат ответа сразу после отправки UserAnswerSubmitDto.
// Только здесь, после того как пользователь уже ответил, можно безопасно
// раскрыть correctAnswerIds — правильные варианты для этого конкретного
// вопроса. До этого момента (см. AnswerQuizDto) эта информация скрыта.
@Schema(description = "Результат проверки ответа пользователя на вопрос")
public class UserAnswerResultDto {

    @Schema(description = "Идентификатор вопроса", example = "5")
    private Long questionId;

    @Schema(description = "Правильно ли ответил пользователь", example = "true")
    private boolean correct;

    @Schema(description = "Идентификаторы правильных вариантов ответа", example = "[3]")
    private List<Long> correctAnswerIds;

    @Schema(description = "Объяснение правильного ответа")
    private String explanation;

    public UserAnswerResultDto() {
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public List<Long> getCorrectAnswerIds() {
        return correctAnswerIds;
    }

    public void setCorrectAnswerIds(List<Long> correctAnswerIds) {
        this.correctAnswerIds = correctAnswerIds;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}