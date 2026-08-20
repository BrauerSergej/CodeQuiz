package dev.codequiz.dto.question;

import dev.codequiz.domain.enums.CodeLanguage;
import dev.codequiz.domain.enums.Difficulty;
import dev.codequiz.domain.enums.QuestionType;
import dev.codequiz.dto.answer.AnswerQuizDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// Response-объект вопроса ВО ВРЕМЯ прохождения квиза. Ключевое отличие от
// QuestionDto: нет поля explanation (не подсказываем разбор до ответа) и
// варианты ответов приходят как AnswerQuizDto — без поля correct
// (см. пояснение в AnswerQuizDto, почему это важно).
@Schema(description = "Данные вопроса для показа пользователю во время прохождения квиза")
public class QuestionQuizDto {

    @Schema(description = "Идентификатор вопроса", example = "1")
    private Long id;

    @Schema(description = "Текст вопроса")
    private String questionText;

    @Schema(description = "Сложность вопроса")
    private Difficulty difficulty;

    @Schema(description = "Тип вопроса")
    private QuestionType questionType;

    @Schema(description = "Фрагмент кода")
    private String codeSnippet;

    @Schema(description = "Язык кода")
    private CodeLanguage codeLanguage;

    @Schema(description = "Варианты ответов без указания правильного")
    private List<AnswerQuizDto> answers;

    public QuestionQuizDto() {
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

    public List<AnswerQuizDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerQuizDto> answers) {
        this.answers = answers;
    }
}