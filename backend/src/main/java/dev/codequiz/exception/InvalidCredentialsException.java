package dev.codequiz.exception;

// Бросается при неверном email или пароле во время логина. Намеренно ОДНО
// исключение на оба случая (а не отдельно "пользователь не найден" и
// "неверный пароль") — иначе по разнице в ответе сервера/времени отклика
// злоумышленник мог бы определить, зарегистрирован ли конкретный email
// в системе (user enumeration).
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}