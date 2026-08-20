package dev.codequiz.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// Response-объект — то, что отдаётся клиенту для списка/деталей категории.
@Schema(description = "Данные категории")
public class CategoryDto {

    @Schema(description = "Идентификатор категории", example = "1")
    private Long id;

    @Schema(description = "Название категории", example = "Java")
    private String name;

    @Schema(description = "URL-идентификатор категории", example = "java")
    private String slug;

    @Schema(description = "Описание категории")
    private String description;

    @Schema(description = "Порядок отображения", example = "1")
    private int displayOrder;

    @Schema(description = "Активна ли категория", example = "true")
    private boolean active;

    @Schema(description = "Дата создания")
    private LocalDateTime createdAt;

    public CategoryDto() {
    }

    public CategoryDto(Long id, String name, String slug, String description,
                       int displayOrder, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.displayOrder = displayOrder;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}