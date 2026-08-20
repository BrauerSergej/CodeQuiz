package dev.codequiz.exception;

// Бросается, если код уже был выдан совсем недавно (см. RESEND_COOLDOWN_SECONDS
// в AuthService) — простая защита от спама повторными запросами кода
// (можно было бы засыпать почту жертвы письмами, если бы такого ограничения не было).
public class ResendTooSoonException extends RuntimeException {

    public ResendTooSoonException(String message) {
        super(message);
    }
}