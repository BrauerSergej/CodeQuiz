package dev.codequiz.exception;

// Бросается при логине, если email/пароль верны, но аккаунт ещё не
// подтверждён (accountStatus == UNCONFIRMED) или заблокирован администратором
// (BLOCKED). Отдельно от InvalidCredentialsException, потому что здесь
// сообщение пользователю ДОЛЖНО отличаться ("подтвердите email", а не
// "неверный пароль") — это не риск user enumeration, раз пароль уже
// подтверждён как верный на этом шаге.
public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(String message) {
        super(message);
    }
}