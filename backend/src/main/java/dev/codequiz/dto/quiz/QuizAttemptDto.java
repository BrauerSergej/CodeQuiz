package dev.codequiz.dto.quiz;

import dev.codequiz.domain.enums.QuizAttemptStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// Response-объект с текущим состоянием попытки — отдаётся после старта,
// после каждого ответа (обновлённый счёт) и при просмотре истории.
@Schema(description = "Состояние попытки прохождения квиза")
public class QuizAttemptDto {

    @Schema(description = "Идентификатор попытки", example = "1")
    private Long id;

    @Schema(description = "Идентификатор темы", example = "1")
    private Long topicId;

    @Schema(description = "Общее количество вопросов в попытке", example = "10")
    private int totalQuestions;

    @Schema(description = "Количество правильных ответов на текущий момент", example = "7")
    private int correctAnswers;

    @Schema(description = "Текущий счёт", example = "70")
    private int score;

    @Schema(description = "Статус попытки")
    private QuizAttemptStatus status;

    @Schema(description = "Время начала попытки")
    private LocalDateTime startedAt;

    @Schema(description = "Время завершения попытки (null, если ещё не завершена)")
    private LocalDateTime finishedAt;

    public QuizAttemptDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public QuizAttemptStatus getStatus() {
        return status;
    }

    public void setStatus(QuizAttemptStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}