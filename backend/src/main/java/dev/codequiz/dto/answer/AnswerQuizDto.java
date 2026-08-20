package dev.codequiz.dto.answer;

import io.swagger.v3.oas.annotations.media.Schema;

// КРИТИЧЕСКИ ВАЖНЫЙ момент: у этого DTO НЕТ поля "correct". Он используется,
// когда вопрос показывается пользователю во время прохождения квиза
// (см. QuestionQuizDto). Если бы здесь было поле correct, любой пользователь
// мог бы открыть вкладку "Network" в браузере, посмотреть JSON-ответ сервера
// и увидеть правильный ответ ДО того, как выбрал вариант — это полностью
// обесценивает квиз. Для сравнения — AnswerDto (с полем correct) используется
// только в админке и при показе разбора ПОСЛЕ ответа.
@Schema(description = "Вариант ответа для показа во время прохождения квиза (без признака правильности)")
public class AnswerQuizDto {

    @Schema(description = "Идентификатор варианта ответа", example = "1")
    private Long id;

    @Schema(description = "Текст варианта ответа")
    private String answerText;

    @Schema(description = "Порядок отображения", example = "1")
    private int displayOrder;

    public AnswerQuizDto() {
    }

    public AnswerQuizDto(Long id, String answerText, int displayOrder) {
        this.id = id;
        this.answerText = answerText;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}