package dev.codequiz.dto.question;

import dev.codequiz.domain.enums.CodeLanguage;
import dev.codequiz.domain.enums.Difficulty;
import dev.codequiz.domain.enums.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;

// Полный response-объект вопроса — включает explanation (объяснение ответа).
// Используется в двух местах: (1) в админке при просмотре/редактировании
// вопросов, (2) при показе пользователю разбора ПОСЛЕ того, как он уже
// ответил на вопрос в рамках попытки. НЕ используется во время активного
// прохождения квиза — для этого есть QuestionQuizDto без explanation,
// чтобы не подсказывать ответ раньше времени.
@Schema(description = "Полные данные вопроса, включая объяснение ответа")
public class QuestionDto {

    @Schema(description = "Идентификатор вопроса", example = "1")
    private Long id;

    @Schema(description = "Текст вопроса")
    private String questionText;

    @Schema(description = "Объяснение правильного ответа")
    private String explanation;

    @Schema(description = "Сложность вопроса")
    private Difficulty difficulty;

    @Schema(description = "Тип вопроса")
    private QuestionType questionType;

    @Schema(description = "Фрагмент кода")
    private String codeSnippet;

    @Schema(description = "Язык кода")
    private CodeLanguage codeLanguage;

    @Schema(description = "Идентификатор темы", example = "1")
    private Long topicId;

    @Schema(description = "Активен ли вопрос", example = "true")
    private boolean active;

    public QuestionDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    public CodeLanguage getCodeLanguage() {
        return codeLanguage;
    }

    public void setCodeLanguage(CodeLanguage codeLanguage) {
        this.codeLanguage = codeLanguage;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}