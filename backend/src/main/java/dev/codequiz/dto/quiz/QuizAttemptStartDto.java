package dev.codequiz.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Request-объект для старта новой попытки прохождения квиза —
// POST /quiz-attempts. Пользователь выбирает тему и желаемое количество
// вопросов, id пользователя сервер берёт из аутентификации, а не из тела
// запроса (иначе можно было бы начать попытку от чужого имени).
@Schema(description = "Данные для старта новой попытки квиза")
public class QuizAttemptStartDto {

    @Schema(description = "Идентификатор темы", example = "1")
    @NotNull(message = "Тема обязательна")
    private Long topicId;

    @Schema(description = "Желаемое количество вопросов в попытке", example = "10")
    @NotNull(message = "Количество вопросов обязательно")
    @Min(value = 1, message = "Минимум 1 вопрос")
    @Max(value = 50, message = "Максимум 50 вопросов за попытку")
    private Integer totalQuestions;

    public QuizAttemptStartDto() {
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
}