package dev.codequiz.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// Request-объект для создания/редактирования категории — используется
// в админ-эндпоинтах. У создания и обновления одинаковый набор полей,
// поэтому один DTO закрывает оба случая (POST и PUT).
@Schema(description = "Данные для создания или обновления категории")
public class CategoryCreateDto {

    @Schema(description = "Название категории", example = "Java")
    @NotBlank(message = "Название обязательно")
    @Size(max = 100, message = "Название не длиннее 100 символов")
    private String name;

    @Schema(description = "URL-идентификатор категории", example = "java")
    @NotBlank(message = "Slug обязателен")
    @Size(max = 100, message = "Slug не длиннее 100 символов")
    private String slug;

    @Schema(description = "Описание категории")
    @Size(max = 1000, message = "Описание не длиннее 1000 символов")
    private String description;

    @Schema(description = "Порядок отображения (чем меньше, тем выше в списке)", example = "1")
    @PositiveOrZero(message = "Порядок отображения не может быть отрицательным")
    private int displayOrder;

    @Schema(description = "Активна ли категория (видна ли пользователям)", example = "true")
    private boolean active;

    public CategoryCreateDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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