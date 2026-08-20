package dev.codequiz.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;

// Response-объект для темы. Отдаём categoryId и categoryName плоскими полями,
// а не вложенным CategoryDto — этого обычно достаточно клиенту (например,
// хлебные крошки "Java / Коллекции"), и не нужно тянуть за собой весь объект
// категории со всеми её полями ради одного имени.
@Schema(description = "Данные темы")
public class TopicDto {

    @Schema(description = "Идентификатор темы", example = "1")
    private Long id;

    @Schema(description = "Название темы", example = "Коллекции")
    private String name;

    @Schema(description = "Описание темы")
    private String description;

    @Schema(description = "Идентификатор родительской категории", example = "1")
    private Long categoryId;

    @Schema(description = "Название родительской категории", example = "Java")
    private String categoryName;

    @Schema(description = "Порядок отображения", example = "1")
    private int displayOrder;

    @Schema(description = "Активна ли тема", example = "true")
    private boolean active;

    public TopicDto() {
    }

    public TopicDto(Long id, String name, String description, Long categoryId,
                    String categoryName, int displayOrder, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.displayOrder = displayOrder;
        this.active = active;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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