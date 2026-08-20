package dev.codequiz.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// Request-объект для создания/обновления темы внутри категории.
@Schema(description = "Данные для создания или обновления темы")
public class TopicCreateDto {

    @Schema(description = "Название темы", example = "Коллекции")
    @NotBlank(message = "Название обязательно")
    @Size(max = 100, message = "Название не длиннее 100 символов")
    private String name;

    @Schema(description = "Описание темы")
    @Size(max = 1000, message = "Описание не длиннее 1000 символов")
    private String description;

    @Schema(description = "Идентификатор родительской категории", example = "1")
    @NotNull(message = "Категория обязательна")
    private Long categoryId;

    @Schema(description = "Порядок отображения внутри категории", example = "1")
    @PositiveOrZero(message = "Порядок отображения не может быть отрицательным")
    private int displayOrder;

    @Schema(description = "Активна ли тема", example = "true")
    private boolean active;

    public TopicCreateDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}