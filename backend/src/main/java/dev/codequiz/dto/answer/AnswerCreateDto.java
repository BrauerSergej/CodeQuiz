package dev.codequiz.dto.answer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// Request-объект для создания/обновления варианта ответа — админ-эндпоинт.
// Здесь поле "correct" присутствует осознанно: администратор ДОЛЖЕН
// указывать, какой вариант правильный, при наполнении базы вопросов.
@Schema(description = "Данные для создания или обновления варианта ответа")
public class AnswerCreateDto {

    @Schema(description = "Текст варианта ответа")
    @NotBlank(message = "Текст ответа обязателен")
    @Size(max = 500, message = "Текст ответа не длиннее 500 символов")
    private String answerText;

    @Schema(description = "Является ли этот вариант правильным", example = "true")
    @NotNull(message = "Признак правильности обязателен")
    private Boolean correct;

    @Schema(description = "Идентификатор вопроса", example = "1")
    @NotNull(message = "Вопрос обязателен")
    private Long questionId;

    @Schema(description = "Порядок отображения варианта", example = "1")
    @PositiveOrZero(message = "Порядок отображения не может быть отрицательным")
    private int displayOrder;

    public AnswerCreateDto() {
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}