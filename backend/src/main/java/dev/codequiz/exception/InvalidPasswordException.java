package dev.codequiz.exception;

// Бросается при смене пароля, если пользователь неверно указал ТЕКУЩИЙ
// пароль. Отдельно от InvalidCredentialsException, потому что здесь
// пользователь уже аутентифицирован (логин прошёл ранее) — это не риск
// user enumeration, а просто проверка знания старого пароля перед сменой.
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}