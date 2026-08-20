package dev.codequiz.exception;

// Отдельно от InvalidConfirmationCodeException — истёкший код это другая
// ситуация для пользователя (нужно запросить новый код), а не "вы ошиблись
// при вводе". Разделение позволяет контроллеру/фронтенду показать разные
// сообщения и, например, сразу предложить кнопку "отправить код повторно".
public class ConfirmationCodeExpiredException extends RuntimeException {

    public ConfirmationCodeExpiredException(String message) {
        super(message);
    }
}