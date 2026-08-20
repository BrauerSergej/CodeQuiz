package dev.codequiz.dto.question;

import dev.codequiz.domain.enums.CodeLanguage;
import dev.codequiz.domain.enums.Difficulty;
import dev.codequiz.domain.enums.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Request-объект для создания/обновления вопроса — используется в
// админ-эндпоинтах (создание контента для квизов), не пользователем при
// прохождении квиза.
@Schema(description = "Данные для создания или обновления вопроса")
public class QuestionCreateDto {

    @Schema(description = "Текст вопроса", example = "Что выведет этот код?")
    @NotBlank(message = "Текст вопроса обязателен")
    @Size(max = 1000, message = "Текст вопроса не длиннее 1000 символов")
    private String questionText;

    @Schema(description = "Объяснение правильного ответа, показывается после ответа")
    @Size(max = 2000, message = "Объяснение не длиннее 2000 символов")
    private String explanation;

    @Schema(description = "Сложность вопроса")
    @NotNull(message = "Сложность обязательна")
    private Difficulty difficulty;

    @Schema(description = "Тип вопроса (один правильный ответ / несколько)")
    @NotNull(message = "Тип вопроса обязателен")
    private QuestionType questionType;

    @Schema(description = "Фрагмент кода, если вопрос про код")
    @Size(max = 3000, message = "Фрагмент кода не длиннее 3000 символов")
    private String codeSnippet;

    @Schema(description = "Язык кода в code Snippet, если применимо")
    private CodeLanguage codeLanguage;

    @Schema(description = "Идентификатор темы, к которой относится вопрос", example = "1")
    @NotNull(message = "Тема обязательна")
    private Long topicId;

    @Schema(description = "Активен ли вопрос (участвует ли в генерации квизов)", example = "true")
    private boolean active;

    public QuestionCreateDto() {
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