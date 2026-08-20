package dev.codequiz.dto.answer;

import io.swagger.v3.oas.annotations.media.Schema;

// Полный response-объект варианта ответа, С полем correct. Используется
// в админке и при показе пользователю разбора ПОСЛЕ того, как попытка
// квиза завершена — то есть когда раскрывать правильный ответ уже безопасно.
@Schema(description = "Полные данные варианта ответа, включая признак правильности")
public class AnswerDto {

    @Schema(description = "Идентификатор варианта ответа", example = "1")
    private Long id;

    @Schema(description = "Текст варианта ответа")
    private String answerText;

    @Schema(description = "Является ли вариант правильным", example = "true")
    private boolean correct;

    @Schema(description = "Порядок отображения", example = "1")
    private int displayOrder;

    public AnswerDto() {
    }

    public AnswerDto(Long id, String answerText, boolean correct, int displayOrder) {
        this.id = id;
        this.answerText = answerText;
        this.correct = correct;
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

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}