package dev.codequiz.exception;

// Бросается при создании/обновлении категории, если slug уже занят другой
// категорией — slug должен быть уникальным (используется в URL).
public class SlugAlreadyExistsException extends RuntimeException {

    public SlugAlreadyExistsException(String message) {
        super(message);
    }
}