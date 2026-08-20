package dev.codequiz.exception;

// Бросается, когда категория не найдена по id или slug.
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String message) {
        super(message);
    }
}