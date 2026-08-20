package dev.codequiz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// Тонкая обёртка над JavaMailSender — единственная задача: собрать письмо
// с кодом подтверждения и отправить. Сам JavaMailSender бин Spring Boot
// создаёт автоматически по свойствам spring.mail.* (см. application-local.yaml),
// нам не нужно настраивать его вручную.
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    // Адрес отправителя — берём из того же spring.mail.username, что
    // используется для SMTP-аутентификации (у большинства провайдеров
    // "от кого" и "логин для входа" — один и тот же адрес).
    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // SimpleMailMessage — только plain text, без HTML и вложений. Для кода
    // подтверждения из 6 цифр этого достаточно, красивый HTML-шаблон можно
    // добавить позже (MimeMessage + Thymeleaf), если понадобится.
    //
    // Ошибку отправки НЕ пробрасываем дальше: register() в AuthService
    // выполняется в одной транзакции с сохранением пользователя и кода —
    // если бы отправка бросала исключение наружу, вся регистрация
    // откатывалась бы при любой проблеме с SMTP (например, если
    // MAIL_USERNAME/MAIL_PASSWORD ещё не настроены при локальной
    // разработке). Вместо этого логируем ошибку и код — на локальной
    // машине разработчик может подтвердить email, прочитав код из лога.
    public void sendConfirmationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Код подтверждения CodeQuiz");
        message.setText("Ваш код подтверждения: " + code
                + "\n\nКод действителен 15 минут. Если вы не регистрировались в CodeQuiz, проигнорируйте это письмо.");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("Не удалось отправить письмо с кодом подтверждения на {}: {}. " +
                    "Код для ручной проверки: {}", toEmail, e.getMessage(), code);
        }
    }
}
