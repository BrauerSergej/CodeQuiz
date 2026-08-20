package dev.codequiz.exception;

// Бросается, когда пользователь не найден по id/email — независимо от того,
// в каком сервисе это произошло (профиль, смена пароля, логин по несуществующему
// email обрабатывается отдельно через InvalidCredentialsException — см. пояснение
// в AuthService, почему это разные исключения).
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}